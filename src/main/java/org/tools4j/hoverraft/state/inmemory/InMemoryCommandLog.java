package org.tools4j.hoverraft.state.inmemory;

import org.jetbrains.annotations.NotNull;
import org.tools4j.hoverraft.state.CommandLog;
import org.tools4j.hoverraft.state.CommandLogEntry;
import org.tools4j.hoverraft.state.LogEntry;
import org.tools4j.hoverraft.state.LogEntryComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCommandLog implements CommandLog {
    private static final Comparator<LogEntry> LOG_ENTRY_COMPARATOR = new LogEntryComparator();

    private final List<CommandLogEntry> commandLogEntries = new ArrayList<>();
    private final AtomicInteger readIndex = new AtomicInteger(0);

    private final LogEntry lastEntry = new LogEntry() {
        @Override
        public int term() {
            readIndex(index());
            return read().term();
        }

        @Override
        public long index() {
            return commandLogEntries.size() - 1;
        }


        @Override
        public LogEntry term(final int term) {
            throw new UnsupportedOperationException("term update is not allowed");
        }

        @Override
        public LogEntry index(final long index) {
            throw new UnsupportedOperationException("index update is not allowed");
        }

        @Override
        public int compareTo(@NotNull final LogEntry logEntry) {
            return LOG_ENTRY_COMPARATOR.compare(this, Objects.requireNonNull(logEntry));
        }
    };

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
        throw new IllegalStateException("Read index " + readIndex + " has reached end of message log with size " + commandLogEntries.size());
    }

    @Override
    public synchronized void append(final CommandLogEntry commandLogEntry) {
        final long newIndex = lastEntry.index() + 1;
        Objects.requireNonNull(commandLogEntry).index(newIndex);
        commandLogEntries.add(commandLogEntry);
     }


    @Override
    public synchronized void truncateIncluding(long index) {
        if (index >= commandLogEntries.size()) {
            throw new IllegalArgumentException("Truncate index " + index + " must be less than the size " + commandLogEntries.size());
        }
        for (long idx = lastEntry.index(); idx >= index; idx--) {
            commandLogEntries.remove((int)idx);
        }
        if (readIndex() >= index) {
            readIndex(index - 1);
        }
    }

    @Override
    public LogEntry lastEntry() {
        return lastEntry;
    }

}
