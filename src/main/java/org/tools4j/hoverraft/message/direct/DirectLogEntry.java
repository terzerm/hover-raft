package org.tools4j.hoverraft.message.direct;

import org.jetbrains.annotations.NotNull;
import org.tools4j.hoverraft.state.LogEntry;
import org.tools4j.hoverraft.state.LogEntryComparator;

import java.util.Comparator;

public class DirectLogEntry extends AbstractDirectPayload implements LogEntry {
    private static final Comparator<LogEntry> LOG_ENTRY_COMPARATOR = new LogEntryComparator();

    protected static final int TERM_OFF = 0;
    protected static final int TERM_LEN = 4;

    protected static final int INDEX_OFF = TERM_OFF + TERM_LEN;
    protected static final int INDEX_LEN = 8;

    public static final int BYTE_LENGTH = INDEX_OFF + INDEX_LEN;

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
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
    public int compareTo(final @NotNull LogEntry logEntry) {
        return LOG_ENTRY_COMPARATOR.compare(this, logEntry);
    }

}
