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

import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.config.ServerConfig;
import org.tools4j.hoverraft.message.MessageHandler;
import org.tools4j.hoverraft.server.Server;

import java.util.Objects;

/**
 * Polls a message from each server and dispatches it to the appropriate method of a
 * {@link MessageHandler}.
 */
public final class MessageBroker {

    private final Server server;
    private final Subscription[] serverSubscriptions;

    private int index;

    public MessageBroker(final Server server) {
        this.server = Objects.requireNonNull(server);
        this.serverSubscriptions = serverSubscriptions();
    }

    private Subscription[] serverSubscriptions() {
        final ConsensusConfig consensusConfig = server.consensusConfig();
        final int servers = consensusConfig.serverCount();
        final Subscription[] subscriptions = new Subscription[servers - 1];
        int index = 0;
        for (int i = 0; i < servers - 1; i++) {
            final ServerConfig serverConfig = consensusConfig.serverConfig(i);
            if (serverConfig.id() != server.serverConfig().id()) {
                subscriptions[i] = null;//FIXME: server.connections().serverReceiver(serverConfig.id());
                index++;
            }
        }
        if (index < servers - 1) {
            throw new IllegalStateException("invalid server ID serverConfig: expected " + servers + " unique IDs but found " + (index + 1));
        }
        return subscriptions;
    }

    public int pollNextMessage(final FragmentHandler fragmentHandler) {
        final int len = serverSubscriptions.length;
        int count = 0;
        for (int i = 0; i < len; i++) {
            final Subscription subscription = serverSubscriptions[index];
            count += subscription.poll(fragmentHandler, 1);
            index++;
            if (index >= len) {
                index = 0;
            }
        }
        return count;
    }

}
