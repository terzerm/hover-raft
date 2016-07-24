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

import io.aeron.Publication;
import org.tools4j.hoverraft.message.MessageHandler;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.message.direct.DirectMessage;
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
    public void perform(final Server server) {
        if (server.state().persistentState().votedFor() < 0) {
            voteForMyself(server);
        }
    }

    private void onVoteResponse(final Server server, final VoteResponse voteResponse) {
        if (voteResponse.term() == server.currentTerm() && voteResponse.voteGranted()) {
            incVoteCount(server);
        }
    }

    private void voteForMyself(final Server server) {
        final VolatileState vstate = server.state().volatileState();
        final PersistentState pstate = server.state().persistentState();
        final ElectionState estate = vstate.electionState();
        final int self = server.serverConfig().id();
        pstate.votedFor(self);
        estate.initVoteCount();
        requestVoteFromAllServers(server, self);
    }

    private void requestVoteFromAllServers(final Server server, final int self) {
        int maxTries = 100;//TODO how often should we retry sending?
        final Publication serverMulticast = server.connections().serverMulticast();
        final DirectMessage message = server.messageFactory().voteRequest()
                .term(server.currentTerm())
                .candidateId(self);
        server.send(serverMulticast, message);
    }

    private void incVoteCount(final Server server) {
        final VolatileState vstate = server.state().volatileState();
        vstate.electionState().incVoteCount();
        final int servers = server.consensusConfig().serverCount();
        final int majority = (servers + 1) / 2;
        if (vstate.electionState().voteCount() >= majority) {
            vstate.changeRoleTo(Role.LEADER);
        }
    }

}
