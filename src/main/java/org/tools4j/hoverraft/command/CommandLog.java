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

public interface CommandLog {
    long size();
    default long lastIndex() {
        return size() - 1;
    }
    int readTerm(long index);
    void readTo(long index, LogKey target);
    void readTo(long index, CommandKey target);
    void readTo(long index, LogEntry target);

    void append(int term, Command command);
    void truncateIncluding(long index);

    default int lastTerm() {
        return readTerm(lastIndex());
    }

    default void lastKeyTo(final LogKey target) {
        readTo(lastIndex(), target);
    }

    default int lastKeyCompareTo(final LogKey logKey) {
        final int termCompare = Integer.compare(lastTerm(), logKey.term());
        return termCompare == 0 ? Long.compare(lastIndex(), logKey.index()) : termCompare;
    }

    boolean contains(CommandKey commandKey);
    default LogContainment contains(final LogKey logKey) {
        return LogContainment.containmentFor(logKey, this);
    }

}
