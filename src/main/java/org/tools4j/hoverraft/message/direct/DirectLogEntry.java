package org.tools4j.hoverraft.message.direct;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.jetbrains.annotations.NotNull;
import org.tools4j.hoverraft.state.LogEntry;
import org.tools4j.hoverraft.state.LogEntryComparator;

import java.util.Comparator;
import java.util.Objects;

public class DirectLogEntry implements DirectPayload, LogEntry {
    private static final Comparator<LogEntry> LOG_ENTRY_COMPARATOR = new LogEntryComparator();

    protected static final int TERM_OFF = 0;
    protected static final int TERM_LEN = 4;

    protected static final int INDEX_OFF = TERM_OFF + TERM_LEN;
    protected static final int INDEX_LEN = 8;

    public static final int BYTE_LENGTH = INDEX_OFF + INDEX_LEN;

    protected DirectBuffer readBuffer;
    protected MutableDirectBuffer writeBuffer;
    protected int offset;

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public DirectBuffer buffer() {
        return readBuffer;
    }

    @Override
    public void wrap(final DirectBuffer buffer, final int offset) {
        this.readBuffer = Objects.requireNonNull(buffer);
        this.writeBuffer = null;
        this.offset = offset;
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        Objects.requireNonNull(buffer);
        this.readBuffer = buffer;
        this.writeBuffer = buffer;
        this.offset = offset;
    }

    @Override
    public void unwrap() {
        this.readBuffer = null;
        this.writeBuffer = null;
        this.offset = 0;
    }

    @Override
    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    @Override
    public long index() {
        return readBuffer.getLong(offset + INDEX_OFF);
    }

    @Override
    public LogEntry term(int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    @Override
    public LogEntry index(long index) {
        writeBuffer.putLong(offset + INDEX_OFF, index);
        return this;
    }

    @Override
    public int compareTo(@NotNull LogEntry logEntry) {
        return LOG_ENTRY_COMPARATOR.compare(this, logEntry);
    }

}
