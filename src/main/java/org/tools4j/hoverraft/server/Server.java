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
import org.tools4j.hoverraft.config.ThreadingMode;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.MessageFactory;
import org.tools4j.hoverraft.message.MessageHandler;
import org.tools4j.hoverraft.message.direct.MessageBroker;
import org.tools4j.hoverraft.state.ElectionTimer;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.ServerState;
import org.tools4j.hoverraft.state.VolatileState;
import org.tools4j.hoverraft.transport.Connections;
import org.tools4j.hoverraft.transport.MessageLog;
import org.tools4j.hoverraft.transport.ResendStrategy;
import org.tools4j.hoverraft.util.Clock;

import java.util.Objects;

public final class Server {

    private final ServerConfig serverConfig;
    private final ConsensusConfig consensusConfig;
    private final ServerState serverState;
    private final MessageLog<?> messageLog;
    private final Connections<Message> connections;
    private final MessageBroker messageBroker;
    private final MessageFactory messageFactory;

    public Server(final int serverId,
                  final ConsensusConfig consensusConfig,
                  final ServerState serverState,
                  final MessageLog<?> messageLog,
                  final Connections<Message> connections,
                  final MessageFactory messageFactory) {
        this.serverConfig = Objects.requireNonNull(consensusConfig.serverConfigById(serverId), "No server serverConfig found for ID " + serverId);
        this.consensusConfig = Objects.requireNonNull(consensusConfig);
        this.serverState = Objects.requireNonNull(serverState);
        this.messageLog = Objects.requireNonNull(messageLog);
        this.connections = Objects.requireNonNull(connections);
        this.messageFactory = Objects.requireNonNull(messageFactory);
        this.messageBroker = new MessageBroker(this);
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

    public MessageFactory messageFactory() {
        return messageFactory;
    }

    public int currentTerm() {
        return state().persistentState().currentTerm();
    }

    public int id() {
        return serverConfig().id();
    }

    public void perform() {
        pollNextInputMessage();
        invokeStateMachineWithCommittedLogEntry();
        checkElectionTimeout();
        performRoleSpecificActivity();
    }

    public void pollEachServer(final MessageHandler messageHandler, final int messageLimitPerServer) {
        messageBroker.pollEachServer(messageHandler, messageLimitPerServer);
    }

    private void pollNextInputMessage() {
        if (consensusConfig.threadingMode() == ThreadingMode.SHARED) {
            //FIXME impl
        }
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
        if (vstate.commitIndex() > vstate.lastApplied()) {
            //FIXME
//            stateMachine.applyCommand(server, vstate.lastApplied());
            System.out.println("STATE MACHINE APPLIED: " + vstate.lastApplied());
        }
    }

    private void performRoleSpecificActivity() {
        serverState.volatileState().role().perform(this);
    }

    public ResendStrategy resendStrategy() {
        return ResendStrategy.NOOP;//FIXME use better resend strategy
    }
}
