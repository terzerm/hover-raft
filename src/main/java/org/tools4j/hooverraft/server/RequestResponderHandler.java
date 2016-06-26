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
import org.tools4j.hooverraft.state.PersistentState;
import org.tools4j.hooverraft.state.Role;
import org.tools4j.hooverraft.state.ServerState;
import org.tools4j.hooverraft.state.VolatileState;

public final class RequestResponderHandler implements MessageHandler {

    private static final int MAX_TRIES = 10;//TODO how often should we retry sending?

    private Publication serverPublication(final Server server, final int candidateId) {
        return server.connections().serverPublication(candidateId);
    }

    @Override
    public void onVoteRequest(final Server server, final VoteRequest voteRequest) {
        final int term = voteRequest.term();
        final int candidateId = voteRequest.candidateId();
        final boolean granted;
        if (server.currentTerm() == term) /* should never be larger */ {
            final ServerState state = server.state();
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
        final Publication serverPublication = serverPublication(server, candidateId);
        server.messageFactory().voteResponse()
                .term(server.currentTerm())
                .voteGranted(granted)
                .offerTo(serverPublication, MAX_TRIES);
    }

    @Override
    public void onAppendRequest(final Server server, final AppendRequest appendRequest) {
        final int term = appendRequest.term();
        final int leaderId = appendRequest.leaderId();
        final boolean successful;
        if (server.currentTerm() == term) /* should never be larger */ {
            final VolatileState vstate = server.state().volatileState();
            if (vstate.role() == Role.FOLLOWER) {
                vstate.electionState().electionTimer().reset();
            } else {
                vstate.changeRoleTo(Role.FOLLOWER);
                vstate.electionState().electionTimer().restart();
            }
            //FIXME append entries to message log here
            successful = true;
        } else {
            successful = false;
        }
        final Publication serverPublication = serverPublication(server, leaderId);
        server.messageFactory().appendResponse()
                .term(server.currentTerm())
                .successful(successful)
                .offerTo(serverPublication, MAX_TRIES);
    }

    @Override
    public void onVoteResponse(final Server server, final VoteResponse voteResponse) {
        //no op
    }

    @Override
    public void onAppendResponse(final Server server, final AppendResponse appendResponse) {
        //no op
    }

}
