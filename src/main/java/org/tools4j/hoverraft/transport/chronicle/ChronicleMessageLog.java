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
import org.tools4j.hoverraft.direct.RecyclingDirectFactory;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.transport.MessageLog;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * AbstractDirectMessage log based on a chronicle queue.
 */
public final class ChronicleMessageLog implements MessageLog<CommandMessage> {

    private final Chronicle chronicle;
    private final Excerpt excerpt;
    private final ExcerptAppender appender;
    private final Method setLastWrittenIndexMethod;
    private final MutableDirectBuffer mutableDirectBuffer;
    private final RecyclingDirectFactory directFactory;

    public ChronicleMessageLog(final VanillaChronicle chronicle,
                               final MutableDirectBuffer mutableDirectBuffer) throws IOException {
        this.chronicle = Objects.requireNonNull(chronicle);
        this.excerpt = chronicle.createExcerpt();
        this.appender = chronicle.createAppender();
        this.setLastWrittenIndexMethod = initLastWrittenIndexMethod();
        this.mutableDirectBuffer = Objects.requireNonNull(mutableDirectBuffer);
        this.directFactory = RecyclingDirectFactory.createForWriting(mutableDirectBuffer, 0);
    }

    public RecyclingDirectFactory messageFactory() {
        return directFactory;
    }

    @Override
    public long size() {
        return excerpt.size();
    }

    @Override
    public void size(long size) {
        try {
            excerpt.index(size - 1);
            setLastWrittenIndexMethod.invoke(appender, size - 1);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readIndex() {
        return excerpt.index();
    }

    @Override
    public void readIndex(long index) {
        if (!excerpt.index(index)) {
            throw new IllegalArgumentException("illegal index: " + index);
        }
    }

    @Override
    public CommandMessage read() {
        if (!excerpt.nextIndex()) {
            throw new IllegalStateException("end of message log, index=" + readIndex());
        }
        final int len = excerpt.readInt();
        for (int i = 0; i < len; ) {
            if (i + 8 <= len) {
                mutableDirectBuffer.putLong(i, excerpt.readLong());
                i += 8;
            } else {
                mutableDirectBuffer.putByte(i, excerpt.readByte());
                i++;
            }
        }
        return directFactory.commandMessage();
    }

    @Override
    public void append(final CommandMessage message) {
        final DirectBuffer buffer = Objects.requireNonNull(message.readBufferOrNull());
        final int len = message.byteLength();
        appender.startExcerpt(len + 4);
        appender.writeInt(len);
        final int end = len + 4;
        int index = 4;
        while (index < end) {
            if (index + 8 <= len) {
                appender.writeLong(buffer.getLong(index));
                index += 8;
            } else {
                appender.writeByte(buffer.getByte(index));
                index++;
            }
        }
        appender.finish();
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

    @Override
    public long truncateIncluding(long inclusiveIndex) {
        //FIXme implement it
        throw new UnsupportedOperationException("not yet implemented");
    }
}
