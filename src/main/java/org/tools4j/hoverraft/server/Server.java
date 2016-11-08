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
import org.tools4j.hoverraft.machine.StateMachine;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.message.direct.MessageBroker;
import org.tools4j.hoverraft.state.ElectionTimer;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.ServerState;
import org.tools4j.hoverraft.state.VolatileState;
import org.tools4j.hoverraft.transport.Connections;
import org.tools4j.hoverraft.transport.MessageLog;
import org.tools4j.hoverraft.transport.Receiver;
import org.tools4j.hoverraft.transport.ResendStrategy;
import org.tools4j.hoverraft.util.Clock;

import java.util.EnumMap;
import java.util.Objects;

public final class Server implements ServerContext {

    private final ServerConfig serverConfig;
    private final ConsensusConfig consensusConfig;
    private final ServerState serverState;
    private final MessageLog<CommandMessage> messageLog;
    private final StateMachine stateMachine;
    private final Connections<Message> connections;
    private final MessageBroker messageBroker;
    private final DirectMessageFactory messageFactory;
    private final EnumMap<Role, Receiver.Poller> rolePollers;

    public Server(final int serverId,
                  final ConsensusConfig consensusConfig,
                  final ServerState serverState,
                  final MessageLog<CommandMessage> messageLog,
                  final StateMachine stateMachine,
                  final Connections<Message> connections,
                  final DirectMessageFactory messageFactory) {
        this.serverConfig = Objects.requireNonNull(consensusConfig.serverConfigByIdOrNull(serverId), "No server serverConfig found for ID " + serverId);
        this.consensusConfig = Objects.requireNonNull(consensusConfig);
        this.serverState = Objects.requireNonNull(serverState);
        this.messageLog = Objects.requireNonNull(messageLog);
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.connections = Objects.requireNonNull(connections);
        this.messageFactory = Objects.requireNonNull(messageFactory);
        this.messageBroker = new MessageBroker(this);
        this.rolePollers = initRolePollers();
    }

    public ServerConfig serverConfig() {
        return serverConfig;
    }

    public ConsensusConfig consensusConfig() {
        return consensusConfig;
    }

    public ServerState state() {
        return serverState;
    }

    public Connections<Message> connections() {
        return connections;
    }

    public DirectMessageFactory messageFactory() {
        return messageFactory;
    }

    public void perform() {
        checkElectionTimeout();
        pollNextServerMessage();
        invokeStateMachineWithCommittedLogEntry();
    }

    private void checkElectionTimeout() {
        final ServerState state = serverState;
        final VolatileState vstate = state.volatileState();
        final ElectionTimer timer = vstate.electionState().electionTimer();
        if (timer.hasTimeoutElapsed(Clock.DEFAULT)) {
            state.persistentState().clearVotedForAndIncCurrentTerm();
            vstate.changeRoleTo(Role.CANDIDATE);
            timer.restart(Clock.DEFAULT);
        }
    }

    private void invokeStateMachineWithCommittedLogEntry() {
        final VolatileState vstate = serverState.volatileState();
        long lastApplied = vstate.lastApplied();
        while (vstate.commitIndex() > lastApplied) {
            lastApplied++;
            messageLog.readIndex(lastApplied);
            final CommandMessage commandMsg = messageLog.read();
            stateMachine.onMessage(commandMsg);
            vstate.lastApplied(lastApplied);
        }
    }

    private void pollNextServerMessage() {
        rolePollers.get(serverState.volatileState().role()).poll(1);
    }

    public ResendStrategy resendStrategy() {
        return ResendStrategy.NOOP;//FIXME use better resend strategy
    }

    private EnumMap<Role, Receiver.Poller> initRolePollers() {
        final EnumMap<Role, Receiver.Poller> map = new EnumMap<>(Role.class);
        for (final Role role : Role.values()) {
            map.put(role, messageBroker.roundRobinPoller(role.toMessageHandler(this)));
        }
        return map;
    }
}
