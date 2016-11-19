package org.tools4j.hoverraft.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCommandLog implements CommandLog {
    private final List<CommandLogEntry> commandLogEntries = new ArrayList<>();
    private final AtomicInteger readIndex = new AtomicInteger(0);


    @Override
    public long size() {
        return commandLogEntries.size();
    }

    @Override
    public long readIndex() {
        return readIndex.get();
    }

    @Override
    public synchronized void readIndex(final long index) {
        if (index < commandLogEntries.size()) {
            readIndex.set((int)index);
        } else {
            throw new IllegalArgumentException("Invalid read index " + index + " for log entry with size " + commandLogEntries.size());
        }
    }

    @Override
    public synchronized CommandLogEntry read() {
        if (readIndex.get() < commandLogEntries.size()) {
            return commandLogEntries.get(readIndex.getAndIncrement());
        }
        throw new IllegalArgumentException("Read index " + readIndex + " has reached end of message log with size " + commandLogEntries.size());
    }

    @Override
    public synchronized void append(final CommandLogEntry commandLogEntry) {
        commandLogEntries.add(Objects.requireNonNull(commandLogEntry));
    }


    @Override
    public synchronized void truncate(long index) {
        if (index >= commandLogEntries.size()) {
            throw new IllegalArgumentException("Truncate index " + index + " must be less than the size " + commandLogEntries.size());
        }
        for (long idx = index(); idx >= index; idx--) {
            commandLogEntries.remove((int)idx);
        }
        if (readIndex() >= index) {
            readIndex(index - 1);
        }
    }

    @Override
    public CONTAINMENT contains(LogEntry logEntry) {
        if (logEntry.index() > index()) {
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

    @Override
    public int compareTo(LogEntry logEntry) {
        int termCompare = Integer.compare(term(), logEntry.term());
        if (termCompare == 0) {
            return Long.compare(index(), logEntry.index());
        } else {
            return termCompare;
        }
    }

    @Override
    public int term() {
        readIndex(index());
        return read().term();
    }

    @Override
    public long index() {
        return commandLogEntries.size() - 1;
    }

    //FIXme apparently setters for term and index should not be in the LogEntry
    @Override
    public LogEntry term(int term) {
        throw new UnsupportedOperationException("This is shit! I can't implement it. Should not have these methods in LogEntry");
    }

    @Override
    public LogEntry index(long index) {
        throw new UnsupportedOperationException("This is shit! I can't implement it. Should not have these methods in LogEntry");
    }
}
