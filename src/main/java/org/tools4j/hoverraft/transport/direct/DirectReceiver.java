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
package org.tools4j.hoverraft.transport.direct;

import org.agrona.DirectBuffer;
import org.tools4j.hoverraft.io.Subscription;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;
import java.util.function.Consumer;

public class DirectReceiver implements Receiver<DirectMessage, DirectBuffer> {

    private final DirectMessageFactory directMessageFactory;
    private final Subscription subscription;

    public DirectReceiver(final DirectMessageFactory directMessageFactory, final Subscription subscription) {
        this.directMessageFactory = Objects.requireNonNull(directMessageFactory);
        this.subscription = Objects.requireNonNull(subscription);
    }

    @Override
    public int poll(final Consumer<? super DirectMessage> messageMandler, final int offset, final int limit) {
        return subscription.poll((buf, off, len, hdr) -> {
            final int type = buf.getInt(offset);
            if (type >= 0 && type <= MessageType.maxOrdinal()) {
                final MessageType messageType = MessageType.valueByOrdinal(type);
                final DirectMessage message = directMessageFactory.createByType(messageType);
                message.wrap(buf, offset + 4);
                messageMandler.accept(message);
            } else {
                throw new RuntimeException("Invalid message type: " + type);
            }
        }, limit);
    }
}
