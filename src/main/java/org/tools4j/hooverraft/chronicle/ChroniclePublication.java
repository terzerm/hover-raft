/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 hoover-raft (tools4j), Marco Terzer
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
package org.tools4j.hooverraft.chronicle;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.agrona.DirectBuffer;
import org.tools4j.hooverraft.message.Publication;

import java.util.Objects;

/**
 * Publication writing to a chronicle queue.
 */
public class ChroniclePublication implements Publication {

    private final ExcerptAppender appender;

    public ChroniclePublication(final ExcerptAppender appender) {
        this.appender = Objects.requireNonNull(appender);
    }

    @Override
    public long offer(final DirectBuffer buffer, final int offset, final int length) {
        appender.startExcerpt(length + 4);
        appender.writeInt(length);
        final int end = offset + length + 4;
        int index = offset + 4;
        while (index < end) {
            if (index + 8 <= length) {
                appender.writeLong(buffer.getLong(index));
                index += 8;
            } else {
                appender.writeByte(buffer.getByte(index));
                index++;
            }
        }
        appender.finish();
        return appender.position();
    }
}
