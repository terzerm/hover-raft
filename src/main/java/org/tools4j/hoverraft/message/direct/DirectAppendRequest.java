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
import org.tools4j.hoverraft.command.LogEntry;
import org.tools4j.hoverraft.command.DirectLogEntry;
import org.tools4j.hoverraft.command.LogKey;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.MessageType;

import java.util.Iterator;

public final class DirectAppendRequest extends AbstractDirectMessage implements AppendRequest {

    /** Byte length without content*/
    private static final int TERM_OFF = TYPE_OFF + TYPE_LEN;
    private static final int TERM_LEN = 4;
    private static final int LEADER_ID_OFF = TERM_OFF + TERM_LEN;
    private static final int LEADER_ID_LEN = 4;

    private static final int PREV_LOG_KEY_OFF = LEADER_ID_OFF + LEADER_ID_LEN;
    private static final int PREV_LOG_KEY_LEN = DirectLogKey.BYTE_LENGTH;

    private static final int LEADER_COMMIT_OFF = PREV_LOG_KEY_OFF + PREV_LOG_KEY_LEN;
    private static final int LEADER_COMMIT_LEN = 8;

    private static final int LOG_BYTE_LENGTH_OFF  = LEADER_COMMIT_OFF + LEADER_COMMIT_LEN;
    private static final int LOG_BYTE_LENGTH_LEN  = 4;

    private static final int LOG_OFF = LOG_BYTE_LENGTH_OFF + LOG_BYTE_LENGTH_LEN;

    public static final int BYTE_LENGTH = LOG_OFF;

    private final DirectLogKey prevLogKey = new DirectLogKey() ;

    private final LogEntryIterator logEntryIterator = new LogEntryIterator();

    @Override
    public MessageType type() {
        return MessageType.APPEND_REQUEST;
    }

    @Override
    public int byteLength() {
        return BYTE_LENGTH + logByteLength();
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
    public LogKey prevLogKey() {
        return prevLogKey;
    }

    public long leaderCommit() {
        return readBuffer.getLong(offset + LEADER_COMMIT_OFF);
    }

    @Override
    public AppendRequest leaderCommit(final long leaderCommit) {
        writeBuffer.putLong(offset + LEADER_COMMIT_OFF, leaderCommit);
        return this;
    }

    private int logByteLength() {
        return readBuffer.getInt(offset + LOG_BYTE_LENGTH_OFF);
    }

    private void logByteLength(final int logByteLength) {
        writeBuffer.putInt(offset + LOG_BYTE_LENGTH_OFF, logByteLength);
    }

    @Override
    public Iterator<LogEntry> logEntryIterator() {
        return logEntryIterator.reset();
    }

    @Override
    public AppendRequest appendLogEntry(final LogEntry logEntry) {
        writeBuffer.putBytes(offset + byteLength(), logEntry.readBufferOrNull(), logEntry.offset(), logEntry.byteLength());
        logByteLength( logByteLength() + logEntry.byteLength());
        return this;
    }

    @Override
    public void wrap(DirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        prevLogKey.wrap(buffer, offset + PREV_LOG_KEY_OFF);
    }

    @Override
    public void wrap(MutableDirectBuffer buffer, int offset) {
        super.wrap(buffer, offset);
        prevLogKey.wrap(buffer, offset + PREV_LOG_KEY_OFF);
    }

    @Override
    public void unwrap() {
        prevLogKey.unwrap();
        super.unwrap();
    }

    private class LogEntryIterator implements Iterator<LogEntry> {
        final DirectLogEntry directLogEntry = new DirectLogEntry();
        private int currentOffset;

        @Override
        public boolean hasNext() {
            return currentOffset < offset + byteLength();
        }

        @Override
        public DirectLogEntry next() {
            directLogEntry.wrap(readBuffer, currentOffset);
            currentOffset = currentOffset + directLogEntry.byteLength();
            return directLogEntry;
        }

        private LogEntryIterator reset() {
            currentOffset = offset + BYTE_LENGTH;
            directLogEntry.unwrap();
            return this;
        }
    }
}
