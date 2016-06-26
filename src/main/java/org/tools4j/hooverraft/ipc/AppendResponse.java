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
package org.tools4j.hooverraft.ipc;

public final class AppendResponse extends Message {

    private static final byte SUCCESSFUL = 1;
    private static final byte UNSUCCESSFUL = 0;

    public static final int MESSAGE_SIZE = 5;

    public AppendResponse() {
        super(MESSAGE_SIZE);
    }

    public int term() {
        return readBuffer.getInt(offset);
    }

    public AppendResponse term(final int term) {
        writeBuffer.putInt(offset, term);
        return this;
    }

    public boolean successful() {
        return readBuffer.getByte(offset + 4) == 1;
    }

    public AppendResponse successful(final boolean successful) {
        writeBuffer.putByte(offset + 4, successful ? SUCCESSFUL : UNSUCCESSFUL);
        return this;
    }

}
