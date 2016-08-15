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

import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.VoteResponse;

public final class DirectVoteResponse extends AbstractMessage implements VoteResponse<DirectMessage> {

    private static final byte GRANTED = 1;
    private static final byte DENIED = 0;

    private static final int TERM_OFF = TYPE_OFF + TYPE_LEN;
    private static final int TERM_LEN = 4;
    private static final int GRANTED_OFF = TERM_OFF + TERM_LEN;
    private static final int GRANTED_LEN = 1;

    public static final int BYTE_LENGTH = GRANTED_OFF + GRANTED_LEN;

    @Override
    public MessageType type() {
        return MessageType.VOTE_RESPONSE;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
    }

    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    public DirectVoteResponse term(final int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    public boolean voteGranted() {
        return readBuffer.getByte(offset + GRANTED_OFF) == GRANTED;
    }

    public DirectVoteResponse voteGranted(final boolean granted) {
        writeBuffer.putByte(offset + GRANTED_OFF, granted ? GRANTED : DENIED);
        return this;
    }
}
