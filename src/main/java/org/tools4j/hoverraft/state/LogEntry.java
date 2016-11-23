package org.tools4j.hoverraft.state;

public interface LogEntry extends Comparable<LogEntry>{
    int term();
    LogEntry term(int term);

    long index();
    LogEntry index(long index);
}
