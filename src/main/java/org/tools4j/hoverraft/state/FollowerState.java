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

import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.TimeoutNow;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.timer.TimerEvent;

public class FollowerState extends AbstractState {

    public FollowerState() {
        super(Role.FOLLOWER);
    }

    @Override
    protected EventHandler eventHandler() {
        return new EventHandler() {
            @Override
            public Transition onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
                return onVoteRequest(serverContext, voteRequest);
            }

            @Override
            public Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
                return FollowerState.this.onAppendRequest(serverContext, appendRequest);
            }

            @Override
            public Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
                return FollowerState.this.onTimerEvent(serverContext, timerEvent);
            }

            @Override
            public Transition onTimeoutNow(final ServerContext serverContext, final TimeoutNow timeoutNow) {
                return FollowerState.this.onTimeoutNow(serverContext, timeoutNow);
            }

        };
    }


    private Transition onTimerEvent(final ServerContext serverContext, final TimerEvent timerEvent) {
        return Transition.TO_CANDIDATE;
    }

    private Transition onTimeoutNow(final ServerContext serverContext, final TimeoutNow timeoutNow) {
        final int currentTerm = serverContext.persistentState().currentTerm();
        if (timeoutNow.term() == currentTerm && timeoutNow.candidateId() == serverContext.id()) {
            return Transition.TO_CANDIDATE;
        }
        return Transition.STEADY;
    }


}
