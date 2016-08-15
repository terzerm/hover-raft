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
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.UserMessage;

public final class DirectAppendRequest extends AbstractMessage implements AppendRequest<DirectMessage> {

    /** Byte length without content*/
    private static final int TERM_OFF = TYPE_OFF + TYPE_LEN;
    private static final int TERM_LEN = 4;
    private static final int LEADER_ID_OFF = TERM_OFF + TERM_LEN;
    private static final int LEADER_ID_LEN = 4;
    private static final int PREV_LOG_TERM_OFF = LEADER_ID_OFF + LEADER_ID_LEN;
    private static final int PREV_LOG_TERM_LEN = 4;
    private static final int PREV_LOG_INDEX_OFF = PREV_LOG_TERM_OFF + PREV_LOG_TERM_LEN;
    private static final int PREV_LOG_INDEX_LEN = 8;
    private static final int LEADER_COMMIT_OFF = PREV_LOG_INDEX_OFF + PREV_LOG_INDEX_LEN;
    private static final int LEADER_COMMIT_LEN = 8;
    private static final int USER_MESSAGE_OFF = LEADER_COMMIT_OFF + LEADER_COMMIT_LEN;
    private static final int USER_MESSAGE_LEN = 4;

    public static final int BYTE_LENGTH = USER_MESSAGE_OFF + USER_MESSAGE_LEN;

    private final DirectUserMessage userMessage = new DirectUserMessage();

    @Override
    public MessageType type() {
        return MessageType.APPEND_REQUEST;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH + userMessage.usserMessageLength();
    }

    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    @Override
    public AppendRequest term(final int term) {
        writeBuffer().putInt(offset + TERM_OFF, term);
        return this;
    }

    public int leaderId() {
        return readBuffer.getInt(offset + LEADER_ID_OFF);
    }

    @Override
    public AppendRequest leaderId(final int leaderId) {
        writeBuffer().putInt(offset + LEADER_ID_OFF, leaderId);
        return this;
    }

    public int prevLogTerm() {
        return readBuffer.getInt(offset + PREV_LOG_TERM_OFF);
    }

    @Override
    public AppendRequest prevLogTerm(final int prevLogTerm) {
        writeBuffer().putInt(offset + PREV_LOG_TERM_OFF, prevLogTerm);
        return this;
    }

    public long prevLogIndex() {
        return readBuffer.getLong(offset + PREV_LOG_INDEX_OFF);
    }

    @Override
    public AppendRequest prevLogIndex(final long prevLogIndex) {
        writeBuffer().putLong(offset + PREV_LOG_INDEX_OFF, prevLogIndex);
        return this;
    }

    public long leaderCommit() {
        return readBuffer.getLong(offset + LEADER_COMMIT_OFF);
    }

    @Override
    public AppendRequest leaderCommit(final long leaderCommit) {
        writeBuffer().putLong(offset + LEADER_COMMIT_OFF, leaderCommit);
        return this;
    }

    @Override
    public UserMessage userMessage() {
        return userMessage;
    }

    public DirectBuffer readBuffer() {
        return readBuffer;
    }

    public MutableDirectBuffer writeBuffer() {
        return writeBuffer;
    }

    public final class DirectUserMessage implements UserMessage {
        public int usserMessageLength() {
            return readBuffer.getInt(offset + USER_MESSAGE_OFF);
        }

        public UserMessage usserMessageLength(final int usserMessageLength) {
            writeBuffer().putInt(offset + USER_MESSAGE_OFF, usserMessageLength);
            return this;
        }

        public int userMessageOffset() {
            return USER_MESSAGE_OFF;
        }
    }
}
