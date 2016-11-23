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
package org.tools4j.hoverraft.command.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCommandLog implements CommandLog {
    private final List<CommandLogEntry> commandLogEntries = new ArrayList<>();
    private final AtomicInteger readIndex = new AtomicInteger(0);

    private final LogEntry lastEntry = new LogEntry() {
        @Override
        public int term() {
            readIndex(index());
            return readTerm();
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
    public int readTerm() {
        return read(null).term();
    }

    @Override
    public synchronized CommandLogEntry read(final CommandLogEntry commandLogEntry) {
        if (readIndex.get() < commandLogEntries.size()) {
            return commandLogEntries.get(readIndex.getAndIncrement());
        }
        throw new IllegalStateException("Read index " + readIndex + " has reached end of message log with size " + commandLogEntries.size());
    }

    @Override
    public synchronized void append(final CommandLogEntry commandLogEntry) {
        commandLogEntries.add(Objects.requireNonNull(commandLogEntry));
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
