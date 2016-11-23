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
import org.tools4j.hoverraft.command.Command;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.direct.PayloadType;


public final class DirectCommandMessage extends AbstractDirectMessage implements CommandMessage {

    private static final int COMMAND_SOURCE_ID_OFF = TYPE_OFF + TYPE_LEN;
    private static final int COMMAND_SOURCE_ID_LEN = 4;
    private static final int COMMAND_INDEX_OFF = COMMAND_SOURCE_ID_OFF + COMMAND_SOURCE_ID_LEN;
    private static final int COMMAND_INDEX_LEN = 8;
    private static final int COMMAND_OFF = COMMAND_INDEX_OFF + COMMAND_INDEX_LEN;

    public static final int EMPTY_COMMAND_BYTE_LENGTH = COMMAND_OFF;

    private final DirectCommand command = new DirectCommand();

    @Override
    public PayloadType type() {
        return PayloadType.COMMAND_MESSAGE;
    }

    @Override
    public int byteLength() {
        return EMPTY_COMMAND_BYTE_LENGTH + command.byteLength();
    }

    @Override
    public int commandSourceId() {
        return readBuffer.getInt(offset + COMMAND_SOURCE_ID_OFF);
    }

    @Override
    public DirectCommandMessage commandSourceId(final int sourceId) {
        writeBuffer.putInt(offset + COMMAND_SOURCE_ID_OFF, sourceId);
        return this;
    }

    @Override
    public long commandIndex() {
        return readBuffer.getLong(offset + COMMAND_INDEX_OFF);
    }

    @Override
    public DirectCommandMessage commandIndex(final long commandIndex) {
        writeBuffer.putLong(offset + COMMAND_INDEX_OFF, commandIndex);
        return this;
    }

    @Override
    public Command command() {
        return command;
    }

    @Override
    public void wrap(DirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        command.wrap(buffer, offset + COMMAND_OFF);
    }

    @Override
    public void wrap(MutableDirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        command.wrap(buffer, offset + COMMAND_OFF);
    }

    @Override
    public void unwrap() {
        command.unwrap();
        super.unwrap();
    }
}
