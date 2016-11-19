package org.tools4j.hoverraft.state;

public interface CommandLog extends LogEntry, Comparable<LogEntry> {
    enum CONTAINMENT {IN, OUT, CONFLICT}
    long size();
    long readIndex();
    void readIndex(long index);
    CommandLogEntry read();
    void append(CommandLogEntry commandLogEntry);
    void truncate(long index);
    CONTAINMENT contains(LogEntry logEntry);
}
