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

import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.ServerState;

public final class VoteRequestHandler {

    public void onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        final int term = voteRequest.term();
        final int candidateId = voteRequest.candidateId();
        final boolean granted;
        if (serverContext.currentTerm() == term && isValidCandidate(serverContext, voteRequest)) {
            final ServerState state = serverContext.state();
            final PersistentState pstate = state.persistentState();
            if (pstate.votedFor() < 0) {
                pstate.votedFor(candidateId);
                state.volatileState().changeRoleTo(Role.FOLLOWER);
                granted = true;
            } else {
                granted = pstate.votedFor() == candidateId;
            }
        } else {
            granted = false;
        }
        serverContext.messageFactory().voteResponse()
                .term(serverContext.currentTerm())
                .voteGranted(granted)
                .sendTo(serverContext.connections().serverSender(candidateId),
                        serverContext.resendStrategy());
    }

    private static boolean isValidCandidate(final ServerContext serverContext, final VoteRequest voteRequest) {
        final int lastLogTerm = voteRequest.lastLogTerm();
        final long lastLogIndex = voteRequest.lastLogIndex();
        final PersistentState pstate = serverContext.state().persistentState();
        return pstate.lastLogTerm() < lastLogTerm ||
                (pstate.lastLogTerm() == lastLogTerm && pstate.lastLogIndex() <= lastLogIndex);
    }
}
