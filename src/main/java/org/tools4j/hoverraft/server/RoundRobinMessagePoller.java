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
package org.tools4j.hoverraft.server;

import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.transport.Receiver;
import org.tools4j.hoverraft.transport.Receivers;

import java.util.Objects;
import java.util.function.Consumer;

public class RoundRobinMessagePoller<M extends Message> {

    private final Receiver<M> roundRobinReceiver;
    private final Consumer<? super M> messageHandler;

    private RoundRobinMessagePoller(final Receiver<M> roundRobinReceiver,
                                    final Consumer<? super M> messageHandler) {
        this.roundRobinReceiver = Objects.requireNonNull(roundRobinReceiver);
        this.messageHandler = Objects.requireNonNull(messageHandler);
    }

    public static RoundRobinMessagePoller<Message> forServerMessages(final ServerContext serverContext,
                                                                     final Consumer<? super Message> messageHandler) {
        final ConsensusConfig config = serverContext.consensusConfig();
        final Receiver<?>[] receivers = new Receiver<?>[config.serverCount() - 1];
        int index = 0;
        for (int i = 0; i < config.serverCount(); i++) {
            final int id = config.serverConfig(i).id();
            if (!Objects.equals(serverContext.id(), id)) {
                if (index < config.serverCount()) {
                    receivers[index] = serverContext.connections().serverReceiver(id);
                    index++;
                } else {
                    throw new IllegalArgumentException("Bad config: Server ID '" +  serverContext.id() + "' not found in list of servers");
                }
            }
        }
        if (index < config.serverCount() - 1) {
            throw new IllegalArgumentException("Bad config: Server ID '" +  serverContext.id() + "' occurs more than once in list of servers");
        }
        return new RoundRobinMessagePoller<>(Receivers.roundRobinReceiver(receivers), messageHandler);
    }

    public static RoundRobinMessagePoller<CommandMessage> forSourceMessages(final ServerContext serverContext,
                                                                            final Consumer<? super CommandMessage> messageHandler) {
        final ConsensusConfig config = serverContext.consensusConfig();
        final Receiver<CommandMessage>[] receivers = (Receiver<CommandMessage>[])new Receiver<?>[config.sourceCount()];
        for (int i = 0; i < config.sourceCount(); i++) {
            final int id = config.sourceConfig(i).id();
            receivers[i] = serverContext.connections().sourceReceiver(id);
        }
        return new RoundRobinMessagePoller<>(Receivers.roundRobinReceiver(receivers), messageHandler);
    }

    public void pollNextMessage() {
        roundRobinReceiver.poll(messageHandler, 1);
    }

}
