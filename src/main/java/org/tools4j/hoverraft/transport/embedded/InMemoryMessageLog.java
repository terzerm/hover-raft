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
package org.tools4j.hoverraft.transport.embedded;

import org.tools4j.hoverraft.message.simple.SimpleMessage;
import org.tools4j.hoverraft.transport.MessageLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryMessageLog implements MessageLog<SimpleMessage> {

    private final List<SimpleMessage> messages = new ArrayList<SimpleMessage>();
    private final AtomicInteger readIndex = new AtomicInteger(0);

    @Override
    public synchronized long size() {
        return messages.size();
    }

    @Override
    public synchronized void size(final long size) {
        if (size == 0) {
            messages.clear();
            return;
        }
        while (size < messages.size()) {
            messages.remove(messages.size() - 1);
        }
        if (size > messages.size()) {
            throw new IllegalArgumentException("New size " + size + " cannot be larger than current message log size " + messages.size());
        }
    }

    @Override
    public long readIndex() {
        return readIndex.get();
    }

    @Override
    public synchronized void readIndex(final long index) {
        if (index < messages.size()) {
            readIndex.set((int)index);
        } else {
            throw new IllegalArgumentException("Invalid read index " + index + " for message log with size " + messages.size());
        }
    }

    @Override
    public synchronized SimpleMessage read() {
        if (readIndex.get() < messages.size()) {
            return messages.get(readIndex.getAndIncrement());
        }
        throw new IllegalArgumentException("Read index " + readIndex + " has reached end of message log with size " + messages.size());
    }

    @Override
    public synchronized void append(final SimpleMessage message) {
        messages.add(Objects.requireNonNull(message));
    }
}
