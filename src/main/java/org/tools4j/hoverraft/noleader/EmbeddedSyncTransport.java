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
package org.tools4j.hoverraft.noleader;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EmbeddedSyncTransport implements Transport {

    private final Collection<Consumer<? super Message>> messageConsumers = new ConcurrentLinkedQueue<>();
    private final Collection<Consumer<? super Envelope>> envelopeConsumers = new ConcurrentLinkedQueue<>();

    @Override
    public void addSourceListener(final Consumer<? super Message> messageConsumer) {
        messageConsumers.add(messageConsumer);
    }

    @Override
    public void addServerListener(final Consumer<? super Envelope> envelopeConsumer) {
        envelopeConsumers.add(envelopeConsumer);
    }

    @Override
    public void notifyServers(final Envelope envelope) {
        for (final Consumer<? super Envelope> envelopeConsumer : envelopeConsumers) {
            envelopeConsumer.accept(envelope);
        }
    }

    @Override
    public void receiveInput(final Message message) {
        for (final Consumer<? super Message> messageConsumer : messageConsumers) {
            messageConsumer.accept(message);
        }
    }

    @Override
    public void sendOutput(final int serverId, final long sequenceNo, final Message message) {
        System.out.println("[" + serverId + "] " + sequenceNo + ":\t" + message);
    }
}
