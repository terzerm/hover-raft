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
    private static final int SOURCE_ID_OFF = TERM_OFF + TERM_LEN;
    private static final int SOURCE_ID_LEN = 4;
    private static final int COMMAND_INDEX_OFF = SOURCE_ID_OFF + SOURCE_ID_LEN;
    private static final int COMMAND_INDEX_LEN = 8;
    private static final int LENGTH_OFF = COMMAND_INDEX_OFF + COMMAND_INDEX_LEN;
    private static final int LENGTH_LEN = 4;
    private static final int COMMAND_OFF = LENGTH_OFF + LENGTH_LEN;

    private final Chronicle chronicle;
    private final Excerpt excerpt;
    private final ExcerptAppender appender;
    private final Method setLastWrittenIndexMethod;
    private final MutableDirectBuffer mutableDirectBuffer;
//    private final DirectFactory directFactory;
    private final CommandKeyLookup commandKeyLookup = new CommandKeyLookup(this);

    public ChronicleCommandLog(final VanillaChronicle chronicle,
                               final MutableDirectBuffer mutableDirectBuffer) throws IOException {
        this.chronicle = Objects.requireNonNull(chronicle);
        this.excerpt = chronicle.createExcerpt();
        this.appender = chronicle.createAppender();
        this.setLastWrittenIndexMethod = initLastWrittenIndexMethod();
        this.mutableDirectBuffer = Objects.requireNonNull(mutableDirectBuffer);
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
            commandKeyLookup.clear();
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
    public void readTo(final long index, final LogKey target) {
        target.term(readTerm(index)).index(index);
    }

    @Override
    public void readTo(final long index, final CommandKey target) {
        readIndex(index);
        excerpt.skipBytes(SOURCE_ID_OFF);
        final int sourceId = excerpt.readInt();
        final long commandIndex = excerpt.readLong();
        target.sourceId(sourceId).commandIndex(commandIndex);
    }

    @Override
    public int lastTerm() {
        return readTerm(appender.index());
    }

    @Override
    public void lastKeyTo(final LogKey target) {
        readTo(appender.index(), target);
    }

    @Override
    public void readTo(final long index, final LogEntry target) {
        final MutableDirectBuffer mutableDirectBuffer = Objects.requireNonNull(target.writeBufferOrNull(), "target write buffer must be initialised");
        readIndex(index);
        final int term = excerpt.readInt();
        final int sourceId = excerpt.readInt();
        final long commandIndex = excerpt.readLong();
        final int len = excerpt.readInt();
        final int offset = target.command().offset();
        for (int i = SOURCE_ID_LEN + COMMAND_INDEX_LEN; i < len; ) {
            if (i + 8 <= len) {
                mutableDirectBuffer.putLong(offset + i, excerpt.readLong());
                i += 8;
            } else {
                mutableDirectBuffer.putByte(offset + i, excerpt.readByte());
                i += 1;
            }
        }
        target.logKey()
                .term(term)
                .index(index);
        target.command().commandKey()
                .sourceId(sourceId)
                .commandIndex(commandIndex);
    }

    @Override
    public void append(final int term, final Command command) {
        final DirectBuffer buffer = Objects.requireNonNull(command.readBufferOrNull());
        final int len = command.byteLength();
        appender.startExcerpt(len + TERM_LEN + LENGTH_LEN);
        appender.writeInt(term);
        appender.writeInt(command.commandKey().sourceId());
        appender.writeLong(command.commandKey().commandIndex());
        appender.writeInt(len);
        final int offset = command.offset();
        for (int i = SOURCE_ID_LEN + COMMAND_INDEX_LEN; i < len; ) {
            if (i + 8 <= len) {
                appender.writeLong(buffer.getLong(offset + i));
                i += 8;
            } else {
                appender.writeByte(buffer.getByte(offset + i));
                i += 1;
            }
        }
        appender.finish();
        commandKeyLookup.append(command.commandKey());
    }

    @Override
    public boolean contains(final CommandKey commandKey) {
        return commandKeyLookup.contains(commandKey);
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
