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

import org.agrona.collections.Long2LongHashMap;
import org.tools4j.hoverraft.direct.AllocatingDirectFactory;
import org.tools4j.hoverraft.direct.DirectFactory;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCommandLog implements CommandLog {

    private final DirectFactory directFactory = new AllocatingDirectFactory();

    private final List<LogEntry> entries = new ArrayList<>();
    private final Long2LongHashMap maxCommandIndexBySourceId = new Long2LongHashMap(-1);

    @Override
    public synchronized long size() {
        return entries.size();
    }

    @Override
    public synchronized int readTerm(final long index) {
        return read(index).logKey().term();
    }

    @Override
    public synchronized void readKeyTo(final long index, final LogKey logKey) {
        final LogKey readKey = read(index).logKey();
        logKey.term(readKey.term()).index(readKey.index());
    }

    @Override
    public synchronized void readTo(final long index, final LogEntry target) {
        final LogEntry source = read(index);
        target.writeBufferOrNull()
                .putBytes(0, source.readBufferOrNull(), source.offset(), source.byteLength());
    }

    private LogEntry read(final long index) {
        if (index < entries.size()) {
            return entries.get((int)index);
        }
        throw new IllegalArgumentException("Index " + index + " is not in [0, " + (entries.size() - 1) + "]");
    }

    @Override
    public void append(final int term, final Command command) {
        final LogEntry logEntry = directFactory.logEntry();
        logEntry.logKey().term(term);
        logEntry.command()
                .writeBufferOrNull()
                .putBytes(0, command.readBufferOrNull(), command.offset(), command.byteLength());
        synchronized (this) {
            final long index = entries.size();
            logEntry.logKey().index(index);//TODO this is redundant, do we need this?
            entries.add(logEntry);
            maxCommandIndexBySourceId.put(command.commandKey().sourceId(), command.commandKey().commandIndex());
        }
    }


    @Override
    public synchronized void truncateIncluding(final long index) {
        for (int idx = entries.size() - 1; idx >= index; idx--) {
            final LogEntry removed = entries.remove(idx);
            maxCommandIndexBySourceId.remove(removed.command().commandKey().sourceId());
        }
    }

    @Override
    public synchronized int lastTerm() {
        return readTerm(entries.size() - 1);
    }

    @Override
    public synchronized void lastKeyTo(final LogKey target) {
        readKeyTo(entries.size() - 1, target);
    }

    @Override
    public synchronized boolean contains(final CommandKey commandKey) {
        final int sourceId = commandKey.sourceId();
        long maxCommandIndex = maxCommandIndexBySourceId.get(sourceId);
        if (maxCommandIndex < 0) {
            for (int idx = entries.size() - 1; idx >= 0; idx--) {
                final CommandKey ckey = entries.get(idx).command().commandKey();
                if (ckey.sourceId() == sourceId) {
                    maxCommandIndex = ckey.commandIndex();
                    maxCommandIndexBySourceId.put(sourceId, maxCommandIndex);
                    break;
                }
            }
        }
        return maxCommandIndex >= commandKey.commandIndex();
    }
}
