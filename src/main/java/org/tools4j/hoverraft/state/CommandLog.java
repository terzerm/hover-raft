package org.tools4j.hoverraft.state;

public interface CommandLog {
    enum CONTAINMENT {IN, OUT, CONFLICT}
    long size();
    long readIndex();
    void readIndex(long index);
    CommandLogEntry read();
    void append(CommandLogEntry commandLogEntry);
    void truncateIncluding(long index);
    LogEntry lastLogEntry();
    CONTAINMENT contains(LogEntry logEntry);
}
