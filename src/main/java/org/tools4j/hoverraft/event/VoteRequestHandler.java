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

import org.tools4j.hoverraft.command.CommandLog;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Transition;

import java.util.Objects;

public final class VoteRequestHandler {

    private final PersistentState persistentState;

    public VoteRequestHandler(final PersistentState persistentState) {
        this.persistentState = Objects.requireNonNull(persistentState);
    }

    public Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        final CommandLog commandLog = persistentState.commandLog();
        final int term = voteRequest.term();
        final int candidateId = voteRequest.candidateId();
        final Transition transition;
        final boolean granted;
        if (persistentState.currentTerm() <= term && commandLog.lastKeyCompareTo(voteRequest.lastLogKey()) <= 0) {
            if (persistentState.votedFor() == PersistentState.NOT_VOTED_YET) {
                persistentState.votedFor(candidateId);
                //Why transition is TO_FOLLOWER?
                //in this case we should not replay the event!?
                transition = Transition.TO_FOLLOWER;
                granted = true;
            } else {
                granted = persistentState.votedFor() == candidateId;
                transition = Transition.STEADY;
            }
        } else {
            granted = false;
            transition = Transition.STEADY;
        }
        serverContext.directFactory().voteResponse()
                .term(persistentState.currentTerm())
                .voteGranted(granted)
                .sendTo(serverContext.connections().serverSender(candidateId),
                        serverContext.resendStrategy());
        return transition;
    }
}
