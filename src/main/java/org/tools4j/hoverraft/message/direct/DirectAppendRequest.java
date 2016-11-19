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
import org.tools4j.hoverraft.state.CommandLogEntry;
import org.tools4j.hoverraft.state.LogEntry;
import org.tools4j.hoverraft.state.direct.DirectCommandLogEntry;

public final class DirectAppendRequest extends AbstractDirectMessage implements AppendRequest {

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
    private static final int COMMAND_LOG_ENTRY_OFF = LEADER_COMMIT_OFF + LEADER_COMMIT_LEN;

    public static final int BYTE_LENGTH = COMMAND_LOG_ENTRY_OFF;

    private final DirectCommandLogEntry directCommandLogEntry = new DirectCommandLogEntry();

    private final LogEntry prevLogEntry = new LogEntry() {
        @Override
        public int term() {
            return readBuffer.getInt(offset + PREV_LOG_TERM_OFF);
        }

        @Override
        public long index() {
            return readBuffer.getLong(offset + PREV_LOG_INDEX_OFF);
        }

        @Override
        public LogEntry term(int term) {
            writeBuffer.putInt(offset + PREV_LOG_TERM_OFF, term);
            return this;
        }

        @Override
        public LogEntry index(long index) {
            writeBuffer.putLong(offset + PREV_LOG_INDEX_OFF, index);
            return this;
        }
    };

    @Override
    public MessageType type() {
        return MessageType.APPEND_REQUEST;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH + directCommandLogEntry.byteLength();
    }

    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    @Override
    public AppendRequest term(final int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    public int leaderId() {
        return readBuffer.getInt(offset + LEADER_ID_OFF);
    }

    @Override
    public AppendRequest leaderId(final int leaderId) {
        writeBuffer.putInt(offset + LEADER_ID_OFF, leaderId);
        return this;
    }

    @Override
    public LogEntry prevLogEntry() {
        return prevLogEntry;
    }

    public long leaderCommit() {
        return readBuffer.getLong(offset + LEADER_COMMIT_OFF);
    }

    @Override
    public AppendRequest leaderCommit(final long leaderCommit) {
        writeBuffer.putLong(offset + LEADER_COMMIT_OFF, leaderCommit);
        return this;
    }

    @Override
    public CommandLogEntry commandLogEntry() {
        return directCommandLogEntry;
    }

    @Override
    public void wrap(DirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        directCommandLogEntry.wrap(buffer, offset + COMMAND_LOG_ENTRY_OFF);
    }

    @Override
    public void wrap(MutableDirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        directCommandLogEntry.wrap(buffer, offset + COMMAND_LOG_ENTRY_OFF);
    }

    @Override
    public void unwrap() {
        super.unwrap();
        directCommandLogEntry.unwrap();
    }
}
