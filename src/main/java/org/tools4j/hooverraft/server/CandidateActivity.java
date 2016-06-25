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

import org.tools4j.hooverraft.ipc.*;
import org.tools4j.hooverraft.message.Publication;
import org.tools4j.hooverraft.state.ElectionState;
import org.tools4j.hooverraft.state.PersistentState;
import org.tools4j.hooverraft.state.Role;
import org.tools4j.hooverraft.state.VolatileState;

public final class CandidateActivity implements ServerActivity {

    private final MessageHandler messageHandler = CompositeMessageHandler.compose(
            new ElectionTimerResetHandler(),
            new HigherTermHandler(),
            new CandidateHandler()
    );

    private final class CandidateHandler implements MessageHandler {

        @Override
        public void onVoteRequest(final Server server, final VoteRequest voteRequest) {
            sendVoteResponse(server, voteRequest);
        }

        @Override
        public void onVoteResponse(final Server server, final VoteResponse voteResponse) {
            if (voteResponse.term() == server.currentTerm() && voteResponse.voteGranted()) {
                incVoteCount(server);
            }
        }

        @Override
        public void onAppendRequest(final Server server, final AppendRequest appendRequest) {
            //no op
        }

        @Override
        public void onAppendResponse(final Server server, final AppendResponse appendResponse) {
            //no op
        }
    }

    @Override
    public MessageHandler messageHandler() {
        return messageHandler;
    }

    @Override
    public void perform(final Server server) {
        final VolatileState vstate = server.state().volatileState();
        final PersistentState pstate = server.state().persistentState();
        final ElectionState estate = vstate.electionState();
        if (pstate.votedFor() < 0 || estate.electionTimer().hasTimeoutElapsed()) {
            voteForMyself(server);
        }
    }

    private void voteForMyself(final Server server) {
        final VolatileState vstate = server.state().volatileState();
        final PersistentState pstate = server.state().persistentState();
        final ElectionState estate = vstate.electionState();
        final int self = server.config().id();
        final int term = pstate.incCurrentTermAndVoteForSelf(self);
        estate.initVoteCount();
        requestVoteFromAllServers(server, term, self);
        estate.electionTimer().restart();
    }

    private void requestVoteFromAllServers(final Server server, final int term, final int self) {
        int maxTries = 100;//TODO how often should we retry sending?
        final Publication serverMulticast = server.connections().serverMulticast();
        server.messageFactory().voteRequest()
                .term(term)
                .candidateId(self)
                .offerTo(serverMulticast, maxTries);
    }

    private void sendVoteResponse(final Server server, final VoteRequest voteRequest) {
        int maxTries = 100;//TODO how often should we retry sending?
        final PersistentState pstate = server.state().persistentState();
        final Publication serverPublication = server.connections().serverPublication(voteRequest.candidateId());
        server.messageFactory().voteResponse()
                .term(server.currentTerm())
                .voteGranted(false) /* CANDIDATE, hence we have voted for ourself */
                .offerTo(serverPublication, maxTries);
    }

    private void incVoteCount(final Server server) {
        final VolatileState vstate = server.state().volatileState();
        vstate.electionState().incVoteCount();
        final int servers = server.config().consensusConfig().serverCount();
        final int majority = (servers + 1) / 2;
        if (vstate.electionState().voteCount() >= majority) {
            vstate.changeRoleTo(Role.LEADER);
        }
    }
}
