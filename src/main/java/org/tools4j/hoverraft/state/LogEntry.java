package org.tools4j.hoverraft.state;

public interface LogEntry {
    int term();
    LogEntry term(int term);

    long index();
    LogEntry index(long index);
}
