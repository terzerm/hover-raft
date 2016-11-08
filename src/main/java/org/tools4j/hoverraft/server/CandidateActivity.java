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

import org.tools4j.hoverraft.message.MessageHandler;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.state.ElectionState;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.VolatileState;

public final class CandidateActivity implements ServerActivity {

    private final MessageHandler messageHandler = new HigherTermHandler()
            .thenHandleVoteRequest(new VoteRequestHandler()::onVoteRequest)
            .thenHandleAppendRequest(new AppendRequestHandler()::onAppendRequest)
            .thenHandleVoteResponse(this::onVoteResponse);

    @Override
    public MessageHandler messageHandler() {
        return messageHandler;
    }

    @Override
    public void perform(final ServerContext serverContext) {
        if (serverContext.state().persistentState().votedFor() < 0) {
            voteForMyself(serverContext);
        }
    }

    private void onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
        if (voteResponse.term() == serverContext.currentTerm() && voteResponse.voteGranted()) {
            incVoteCount(serverContext);
        }
    }

    private void voteForMyself(final ServerContext serverContext) {
        final VolatileState vstate = serverContext.state().volatileState();
        final PersistentState pstate = serverContext.state().persistentState();
        final ElectionState estate = vstate.electionState();
        final int self = serverContext.serverConfig().id();
        pstate.votedFor(self);
        estate.initVoteCount();
        requestVoteFromAllServers(serverContext, self);
    }

    private void requestVoteFromAllServers(final ServerContext serverContext, final int self) {
        serverContext.messageFactory().voteRequest()
                .term(serverContext.currentTerm())
                .candidateId(self)
                .sendTo(serverContext.connections().serverMulticastSender(),
                        serverContext.resendStrategy());
    }

    private void incVoteCount(final ServerContext serverContext) {
        final VolatileState vstate = serverContext.state().volatileState();
        vstate.electionState().incVoteCount();
        final int servers = serverContext.consensusConfig().serverCount();
        final int majority = (servers + 1) / 2;
        if (vstate.electionState().voteCount() >= majority) {
            vstate.changeRoleTo(Role.LEADER);
        }
    }

}
