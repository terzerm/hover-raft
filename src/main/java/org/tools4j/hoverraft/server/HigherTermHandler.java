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

import org.tools4j.hoverraft.ipc.*;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.ServerState;

public final class HigherTermHandler implements MessageHandler {

    @Override
    public void onVoteRequest(final Server server, final VoteRequest voteRequest) {
        onTerm(server, voteRequest.term());
    }

    @Override
    public void onVoteResponse(final Server server, final VoteResponse voteResponse) {
        onTerm(server, voteResponse.term());
    }

    @Override
    public void onAppendRequest(final Server server, final AppendRequest appendRequest) {
        onTerm(server, appendRequest.term());
    }

    @Override
    public void onAppendResponse(final Server server, final AppendResponse appendResponse) {
        onTerm(server, appendResponse.term());
    }

    @Override
    public void onTimeoutNow(final Server server, final TimeoutNow timeoutRequest) {
        onTerm(server, timeoutRequest.term());
    }

    private void onTerm(final Server server, final int term) {
        final ServerState state = server.state();
        final PersistentState pstate = state.persistentState();
        if (term > pstate.currentTerm()) {
            pstate.clearVotedForAndSetCurrentTerm(term);
            state.volatileState().changeRoleTo(Role.FOLLOWER);
        }
    }

}
