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
package org.tools4j.hoverraft.transport.aeron;

import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.tools4j.hoverraft.message.direct.AbstractDirectMessage;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;
import java.util.function.Consumer;

public class AeronReceiver implements Receiver<AbstractDirectMessage> {

    private final DirectMessageFactory directMessageFactory;
    private final Subscription subscription;
    private final AeronHandler aeronHandler = new AeronHandler();

    public AeronReceiver(final DirectMessageFactory directMessageFactory, final Subscription subscription) {
        this.directMessageFactory = Objects.requireNonNull(directMessageFactory);
        this.subscription = Objects.requireNonNull(subscription);
    }

    @Override
    public int poll(final Consumer<? super AbstractDirectMessage> messageMandler, final int limit) {
        final FragmentHandler fragmentHandler = (buf, off, len, hdr) -> {
            final AbstractDirectMessage message = directMessageFactory.wrapForReading(buf, off);
            messageMandler.accept(message);
        };
        for (int i = 0; i < limit; i++) {
            if (0 < subscription.poll(aeronHandler, 1)) {
                messageMandler.accept(aeronHandler.message.get());
                aeronHandler.message.set(null);
            } else {
                return i;
            }
        }
        return limit;
    }

    private class AeronHandler implements FragmentHandler {
        private final ThreadLocal<AbstractDirectMessage> message = ThreadLocal.withInitial(() -> null);

        @Override
        public void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
            final AbstractDirectMessage message = directMessageFactory.wrapForReading(buffer, offset);
            this.message.set(message);
        }
    }
}
