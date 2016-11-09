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

import org.tools4j.hoverraft.handler.HigherTermHandler;
import org.tools4j.hoverraft.handler.MessageHandler;
import org.tools4j.hoverraft.handler.VoteRequestHandler;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.util.Clock;

public final class CandidateState extends AbstractState {

    private final MessageHandler messageHandler;

    public CandidateState(final PersistentState persistentState, final VolatileState volatileState) {
        super(Role.CANDIDATE, persistentState, volatileState);
        this.messageHandler = new HigherTermHandler(persistentState, volatileState)
                .thenHandleVoteRequest(new VoteRequestHandler(persistentState, volatileState)::onVoteRequest)
                .thenHandleAppendRequest(this::onAppendRequest)
                .thenHandleVoteResponse(this::onVoteResponse);
    }

    @Override
    public Role onMessage(final ServerContext serverContext, final Message message) {
        message.accept(serverContext, messageHandler);
        return volatileState().role();
    }

    @Override
    public void perform(final ServerContext serverContext) {
        if (persistentState().votedFor() < 0) {
            voteForMyself(serverContext);
        }
    }

    private void onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
        if (voteResponse.term() == currentTerm() && voteResponse.voteGranted()) {
            incVoteCount(serverContext);
        }
    }

    private void onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        final int term = appendRequest.term();
        final int leaderId = appendRequest.leaderId();
        final int currentTerm = currentTerm();
        final boolean successful;
        if (currentTerm == term) /* should never be larger */ {
            volatileState().changeRoleTo(Role.FOLLOWER);
            volatileState().electionState().electionTimer().restart(Clock.DEFAULT);
            successful = appendToLog(serverContext, appendRequest);
        } else {
            successful = false;
        }
        serverContext.messageFactory().appendResponse()
                .term(currentTerm)
                .successful(successful)
                .sendTo(serverContext.connections().serverSender(leaderId),
                        serverContext.resendStrategy());
    }

    private boolean appendToLog(final ServerContext serverContext, final AppendRequest appendRequest) {
        //FIXME write to message log
        return true;
    }

    private void voteForMyself(final ServerContext serverContext) {
        final VolatileState vstate = volatileState();
        final PersistentState pstate = persistentState();
        final ElectionState estate = vstate.electionState();
        final int self = serverContext.serverConfig().id();
        pstate.votedFor(self);
        estate.initVoteCount();
        requestVoteFromAllServers(serverContext, self);
    }

    private void requestVoteFromAllServers(final ServerContext serverContext, final int self) {
        serverContext.messageFactory().voteRequest()
                .term(currentTerm())
                .candidateId(self)
                .sendTo(serverContext.connections().serverMulticastSender(),
                        serverContext.resendStrategy());
    }

    private void incVoteCount(final ServerContext serverContext) {
        final VolatileState vstate = volatileState();
        vstate.electionState().incVoteCount();
        final int servers = serverContext.consensusConfig().serverCount();
        final int majority = (servers + 1) / 2;
        if (vstate.electionState().voteCount() >= majority) {
            vstate.changeRoleTo(Role.LEADER);
        }
    }

}
