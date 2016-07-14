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
package org.tools4j.hoverraft.message;

import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.ipc.AbstractMessage;

import java.nio.ByteBuffer;

public final class CommandAbstractMessage extends AbstractMessage {

    public static final int MESSAGE_SIZE = 4 + 4 + 8;

    public CommandAbstractMessage() {
        super(MESSAGE_SIZE);
        wrap(new UnsafeBuffer(ByteBuffer.allocateDirect(MESSAGE_SIZE)), 0);
    }

    public int term() {
        return readBuffer.getInt(offset);
    }

    public CommandAbstractMessage term(final int term) {
        writeBuffer.putInt(offset, term);
        return this;
    }

    public int commandSourceId() {
        return readBuffer.getInt(offset + 4);
    }

    public CommandAbstractMessage commandSourceId(final int sourceId) {
        writeBuffer.putInt(offset + 4, sourceId);
        return this;
    }

    public long commandIndex() {
        return readBuffer.getLong(offset + 8);
    }

    public CommandAbstractMessage commandIndex(final long commandIndex) {
        writeBuffer.putLong(offset + 8, commandIndex);
        return this;
    }

    public CommandAbstractMessage read(final MessageLog commandLog) {
        commandLog.read(writeBuffer, offset);
        return this;
    }
}
