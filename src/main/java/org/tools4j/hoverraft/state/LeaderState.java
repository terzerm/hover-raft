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

import org.tools4j.hoverraft.command.LogEntry;
import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.timer.TimerEvent;

public class LeaderState extends AbstractState {

    public LeaderState(final PersistentState persistentState, final VolatileState volatileState) {
        super(Role.LEADER, persistentState, volatileState);
    }

    @Override
    protected EventHandler eventHandler() {
        return new EventHandler() {
            @Override
            public Transition onTransition(ServerContext serverContext, Transition transition) {
                return LeaderState.this.onTransition(serverContext, transition);
            }

            @Override
            public Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
                return LeaderState.this.onVoteRequest(serverContext, voteRequest);
            }

            @Override
            public Transition onAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
                return LeaderState.this.onAppendResponse(serverContext, appendResponse);
            }

            @Override
            public Transition onCommandMessage(final ServerContext serverContext, final CommandMessage commandMessage) {
                return LeaderState.this.onCommandMessage(serverContext, commandMessage);
            }

            @Override
            public Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
                return LeaderState.this.onTimerEvent(serverContext, timerEvent);
            }
        };
    }

    private Transition onTransition(final ServerContext serverContext, final Transition transition) {
        final long heartbeatMillis = serverContext.consensusConfig().heartbeatTimeoutMillis();
        serverContext.timer().restart(heartbeatMillis, heartbeatMillis);
        volatileState().resetFollowersState(persistentState().commandLog().lastKey().index() + 1);
        return Transition.STEADY;
    }

    private Transition onCommandMessage(final ServerContext serverContext, final CommandMessage commandMessage) {
        LogEntry newLogEntry = serverContext.directFactory().logEntry();

        newLogEntry.logKey().term(currentTerm());
        newLogEntry.commandMessage()
                .commandSourceId(commandMessage.commandSourceId())
                .commandIndex(commandMessage.commandIndex())
                .command().copyFrom(commandMessage.command());

        persistentState().commandLog().append(newLogEntry);
        sendAppendRequest(serverContext);//FIXME send log message in request
        return Transition.STEADY;
    }

    private Transition onAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
        if (!appendResponse.successful()) {
            volatileState().followerState(appendResponse.serverId()).decrementNextIndex();
            sendAppendRequest(serverContext, appendResponse.serverId());
        } else {
            volatileState().followerState(appendResponse.serverId()).matchIndex(appendResponse.matchLogIndex());
            volatileState().followerState(appendResponse.serverId()).nextIndex(appendResponse.matchLogIndex() + 1);
        }
        updateCommitIndex();
        invokeStateMachineWithCommittedLogEntries(serverContext);
        return Transition.STEADY;
    }

    private Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
        sendAppendRequest(serverContext);
        return Transition.STEADY;
    }

    //FIXme test it!!!
    private void updateCommitIndex() {

        long currentCommitIndex = volatileState().commitIndex();

        int followerCount = volatileState().followerCount();

        final int majority = (followerCount + 1) / 2;

        long minIndexHigherThanCommitIndex = Long.MAX_VALUE;
        int higherThanCommitCount = 0;

        for(int idx = 0; idx < followerCount; idx++) {

            final TrackedFollowerState followerState = volatileState().followerState(idx);
            final long matchIndex = followerState.matchIndex();

            if (matchIndex > currentCommitIndex) {

                persistentState().commandLog().readIndex(matchIndex);
                final int matchTerm = persistentState().commandLog().readTerm();

                if (matchTerm == currentTerm()) {

                    higherThanCommitCount++;
                    if (matchIndex < minIndexHigherThanCommitIndex) {
                        minIndexHigherThanCommitIndex = matchIndex;
                    }
                }
            }
        }
        if (higherThanCommitCount >= majority) {
            volatileState().commitIndex(minIndexHigherThanCommitIndex);
        }
    }

    private void sendAppendRequest(final ServerContext serverContext) {
        //FIXME impl
        serverContext.timer().reset();

    }

    private void sendAppendRequest(final ServerContext serverContext, final int serverId) {
        //FIXME impl

//        final TrackedFollowerState trackedFollowerState =  volatileState().followerStateById(serverId);
//        final long prevLogEntryIndex = trackedFollowerState.nextIndex() - 1;
//
//        final AppendRequest appendRequest = serverContext.directFactory().appendRequest()
//                .term(currentTerm())
//                .leaderCommit(volatileState().commitIndex())
//                .leaderId(serverContext.id());
//
//
//        appendRequest.prevLogKey().index(prevLogEntryIndex).term()
//
//        //here I realised that CommandLogEntry should have LogEntryKey instead of be a LogEntry
//        appendRequest
//                .prevLogKey().index(44)
//                .term(33);
//
//        persistentState().commandLog().readIndex(trackedFollowerState.nextIndex());
//        persistentState().commandLog().readTo(appendRequest.logEntry());
//
//
//        appendRequest.logEntry().term()
//
//                .sendTo(serverContext.connections().serverMulticastSender(),
//                        serverContext.resendStrategy());

    }

}
