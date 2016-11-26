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
package org.tools4j.hoverraft.command;

import org.tools4j.hoverraft.direct.AllocatingDirectFactory;
import org.tools4j.hoverraft.direct.DirectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCommandLog implements CommandLog {

    private final DirectFactory directFactory = new AllocatingDirectFactory();
    private final List<LogEntry> entries = new ArrayList<>();
    private final AtomicInteger readIndex = new AtomicInteger(0);
    private final DirectLogEntry lastEntry = new DirectLogEntry();

    @Override
    public long size() {
        return entries.size();
    }

    @Override
    public long readIndex() {
        return readIndex.get();
    }

    @Override
    public synchronized void readIndex(final long index) {
        if (index < entries.size()) {
            readIndex.set((int)index);
        } else {
            throw new IllegalArgumentException("Invalid read index " + index + " for log entry with size " + entries.size());
        }
    }

    @Override
    public synchronized int readTerm() {
        return read(null).logKey().term();
    }

    @Override
    public synchronized LogEntry read(final DirectFactory directFactory) {
        return clone(read());
    }

    @Override
    public void readTo(final LogEntry logEntry) {
        final LogEntry readLogEntry = read();
        logEntry.writeBufferOrNull().putBytes(0, readLogEntry.readBufferOrNull(), readLogEntry.offset(), readLogEntry.byteLength());

    }

    private LogEntry read() {
        if (readIndex.get() < entries.size()) {
            return entries.get(readIndex.getAndIncrement());
        }
        throw new IllegalStateException("Read index " + readIndex + " has reached end of message log with size " + entries.size());
    }

    @Override
    public synchronized void append(final LogEntry logEntry) {
        entries.add(clone(logEntry));
    }


    @Override
    public synchronized void truncateIncluding(long index) {
        if (index >= entries.size()) {
            throw new IllegalArgumentException("Truncate index " + index + " must be less than the size " + entries.size());
        }
        for (long idx = lastKey().index(); idx >= index; idx--) {
            entries.remove((int)idx);
        }
        if (readIndex() >= index) {
            readIndex(index - 1);
        }
    }

    @Override
    public LogKey lastKey() {
        readIndex(entries.size() - 1);
        readTo(lastEntry);
        return lastEntry.logKey();
    }

    private LogEntry clone(final LogEntry e) {
        final LogEntry clone = directFactory.logEntry();
        clone.writeBufferOrNull().putBytes(0, e.readBufferOrNull(), e.offset(), e.byteLength());
        return clone;
    }

}
