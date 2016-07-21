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
package org.tools4j.hoverraft.chronicle;

import io.aeron.logbuffer.FragmentHandler;
import net.openhft.chronicle.ExcerptTailer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.io.Subscription;

import java.util.Objects;

/**
 * Subscription reading from a chronicle queue.
 */
public class ChronicleSubscription implements Subscription {

    private final ExcerptTailer tailer;
    private final MutableDirectBuffer buffer;

    public ChronicleSubscription(final ExcerptTailer tailer, final int initialBufferCapacity) {
        this.tailer = Objects.requireNonNull(tailer);
        this.buffer = new ExpandableArrayBuffer(initialBufferCapacity);
    }

    @Override
    public int poll(final FragmentHandler fragmentHandler, final int fragmentLimit) {
        int fragmentsRead = 0;
        while (fragmentsRead < fragmentLimit && tailer.nextIndex()) {
            final int len = tailer.readInt();
            for (int i = 0; i < len; ) {
                if (i + 8 <= len) {
                    buffer.putLong(i, tailer.readLong());
                    i += 8;
                } else {
                    buffer.putByte(i, tailer.readByte());
                    i++;
                }
            }
            fragmentsRead++;
        }
        return fragmentsRead;
    }
}
