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
import org.tools4j.hoverraft.transport.Receiver;
import org.tools4j.hoverraft.transport.RejectReason;
import org.tools4j.hoverraft.transport.Sender;

import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * In-process message bus for simple messages.
 */
public class SimpleMessageBus implements Sender<SimpleMessage>, Receiver<SimpleMessage> {

    private final Collection<BufferingPoller> pollers = new ConcurrentLinkedQueue<>();

    @Override
    public long offer(final SimpleMessage message) {
        int count = 0;
        for (final BufferingPoller poller : pollers) {
            if (poller.buffer.offer(message)) {
                count++;
            }
        }
        return count > 0 ? 0 : RejectReason.NOT_CONNECTED;
    }

    @Override
    public Poller poller(final Consumer<? super SimpleMessage> messageMandler) {
        final BufferingPoller poller = new BufferingPoller(messageMandler);
        pollers.add(poller);
        return poller;
    }

    private static final class BufferingPoller implements Poller {
        private final Consumer<? super SimpleMessage> messageHandler;
        private final Deque<SimpleMessage> buffer = new ConcurrentLinkedDeque<>();

        public BufferingPoller(final Consumer<? super SimpleMessage> messageHandler) {
            this.messageHandler = Objects.requireNonNull(messageHandler);
        }
        @Override
        public int poll(final int limit) {
            for (int i = 0; i < limit; i++) {
                final SimpleMessage msg = buffer.pollFirst();
                if (msg == null) {
                    return i;
                }
                messageHandler.accept(msg.clone());
            }
            return limit;
        }
    }
}
