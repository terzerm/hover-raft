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
package org.tools4j.hoverraft.server;

import org.tools4j.hoverraft.command.Command;
import org.tools4j.hoverraft.command.machine.StateMachine;
import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.config.ServerConfig;
import org.tools4j.hoverraft.direct.DirectFactory;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.state.HoverRaftMachine;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.VolatileState;
import org.tools4j.hoverraft.timer.Timer;
import org.tools4j.hoverraft.timer.TimerEvent;
import org.tools4j.hoverraft.transport.Connections;
import org.tools4j.hoverraft.transport.ResendStrategy;

import java.util.Objects;

public final class Server implements ServerContext {

    private final ServerConfig serverConfig;
    private final ConsensusConfig consensusConfig;
    private final HoverRaftMachine hoverRaftMachine;
    private final StateMachine stateMachine;
    private final Connections<Message> connections;
    private final DirectFactory directFactory;
    private final RoundRobinMessagePoller<Message> serverMessagePoller;
    private final RoundRobinMessagePoller<Command> sourceMessagePoller;
    private final Timer timer;

    public Server(final int serverId,
                  final ConsensusConfig consensusConfig,
                  final PersistentState persistentState,
                  final VolatileState volatileState,
                  final StateMachine stateMachine,
                  final Connections<Message> connections,
                  final DirectFactory directFactory) {
        this.serverConfig = Objects.requireNonNull(consensusConfig.serverConfigByIdOrNull(serverId), "No server serverConfig found for ID " + serverId);
        this.consensusConfig = Objects.requireNonNull(consensusConfig);
        this.hoverRaftMachine = new HoverRaftMachine(Objects.requireNonNull(persistentState),
                Objects.requireNonNull(volatileState));
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.connections = Objects.requireNonNull(connections);
        this.directFactory = Objects.requireNonNull(directFactory);
        this.serverMessagePoller = RoundRobinMessagePoller.forServerMessages(this, this::handleMessage);
        this.sourceMessagePoller = RoundRobinMessagePoller.forSourceMessages(this, this::handleCommand);
        this.timer = new Timer();
    }

    @Override
    public ServerConfig serverConfig() {
        return serverConfig;
    }

    @Override
    public ConsensusConfig consensusConfig() {
        return consensusConfig;
    }

    @Override
    public Connections<Message> connections() {
        return connections;
    }

    @Override
    public DirectFactory directFactory() {
        return directFactory;
    }

    @Override
    public StateMachine stateMachine() {
        return stateMachine;
    }

    @Override
    public void perform() {
        checkTimeoutElapsed();
        serverMessagePoller.pollNextMessage();
        sourceMessagePoller.pollNextMessage();
    }

    private void handleCommand(final Command command) {
        hoverRaftMachine.onEvent(this, command);
    }

    private void handleMessage(final Message message) {
        hoverRaftMachine.onEvent(this, message);
    }

    private void checkTimeoutElapsed() {
        if (timer.hasTimeoutElapsed()) {
            hoverRaftMachine.onEvent(this, TimerEvent.TIMEOUT);
        }
    }

    @Override
    public Timer timer() {
        return timer;
    }

    @Override
    public ResendStrategy resendStrategy() {
        return ResendStrategy.NOOP;//FIXME use better resend strategy
    }

}
