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
package org.tools4j.hoverraft.ipc;

/**
 * Timeout request to initiate leadership transfer.
 */
public final class TimeoutNow extends Message {

    public static final int MESSAGE_SIZE = 8;

    public TimeoutNow() {
        super(MESSAGE_SIZE);
    }

    public int term() {
        return readBuffer.getInt(offset);
    }

    public TimeoutNow term(final int term) {
        writeBuffer.putInt(offset, term);
        return this;
    }

    public int candidateId() {
        return readBuffer.getInt(offset + 4);
    }

    public TimeoutNow candidateId(final int candidateId) {
        writeBuffer.putInt(offset + 4, candidateId);
        return this;
    }

}
