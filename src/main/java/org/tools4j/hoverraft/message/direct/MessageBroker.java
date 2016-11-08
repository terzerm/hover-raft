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
package org.tools4j.hoverraft.message.direct;

import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.config.ServerConfig;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.MessageHandler;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.transport.Pollers;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Polls a message from each serverContext and dispatches it to the appropriate method of a
 * {@link MessageHandler}.
 */
public final class MessageBroker {

    private final ServerContext serverContext;
    private final Receiver<?>[] serverReceivers;

    public MessageBroker(final ServerContext serverContext) {
        this.serverContext = Objects.requireNonNull(serverContext);
        this.serverReceivers = serverSubscriptions(serverContext);
    }

    private static Receiver<?>[] serverSubscriptions(final ServerContext serverContext) {
        final ConsensusConfig consensusConfig = serverContext.consensusConfig();
        final int servers = consensusConfig.serverCount();
        final Receiver<?>[] receivers= new Receiver<?>[servers - 1];
        int index = 0;
        for (int i = 0; i < servers - 1; i++) {
            final ServerConfig serverConfig = consensusConfig.serverConfig(i);
            if (serverConfig.id() != serverContext.serverConfig().id()) {
                receivers[i] = serverContext.connections().serverReceiver(serverConfig.id());
                index++;
            }
        }
        if (index < servers - 1) {
            throw new IllegalStateException("invalid serverContext ID serverConfig: expected " + servers + " unique IDs but found " + (index + 1));
        }
        return receivers;
    }

    public Receiver.Poller roundRobinPoller(final Consumer<Message> messageHandler) {
        return Pollers.roundRobinPoller(messageHandler, serverReceivers);
    }

}
