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

public class InMemoryCommandLog implements CommandLog {

    private final DirectFactory directFactory = new AllocatingDirectFactory();
    private final List<LogEntry> entries = new ArrayList<>();

    @Override
    public long size() {
        return entries.size();
    }

    @Override
    public synchronized int readTerm(final long position) {
        return read(position, null).logKey().term();
    }

    @Override
    public synchronized LogEntry read(final long position, final DirectFactory directFactory) {
        return clone(read(position));
    }

    @Override
    public void readTo(final long position, final LogEntry logEntry) {
        logEntry.copyFrom(read(position));
    }

    @Override
    public void readKeyTo(final long position, final LogKey logKey) {
        final LogKey readKey = read(position).logKey();
        logKey.term(readKey.term()).index(readKey.index());
    }

    private LogEntry read(final long position) {
        if (position < entries.size()) {
            return entries.get((int)position);
        }
        throw new IllegalArgumentException("Position " + position + " is out of bounds for size=" + entries.size());
    }

    @Override
    public synchronized void append(final LogEntry logEntry) {
        entries.add(clone(logEntry));
    }


    @Override
    public synchronized void truncateIncluding(final long index) {
        if (index >= entries.size()) {
            throw new IllegalArgumentException("Truncate index " + index + " must be less than the size " + entries.size());
        }
        for (long idx = lastIndex(); idx >= index; idx--) {
            entries.remove((int)idx);
        }
    }

    @Override
    public long lastIndex() {
        return read(entries.size() - 1).logKey().index();
    }

    @Override
    public void lastKeyTo(final LogKey logKey) {
        readKeyTo(entries.size() - 1, logKey);
    }

    @Override
    public int lastKeyCompareTo(final LogKey logKey) {
        return read(entries.size() - 1).logKey().compareTo(logKey);
    }

    private LogEntry clone(final LogEntry e) {
        final LogEntry clone = directFactory.logEntry();
        clone.writeBufferOrNull().putBytes(0, e.readBufferOrNull(), e.offset(), e.byteLength());
        return clone;
    }

}
