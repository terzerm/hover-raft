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
package org.tools4j.hoverraft.state;

import org.tools4j.hoverraft.machine.StateMachine;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.transport.MessageLog;
import org.tools4j.hoverraft.util.Clock;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Hover raft state machine. Not named state machine to avoid confusion with the application
 * {@link org.tools4j.hoverraft.machine.StateMachine}.
 */
public class HoverRaftMachine {

    private final Map<Role, State> stateMap;
    private final PersistentState persistentState;
    private final VolatileState volatileState;

    private State currentState;

    public HoverRaftMachine(final PersistentState persistentState, final VolatileState volatileState) {
        this.persistentState = Objects.requireNonNull(persistentState);
        this.volatileState = Objects.requireNonNull(volatileState);
        this.stateMap = initStateMap();
        currentState = initialState();
    }

    private Map<Role, State> initStateMap() {
        final Map<Role, State> map = new EnumMap<>(Role.class);
        for (final Role role : Role.values()) {
            map.put(role, role.createState(persistentState, volatileState));
        }
        return map;
    }

    private State initialState() {
        return transitionTo(Role.FOLLOWER);
    }

    private State transitionTo(final Role role) {
        return stateMap.get(role);
    }

    public void onMessage(final ServerContext serverContext, final Message message) {
        final Role targetRole = currentState.onMessage(serverContext, message);
        if (targetRole != currentState.role()) {
            currentState = transitionTo(targetRole);
        }
    }

    @Deprecated //FIXME remove this and merge with onMessage(..)
    public void perform(final ServerContext serverContext) {
        checkElectionTimeout();
        currentState.perform(serverContext);
        invokeStateMachineWithCommittedLogEntry(serverContext);
        if (volatileState.role() != currentState.role()) {
            currentState = transitionTo(volatileState.role());
        }
    }

    private void checkElectionTimeout() {
        final Timer timer = volatileState.electionState().electionTimer();
        if (timer.hasTimeoutElapsed(Clock.DEFAULT)) {
            persistentState.clearVotedForAndIncCurrentTerm();
            volatileState.changeRoleTo(Role.CANDIDATE);
            timer.restart(Clock.DEFAULT);
        }
    }

    private void invokeStateMachineWithCommittedLogEntry(final ServerContext serverContext) {
        final MessageLog<CommandMessage> messageLog = serverContext.messageLog();
        final StateMachine stateMachine = serverContext.stateMachine();
        long lastApplied = volatileState.lastApplied();
        while (volatileState.commitIndex() > lastApplied) {
            lastApplied++;
            messageLog.readIndex(lastApplied);
            final CommandMessage commandMsg = messageLog.read();
            stateMachine.onMessage(commandMsg);
            volatileState.lastApplied(lastApplied);
        }
    }

}
