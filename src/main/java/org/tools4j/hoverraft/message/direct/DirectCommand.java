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
import org.tools4j.hoverraft.command.CommandKey;
import org.tools4j.hoverraft.command.CommandPayload;
import org.tools4j.hoverraft.direct.AbstractDirectPayload;

public final class DirectCommand extends AbstractDirectPayload implements Command {

    private static final int COMMAND_KEY_OFF = 0;
    private static final int COMMAND_KEY_LEN = DirectCommandKey.BYTE_LENGTH;
    private static final int COMMAND_PAYLOAD_OFF = COMMAND_KEY_OFF + COMMAND_KEY_LEN;

    public static final int EMPTY_COMMAND_BYTE_LENGTH = COMMAND_PAYLOAD_OFF;

    private final DirectCommandKey commandKey = new DirectCommandKey();
    private final DirectCommandPayload commandPayload = new DirectCommandPayload();

    @Override
    public int byteLength() {
        return EMPTY_COMMAND_BYTE_LENGTH + commandPayload.commandByteLength();
    }

    @Override
    public CommandKey commandKey() {
        return commandKey;
    }

    @Override
    public CommandPayload commandPayload() {
        return commandPayload;
    }

    @Override
    public void wrap(final DirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        commandKey.wrap(buffer, offset + COMMAND_KEY_OFF);
        commandPayload.wrap(buffer, offset + COMMAND_PAYLOAD_OFF);
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        commandKey.wrap(buffer, offset + COMMAND_KEY_OFF);
        commandPayload.wrap(buffer, offset + COMMAND_PAYLOAD_OFF);
    }

    @Override
    public void unwrap() {
        commandPayload.unwrap();
        commandKey.unwrap();
        super.unwrap();
    }

}
