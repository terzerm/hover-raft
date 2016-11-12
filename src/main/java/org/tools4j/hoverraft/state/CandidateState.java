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

import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.timer.TimerEvent;

public final class CandidateState extends AbstractState {

    private int voteCount;

    public CandidateState(final PersistentState persistentState, final VolatileState volatileState) {
        super(Role.CANDIDATE, persistentState, volatileState);
    }

    @Override
    protected EventHandler eventHandler() {
        return new EventHandler() {
            @Override
            public Transition onTransition(final ServerContext serverContext, final Transition transition) {
                return CandidateState.this.onTransition(serverContext, transition);
            }

            @Override
            public Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
                return CandidateState.this.onVoteRequest(serverContext, voteRequest);
            }

            @Override
            public Transition onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
                return CandidateState.this.onVoteResponse(serverContext, voteResponse);
            }

            @Override
            public Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
                return CandidateState.this.onAppendRequest(serverContext, appendRequest);
            }

            @Override
            public Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
                return CandidateState.this.onTimerEvent(serverContext, timerEvent);
            }
        };
    }

    private Transition onTransition(final ServerContext serverContext, final Transition transition) {
        startNewElection(serverContext);
        return Transition.STEADY;
    }

    private Transition onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
        if (voteResponse.term() == currentTerm() && voteResponse.voteGranted()) {
            return incVoteCount(serverContext);
        }
        return Transition.STEADY;
    }


    //should probably be shared with a follower
    private Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        final int term = appendRequest.term();
        final int leaderId = appendRequest.leaderId();
        final int currentTerm = currentTerm();
        final Transition transition;
        final boolean successful;
        if (currentTerm <= term) /* should never be larger */ {
            //If AppendEntries RPC received from new leader: convert to
            //follower
            serverContext.timer().reset();
            successful = appendToLog(serverContext, appendRequest);
            transition = Transition.TO_FOLLOWER;
        } else { //currentTerm > term based on paper
            successful = false;
            transition = Transition.STEADY;
        }
        serverContext.messageFactory().appendResponse()
                .term(currentTerm)
                .successful(successful)
                .sendTo(serverContext.connections().serverSender(leaderId),
                        serverContext.resendStrategy());
        return transition;
    }

    private Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
        startNewElection(serverContext);
        return Transition.STEADY;
    }

    private void startNewElection(final ServerContext serverContext) {
        final ConsensusConfig config = serverContext.consensusConfig();
        persistentState().clearVotedForAndIncCurrentTerm();
        serverContext.timer().restart(config.minElectionTimeoutMillis(), config.maxElectionTimeoutMillis());
        voteForMyself(serverContext);
    }

    //should be shared with a follower as well.
    private boolean appendToLog(final ServerContext serverContext, final AppendRequest appendRequest) {
        final long prevLogIndex = appendRequest.prevLogIndex();
        final long prevLogTerm = appendRequest.prevLogTerm();
        final int currentTerm = currentTerm();

        if (persistentState().lastLogIndex() < prevLogIndex) {
            return false;
        } else {
            final int termAtPrevLogIndex = persistentState().termAtLogIndex(prevLogIndex);
            if (currentTerm != termAtPrevLogIndex) {
                //delete logs starting from prevLogIndex
                persistentState().truncateLog(prevLogIndex); //could access the log via serverContext.messageLog()
            }
            //FIXme implement append new entries
            //Append any new entries not already in the log

            //If leaderCommit > commitIndex, set commitIndex =
            //        min(leaderCommit, index of last new entry)
            return true;
        }

        //Reply false if log doesn’t contain an entry at prevLogIndex
        //whose term matches prevLogTerm (§5.3)
        //3. If an existing entry conflicts with a new one (same index
        //        but different terms), delete the existing entry and all that
        //follow it (§5.3)
    }

    private void voteForMyself(final ServerContext serverContext) {
        final PersistentState pstate = persistentState();
        final int self = serverContext.serverConfig().id();
        pstate.votedFor(self);
        voteCount = 1;
        requestVoteFromAllServers(serverContext, self);
    }

    private void requestVoteFromAllServers(final ServerContext serverContext, final int self) {
        serverContext.messageFactory().voteRequest()
                .term(currentTerm())
                .candidateId(self)
                .sendTo(serverContext.connections().serverMulticastSender(),
                        serverContext.resendStrategy());
    }

    private Transition incVoteCount(final ServerContext serverContext) {
        voteCount++;
        final int servers = serverContext.consensusConfig().serverCount();
        final int majority = (servers + 1) / 2;
        if (voteCount >= majority) {
            return Transition.TO_LEADER;
        }
        return Transition.STEADY;
    }

}
