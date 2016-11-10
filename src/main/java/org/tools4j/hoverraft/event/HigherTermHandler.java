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
package org.tools4j.hoverraft.event;

import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Transition;

import java.util.Objects;

public final class HigherTermHandler implements EventHandler {

    private final PersistentState persistentState;

    public HigherTermHandler(final PersistentState persistentState) {
        this.persistentState = Objects.requireNonNull(persistentState);
    }

    @Override
    public Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        return onTerm(voteRequest.term());
    }

    @Override
    public Transition onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
        return onTerm(voteResponse.term());
    }

    @Override
    public Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        return onTerm(appendRequest.term());
    }

    @Override
    public Transition onAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
        return onTerm(appendResponse.term());
    }

    @Override
    public Transition onTimeoutNow(final ServerContext serverContext, final TimeoutNow timeoutRequest) {
        return onTerm(timeoutRequest.term());
    }

    @Override
    public Transition onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
        return onTerm(commandMessage.term());
    }

    private Transition onTerm(final int term) {
        if (term > persistentState.currentTerm()) {
            persistentState.clearVotedForAndSetCurrentTerm(term);
            return Transition.TO_FOLLOWER;
        }
        return Transition.STEADY;
    }

}
