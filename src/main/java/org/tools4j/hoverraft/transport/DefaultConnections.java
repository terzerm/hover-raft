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
package org.tools4j.hoverraft.transport;

import org.tools4j.hoverraft.message.Message;

import java.util.List;
import java.util.Objects;

public class DefaultConnections<M extends Message> implements Connections<M> {

    private final Receiver<M>[] sourceReceivers;
    private final Receiver<M>[] serverReceivers;
    private final Sender<M> serverMulticastSender;

    public DefaultConnections(final List<Receiver<? extends M>> sourceReceivers,
                              final List<Receiver<? extends M>> serverReceivers,
                              final Sender<? super M> serverMulticastSender) {
        Objects.requireNonNull(serverMulticastSender);
        this.sourceReceivers = receivers(sourceReceivers);
        this.serverReceivers = receivers(serverReceivers);
        this.serverMulticastSender = (m) -> serverMulticastSender.offer(m);
    }

    //safely wraps Receiver<? super M> to Receiver<M>
    private Receiver<M>[] receivers(final List<Receiver<? extends M>> receivers) {
        final Receiver<M>[] r = (Receiver<M>[])new Receiver<?>[receivers.size()];
        for (int i = 0; i < r.length; i++) {
            final int index = i;
            r[index] = (c) -> receivers.get(index).poller(c);
        }
        return r;
    }

    @Override
    public Receiver<M> sourceReceiver(int sourceId) {
        return sourceReceivers[sourceId];
    }

    @Override
    public Receiver<M> serverReceiver(int serverId) {
        return serverReceivers[serverId];
    }

    @Override
    public Sender<M> serverSender(int serverId) {
        return serverMulticastSender;
    }

    @Override
    public Sender<M> serverMulticastSender() {
        return serverMulticastSender;
    }
}
