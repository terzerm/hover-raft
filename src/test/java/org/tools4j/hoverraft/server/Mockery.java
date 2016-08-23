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
import org.tools4j.hoverraft.config.ServerConfig;
import org.tools4j.hoverraft.config.SourceConfig;
import org.tools4j.hoverraft.config.ThreadingMode;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.MessageFactory;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.ServerState;
import org.tools4j.hoverraft.state.VolatileState;
import org.tools4j.hoverraft.transport.Connections;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Mockery {

    public static final int SERVER_ID = 1;
    public static final long MIN_ELECTION_TIMEOUT_MILLIS = 50;
    public static final long MAX_ELECTION_TIMEOUT_MILLIS = 100;

    public static Server<DirectMessage> direct(final int servers) {
        return direct(servers, 0, 0);
    }

    public static Server<DirectMessage> direct(final int servers, final int sources, final int connections) {
        final ConsensusConfig consensusConfig = consensusConfig(servers, sources);
        final Connections<DirectMessage> connects = connections(servers, sources);
        final MessageFactory<DirectMessage> messageFactory = messageFactory();
        final ServerState serverState = serverState(consensusConfig);
        return new Server<>(SERVER_ID, consensusConfig, serverState, connects, messageFactory);
    }

    public static ConsensusConfig consensusConfig(final int servers, final int sources) {
        final ConsensusConfig consensusConfig = mock(ConsensusConfig.class);
        when(consensusConfig.minElectionTimeoutMillis()).thenReturn(MIN_ELECTION_TIMEOUT_MILLIS);
        when(consensusConfig.maxElectionTimeoutMillis()).thenReturn(MAX_ELECTION_TIMEOUT_MILLIS);
        when(consensusConfig.ipcMulticastChannel()).thenReturn(Optional.empty());
        when(consensusConfig.threadingMode()).thenReturn(ThreadingMode.SHARED);

        //server configs
        final ServerConfig[] serverConfigs = serverConfigs(servers);
        for (int i = 0; i < servers; i++) {
            when(consensusConfig.serverConfig(i)).thenReturn(serverConfigs[i]);
            when(consensusConfig.serverConfigById(serverConfigs[i].id())).thenReturn(serverConfigs[i]);
        }
        when(consensusConfig.serverCount()).thenReturn(servers);

        //source configs
        final SourceConfig[] sourceConfigs = sourceConfigs(sources);
        for (int i = 0; i < sources; i++) {
            when(consensusConfig.sourceConfig(i)).thenReturn(sourceConfigs[i]);
            when(consensusConfig.sourceConfigById(serverConfigs[i].id())).thenReturn(sourceConfigs[i]);
        }
        when(consensusConfig.sourceCount()).thenReturn(sources);
        return consensusConfig;
    }

    private static ServerConfig[] serverConfigs(final int servers) {
        final ServerConfig[] configs = new ServerConfig[servers];
        for (int i = 0; i < servers; i++) {
            configs[i] = mock(ServerConfig.class);
            when(configs[i].id()).thenReturn(i + 1);
            when(configs[i].channel()).thenReturn("channel-" + (i + 1));
        }
        return configs;
    }

    private static SourceConfig[] sourceConfigs(final int sources) {
        final SourceConfig[] configs = new SourceConfig[sources];
        for (int i = 0; i < sources; i++) {
            configs[i] = mock(SourceConfig.class);
            when(configs[i].id()).thenReturn(i + 1);
            when(configs[i].channel()).thenReturn("channel-" + (i + 1));
        }
        return configs;
    }

    public static <M> Connections<M> connections(final int servers, final int sources) {
        final Connections<M> connects = mock(Connections.class);
        //when
        return connects;
    }

    public static <M extends Message<M>> MessageFactory<M> messageFactory() {
        return mock(MessageFactory.class);
    }

    public static ServerState serverState(final ConsensusConfig consensusConfig) {
        final PersistentState persistentState = persistentState();
        return new ServerState(persistentState, new VolatileState(SERVER_ID, consensusConfig));
    }

    public static PersistentState persistentState() {
        final PersistentState persistentState = mock(PersistentState.class);
        when(persistentState.currentTerm()).thenReturn(1);
        when(persistentState.lastLogTerm()).thenReturn(0);
        when(persistentState.lastLogIndex()).thenReturn(-1L);
        when(persistentState.votedFor()).thenReturn(-1);
        return persistentState;
    }
}