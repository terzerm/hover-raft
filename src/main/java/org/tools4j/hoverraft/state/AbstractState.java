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

import org.tools4j.hoverraft.command.log.CommandLog;
import org.tools4j.hoverraft.command.log.CommandLogEntry;
import org.tools4j.hoverraft.command.log.DirectCommandLogEntry;
import org.tools4j.hoverraft.command.machine.StateMachine;
import org.tools4j.hoverraft.event.*;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.server.ServerContext;

import java.util.Objects;

abstract public class AbstractState implements State {

    private final Role role;
    private final PersistentState persistentState;
    private final VolatileState volatileState;
    private final HigherTermHandler higherTermHandler;
    private final VoteRequestHandler voteRequestHandler;
    private final AppendRequestHandler appendRequestHandler;

    public AbstractState(final Role role, final PersistentState persistentState, final VolatileState volatileState) {
        this.role = Objects.requireNonNull(role);
        this.persistentState = Objects.requireNonNull(persistentState);
        this.volatileState = Objects.requireNonNull(volatileState);
        this.higherTermHandler = new HigherTermHandler(persistentState);
        this.voteRequestHandler = new VoteRequestHandler(persistentState);
        this.appendRequestHandler = new AppendRequestHandler(persistentState, volatileState);
    }

    abstract protected EventHandler eventHandler();

    @Override
    public final Role role() {
        return role;
    }

    @Override
    public final Transition onEvent(final ServerContext serverContext, final Event event) {
        return Transition
                .startWith(serverContext, event, higherTermHandler)
                .ifSteadyThen(serverContext, event, eventHandler());
    }

    public int currentTerm() {
        return persistentState.currentTerm();
    }

    protected PersistentState persistentState() {
        return persistentState;
    }

    protected VolatileState volatileState() {
        return volatileState;
    }

    protected Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        return voteRequestHandler.onVoteRequest(serverContext, voteRequest);
    }

    protected Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        return appendRequestHandler.onAppendRequest(serverContext, appendRequest);
    }

    protected void invokeStateMachineWithCommittedLogEntries(final ServerContext serverContext) {
        final CommandLog commandLog = persistentState.commandLog();
        final StateMachine stateMachine = serverContext.stateMachine();
        long lastApplied = volatileState.lastApplied();
        while (volatileState.commitIndex() > lastApplied) {
            lastApplied++;
            commandLog.readIndex(lastApplied);
            final CommandLogEntry commandLogEntry = commandLog.read(new DirectCommandLogEntry() /*FIXME move to factory*/);
            stateMachine.onMessage(commandLogEntry.commandMessage());
            volatileState.lastApplied(lastApplied);
        }
    }
}
