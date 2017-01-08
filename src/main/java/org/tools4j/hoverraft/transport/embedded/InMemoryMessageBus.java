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
package org.tools4j.hoverraft.transport.embedded;

import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.transport.Receiver;
import org.tools4j.hoverraft.transport.Sender;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * In-process message bus for simple messages.
 */
public class InMemoryMessageBus<M extends Message> implements Sender<M>,Receiver<M> {

    private final Deque<M> queue = new ConcurrentLinkedDeque<M>();

    @Override
    public long offer(final M message) {
        return queue.offerLast(message) ? 1 : 0;
    }

    @Override
    public int poll(final Consumer<? super M> messageMandler, final int limit) {
        int cnt = 0;
        while (cnt < limit) {
            M msg = queue.pollFirst();
            if (msg == null) {
                return cnt;
            }
            messageMandler.accept(msg);
            cnt++;
        }
        return cnt;
    }

}
