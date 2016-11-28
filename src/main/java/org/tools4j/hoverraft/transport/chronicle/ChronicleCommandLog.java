/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 hover-raft (tools4j), Marco Terzer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.hoverraft.transport.chronicle;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.Excerpt;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.VanillaChronicle;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.command.*;
import org.tools4j.hoverraft.direct.RecyclingDirectFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * AbstractDirectMessage log based on a chronicle queue.
 */
public final class ChronicleCommandLog implements CommandLog {

    private static final int TERM_OFF = 0;
    private static final int TERM_LEN = 4;
    private static final int LENGTH_OFF = TERM_OFF + TERM_LEN;
    private static final int LENGTH_LEN = 4;
    private static final int COMMAND_OFF = LENGTH_OFF + LENGTH_LEN;

    private final Chronicle chronicle;
    private final Excerpt excerpt;
    private final ExcerptAppender appender;
    private final Method setLastWrittenIndexMethod;
    private final MutableDirectBuffer mutableDirectBuffer;
    private final RecyclingDirectFactory directFactory;

    public ChronicleCommandLog(final VanillaChronicle chronicle,
                               final MutableDirectBuffer mutableDirectBuffer) throws IOException {
        this.chronicle = Objects.requireNonNull(chronicle);
        this.excerpt = chronicle.createExcerpt();
        this.appender = chronicle.createAppender();
        this.setLastWrittenIndexMethod = initLastWrittenIndexMethod();
        this.mutableDirectBuffer = Objects.requireNonNull(mutableDirectBuffer);
        this.directFactory = new RecyclingDirectFactory();
    }

    public RecyclingDirectFactory messageFactory() {
        return directFactory;
    }

    @Override
    public long size() {
        return excerpt.size();
    }

    @Override
    public void truncateIncluding(final long size) {
        try {
            excerpt.index(size - 1);
            setLastWrittenIndexMethod.invoke(appender, size - 1);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void readIndex(final long index) {
        if (!excerpt.index(index)) {
            throw new IllegalArgumentException("illegal index: " + index);
        }
    }

    @Override
    public int readTerm(final long index) {
        readIndex(index);
        excerpt.skipBytes(TERM_OFF);
        return excerpt.readInt();
    }

    @Override
    public void readKeyTo(final long index, final LogKey target) {
        target.term(readTerm(index)).index(index);
    }

    @Override
    public int lastTerm() {
        return readTerm(appender.index());
    }

    @Override
    public void lastKeyTo(final LogKey target) {
        readKeyTo(appender.index(), target);
    }

    @Override
    public void readTo(final long index, final LogEntry target) {
        final MutableDirectBuffer mutableDirectBuffer = Objects.requireNonNull(target.writeBufferOrNull(), "target write buffer must be initialised");
        readIndex(index);
        final int term = excerpt.readInt();
        final int len = excerpt.readInt();
        target.logKey().term(term).index(index);
        final int offset = target.command().offset();
        for (int i = 0; i < len; ) {
            if (i + 8 <= len) {
                mutableDirectBuffer.putLong(offset + i, excerpt.readLong());
                i += 8;
            } else {
                mutableDirectBuffer.putByte(offset + i, excerpt.readByte());
                i += 1;
            }
        }
    }

    @Override
    public void append(final int term, final Command message) {
        final DirectBuffer buffer = Objects.requireNonNull(message.readBufferOrNull());
        final int len = message.byteLength();
        appender.startExcerpt(len + TERM_LEN + LENGTH_LEN);
        appender.writeInt(term);
        appender.writeInt(len);
        final int offset = message.offset();
        for (int i = 0; i < len; ) {
            if (i + 8 <= len) {
                appender.writeLong(buffer.getLong(offset + i));
                i += 8;
            } else {
                appender.writeByte(buffer.getByte(offset + i));
                i += 1;
            }
        }
        appender.finish();
    }

    @Override
    public boolean contains(CommandKey commandKey) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public LogContainment contains(final LogKey logKey) {
        return LogContainment.containmentFor(logKey, this);
    }

    //VanillaChronicle.VanillaAppenderImpl:
    //  protected void setLastWrittenIndex(long lastWrittenIndex)
    private Method initLastWrittenIndexMethod() {
        try {
            final Method method = appender.getClass().getDeclaredMethod("setLastWrittenIndex", long.class);
            method.setAccessible(true);
            return method;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
