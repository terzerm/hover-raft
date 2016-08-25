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
import org.tools4j.hoverraft.message.VoteRequest;

public final class DirectVoteRequest extends AbstractDirectMessage implements VoteRequest {

    private static final int TERM_OFF = TYPE_OFF + TYPE_LEN;
    private static final int TERM_LEN = 4;
    private static final int CANDIDATE_ID_OFF = TERM_OFF + TERM_LEN;
    private static final int CANDIDATE_ID_LEN = 4;
    private static final int LAST_LOG_TERM_OFF = CANDIDATE_ID_OFF + CANDIDATE_ID_LEN;
    private static final int LAST_LOG_TERM_LEN = 4;
    private static final int LAST_LOG_INDEX_OFF = LAST_LOG_TERM_OFF + LAST_LOG_TERM_LEN;
    private static final int LAST_LOG_INDEX_LEN = 8;
    public static final int BYTE_LENGTH = LAST_LOG_INDEX_OFF + LAST_LOG_INDEX_LEN;

    @Override
    public MessageType type() {
        return MessageType.VOTE_REQUEST;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
    }

    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    public DirectVoteRequest term(final int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    public int candidateId() {
        return readBuffer.getInt(offset + CANDIDATE_ID_OFF);
    }

    public DirectVoteRequest candidateId(final int candidateId) {
        writeBuffer.putInt(offset + CANDIDATE_ID_OFF, candidateId);
        return this;
    }

    public int lastLogTerm() {
        return readBuffer.getInt(offset + LAST_LOG_TERM_OFF);
    }

    public DirectVoteRequest lastLogTerm(final int lastLogTerm) {
        writeBuffer.putInt(offset + LAST_LOG_TERM_OFF, lastLogTerm);
        return this;
    }

    public long lastLogIndex() {
        return readBuffer.getLong(offset + LAST_LOG_INDEX_OFF);
    }

    public DirectVoteRequest lastLogIndex(final int lastLogIndex) {
        writeBuffer.putLong(offset + LAST_LOG_INDEX_OFF, lastLogIndex);
        return this;
    }

}
