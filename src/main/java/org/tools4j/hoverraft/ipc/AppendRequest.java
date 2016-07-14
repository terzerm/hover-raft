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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public final class AppendRequest extends AbstractMessage {

    /** Byte length without content*/
    public static final int BYTE_LENGTH = 4 + 4 + 4 + 8 + 8 + 4;

    @Override
    public int byteLength() {
        return BYTE_LENGTH + contentByteLength();
    }

    public int term() {
        return readBuffer.getInt(offset);
    }

    public int leaderId() {
        return readBuffer.getInt(offset + 4);
    }

    public int prevLogTerm() {
        return readBuffer.getInt(offset + 8);
    }

    public long prevLogIndex() {
        return readBuffer.getLong(offset + 12);
    }

    public long leaderCommit() {
        return readBuffer.getLong(offset + 20);
    }

    public int contentByteLength() {
        return readBuffer.getInt(offset + 28);
    }

    public int contentOffset() {
        return BYTE_LENGTH;
    }

    public DirectBuffer readBuffer() {
        return readBuffer;
    }

    public MutableDirectBuffer writeBuffer() {
        return writeBuffer;
    }
}
