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
package org.tools4j.hoverraft.handler;

import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.VolatileState;

import java.util.Objects;

public final class HigherTermHandler implements MessageHandler {

    private final PersistentState persistentState;
    private final VolatileState volatileState;

    public HigherTermHandler(final PersistentState persistentState, final VolatileState volatileState) {
        this.persistentState = Objects.requireNonNull(persistentState);
        this.volatileState = Objects.requireNonNull(volatileState);
    }

    @Override
    public void onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        onTerm(serverContext, voteRequest.term());
    }

    @Override
    public void onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
        onTerm(serverContext, voteResponse.term());
    }

    @Override
    public void onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        onTerm(serverContext, appendRequest.term());
    }

    @Override
    public void onAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
        onTerm(serverContext, appendResponse.term());
    }

    @Override
    public void onTimeoutNow(final ServerContext serverContext, final TimeoutNow timeoutRequest) {
        onTerm(serverContext, timeoutRequest.term());
    }

    @Override
    public void onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
        onTerm(serverContext, commandMessage.term());
    }

    private void onTerm(final ServerContext serverContext, final int term) {
        if (term > persistentState.currentTerm()) {
            persistentState.clearVotedForAndSetCurrentTerm(term);
            volatileState.changeRoleTo(Role.FOLLOWER);
        }
    }

}