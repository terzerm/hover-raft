/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 hoover-raft (tools4j), Marco Terzer
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
package org.tools4j.hooverraft.server;

import org.tools4j.hooverraft.config.ServerConfig;
import org.tools4j.hooverraft.config.ThreadingMode;
import org.tools4j.hooverraft.io.Connections;
import org.tools4j.hooverraft.ipc.MessageBroker;
import org.tools4j.hooverraft.ipc.MessageFactory;
import org.tools4j.hooverraft.ipc.MessageHandler;
import org.tools4j.hooverraft.state.ElectionTimer;
import org.tools4j.hooverraft.state.Role;
import org.tools4j.hooverraft.state.ServerState;
import org.tools4j.hooverraft.state.VolatileState;

import java.util.Objects;

public final class Server {

    private final ServerConfig serverConfig;
    private final ServerState serverState;
    private final Connections connections;
    private final MessageBroker messageBroker = new MessageBroker(this);
    private final MessageFactory messageFactory = new MessageFactory();

    public Server(final ServerConfig serverConfig,
                  final ServerState serverState,
                  final Connections connections) {
        this.serverConfig = Objects.requireNonNull(serverConfig);
        this.serverState = Objects.requireNonNull(serverState);
        this.connections = Objects.requireNonNull(connections);
    }

    public ServerConfig config() {
        return serverConfig;
    }

    public ServerState state() {
        return serverState;
    }

    public Connections connections() {
        return connections;
    }

    public MessageFactory messageFactory() {
        return messageFactory;
    }

    public int currentTerm() {
        return state().persistentState().currentTerm();
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
        if (serverConfig.consensusConfig().threadingPolicy() == ThreadingMode.SHARED) {
            //FIXME impl
        }
    }

    private void checkElectionTimeout() {
        final ServerState state = serverState;
        final VolatileState vstate = state.volatileState();
        final ElectionTimer timer = vstate.electionState().electionTimer();
        if (timer.hasTimeoutElapsed()) {
            state.persistentState().clearVotedForAndIncCurrentTerm();
            vstate.changeRoleTo(Role.CANDIDATE);
            timer.restart();
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

}
