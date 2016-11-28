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
import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.message.direct.DirectCommandKey;

import java.util.Objects;

/**
 * Lookup to determine whether a {@link CommandKey} is contained in a {@link CommandLog}.
 * Based on the observation that commands are appended with gap free strictly increasing indices per
 * source ID.
 */
public final class CommandKeyLookup {

    private final Long2LongHashMap maxCommandIndexBySourceId = new Long2LongHashMap(-1);
    private final DirectCommandKey commandKey = new DirectCommandKey();
    private final CommandLog commandLog;

    public CommandKeyLookup(final CommandLog commandLog) {
        this.commandLog = Objects.requireNonNull(commandLog);
        this.commandKey.wrap(new UnsafeBuffer(new byte[DirectCommandKey.BYTE_LENGTH]), 0);
    }

    public void append(final CommandKey commandKey) {
        maxCommandIndexBySourceId.put(commandKey.sourceId(), commandKey.commandIndex());
    }

    public void remove(final CommandKey commandKey) {
        maxCommandIndexBySourceId.remove(commandKey.sourceId());
    }

    public boolean contains(final CommandKey commandKey) {
        final int sourceId = commandKey.sourceId();
        long maxCommandIndex = maxCommandIndexBySourceId.get(sourceId);
        if (maxCommandIndex < 0) {
            for (long idx = commandLog.size() - 1; idx >= 0; idx--) {
                commandLog.readTo(idx, commandKey);
                if (commandKey.sourceId() == sourceId) {
                    maxCommandIndex = commandKey.commandIndex();
                    maxCommandIndexBySourceId.put(sourceId, maxCommandIndex);
                    break;
                }
            }
        }
        return maxCommandIndex >= commandKey.commandIndex();
    }

    public void clear() {
        maxCommandIndexBySourceId.clear();
    }

}
