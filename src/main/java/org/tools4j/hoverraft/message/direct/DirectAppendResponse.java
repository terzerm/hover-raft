/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 hover-raft (tools4j), Marco Terzer
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

import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.MessageType;

public final class DirectAppendResponse extends AbstractDirectMessage implements AppendResponse {

    private static final byte SUCCESSFUL = 1;
    private static final byte UNSUCCESSFUL = 0;

    private static final int TERM_OFF = TYPE_OFF + TYPE_LEN;
    private static final int TERM_LEN = 4;
    private static final int SUCCESSFUL_OFF = TERM_OFF + TERM_LEN;
    private static final int SUCCESSFUL_LEN = 1;

    private static final int SERVER_ID_OFF = SUCCESSFUL_OFF + SUCCESSFUL_LEN;
    private static final int SERVER_ID_LEN = 4;

    private static final int MATCH_LOG_INDEX_OFF = SERVER_ID_OFF + SERVER_ID_LEN;
    private static final int MATCH_LOG_INDEX__LEN = 8;

    public static final int BYTE_LENGTH = MATCH_LOG_INDEX_OFF + MATCH_LOG_INDEX__LEN;

    @Override
    public MessageType type() {
        return MessageType.APPEND_RESPONSE;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
    }

    @Override
    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    @Override
    public DirectAppendResponse term(final int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    @Override
    public boolean successful() {
        return readBuffer.getByte(offset + SUCCESSFUL_OFF) == 1;
    }

    @Override
    public DirectAppendResponse successful(final boolean successful) {
        writeBuffer.putByte(offset + SUCCESSFUL_OFF, successful ? SUCCESSFUL : UNSUCCESSFUL);
        return this;
    }

    public int serverId() {
        return readBuffer.getInt(offset + SERVER_ID_OFF);
    }

    @Override
    public DirectAppendResponse serverId(final int serverId) {
        writeBuffer.putInt(offset + SERVER_ID_OFF, serverId);
        return this;
    }

    public long matchLogIndex() {
        return readBuffer.getLong(offset + MATCH_LOG_INDEX_OFF);
    }

    @Override
    public DirectAppendResponse matchLogIndex(final long matchLogIndex) {
        writeBuffer.putLong(offset + MATCH_LOG_INDEX_OFF, matchLogIndex);
        return this;
    }
}
