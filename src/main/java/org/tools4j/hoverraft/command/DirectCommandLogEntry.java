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
package org.tools4j.hoverraft.command;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.command.CommandLogEntry;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.direct.DirectCommandMessage;
import org.tools4j.hoverraft.message.direct.DirectLogEntry;

public class DirectCommandLogEntry extends DirectLogEntry implements CommandLogEntry {
    private static final int COMMAND_MSG_OFF = DirectLogEntry.BYTE_LENGTH;

    private DirectCommandMessage directCommandMessage = new DirectCommandMessage();

    @Override
    public void wrap(final DirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    @Override
    public void unwrap() {
        directCommandMessage.unwrap();
        super.unwrap();
    }


    @Override
    public CommandMessage commandMessage() {
        return directCommandMessage;
    }

    @Override
    public int byteLength() {
        return COMMAND_MSG_OFF + directCommandMessage.byteLength();
    }

}
