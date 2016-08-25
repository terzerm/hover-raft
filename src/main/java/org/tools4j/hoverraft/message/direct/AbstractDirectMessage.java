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
package org.tools4j.hoverraft.message.direct;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.message.AbstractMessage;

import java.util.Objects;

abstract public class AbstractDirectMessage extends AbstractMessage implements DirectMessage {

    protected static final int TYPE_OFF = 0;
    protected static final int TYPE_LEN = 4;
    protected DirectBuffer readBuffer;
    protected MutableDirectBuffer writeBuffer;
    protected int offset;

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public DirectBuffer buffer() {
        return readBuffer;
    }

    public void wrap(final DirectBuffer buffer, final int offset) {
        if (buffer.getInt(offset) != type().ordinal()) {
            throw new IllegalArgumentException("Buffer must contain message type " + type());
        }
        this.readBuffer = Objects.requireNonNull(buffer);
        this.writeBuffer = null;
        this.offset = offset;
    }

    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        Objects.requireNonNull(buffer);
        this.readBuffer = buffer;
        this.writeBuffer = buffer;
        this.offset = offset;
        buffer.putInt(offset, type().ordinal());
    }

    public void unwrap() {
        this.readBuffer = null;
        this.writeBuffer = null;
        this.offset = 0;
    }
}
