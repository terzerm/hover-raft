/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 hover-raft (tools4j), Marco Terzer
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

import java.util.Comparator;

public interface CommandKey extends Comparable<CommandKey> {
    Comparator<CommandKey> COMPARATOR = (k1, k2) -> {
        final int sourceCompare = Integer.compare(k1.sourceId(), k2.sourceId());
        return sourceCompare == 0 ? Long.compare(k1.commandIndex(), k2.commandIndex()) : sourceCompare;
    };

    int sourceId();
    CommandKey sourceId(int sourceId);

    long commandIndex();
    CommandKey commandIndex(long commandIndex);

    @Override
    default int compareTo(final CommandKey other) {
        return COMPARATOR.compare(this, other);
    }

    default boolean containedIn(final CommandLog commandLog) {
        return commandLog.contains(this);
    }
}
