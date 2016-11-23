package org.tools4j.hoverraft.state;

public interface CommandLog {
    enum CONTAINMENT {IN, OUT, CONFLICT}
    long size();
    long readIndex();
    void readIndex(long index);
    CommandLogEntry read();
    void append(CommandLogEntry commandLogEntry);
    void truncateIncluding(long index);
    LogEntry lastEntry();

    default CONTAINMENT contains(final LogEntry logEntry) {
        if (logEntry.index() > lastEntry().index()) {
            return CONTAINMENT.OUT;
        } else {
            readIndex(logEntry.index());
            final int termAtLogEntryIndex = read().term();
            if (logEntry.term() != termAtLogEntryIndex) {
                return CONTAINMENT.CONFLICT;
            } else {
                return CONTAINMENT.IN;
            }
        }
    }

}
