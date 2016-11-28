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
import org.tools4j.hoverraft.direct.AbstractDirectPayload;
import org.tools4j.hoverraft.message.direct.DirectCommand;
import org.tools4j.hoverraft.message.direct.DirectLogKey;

public class DirectLogEntry extends AbstractDirectPayload implements LogEntry {
    private static final int LOG_KEY_OFF = 0;
    private static final int LOG_KEY_LEN = DirectLogKey.BYTE_LENGTH;

    private static final int COMMAND_OFF = LOG_KEY_OFF + LOG_KEY_LEN;

    private DirectLogKey directLogKey = new DirectLogKey();
    private DirectCommand directCommand = new DirectCommand();

    @Override
    public void wrap(final DirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directLogKey.wrap(buffer, offset + LOG_KEY_OFF);
        directCommand.wrap(buffer, offset + COMMAND_OFF);
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directLogKey.wrap(buffer, offset + LOG_KEY_OFF);
        directCommand.wrap(buffer, offset + COMMAND_OFF);
    }

    @Override
    public void unwrap() {
        directLogKey.unwrap();
        directCommand.unwrap();
        super.unwrap();
    }


    @Override
    public Command command() {
        return directCommand;
    }

    @Override
    public LogKey logKey() {
        return directLogKey;
    }

    @Override
    public int byteLength() {
        return COMMAND_OFF + directCommand.byteLength();
    }
}
