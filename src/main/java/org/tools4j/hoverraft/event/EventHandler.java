/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 hover-raft (tools4j), Marco Terzer
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

import org.tools4j.hoverraft.command.Command;
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.Transition;
import org.tools4j.hoverraft.timer.TimerEvent;

/**
 * Event handler for all event types. Acts as visitor called back in
 * {@link Event#accept(ServerContext, EventHandler)}.
 */
public interface EventHandler {
    default Transition onTransition(ServerContext serverContext, Transition transition) {return Transition.STEADY;}
    default Transition onCommand(ServerContext serverContext, Command command) {return Transition.STEADY;}
    default Transition onVoteRequest(ServerContext serverContext, VoteRequest voteRequest) {return Transition.STEADY;}
    default Transition onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {return Transition.STEADY;}
    default Transition onAppendRequest(ServerContext serverContext, AppendRequest appendRequest) {return Transition.STEADY;}
    default Transition onAppendResponse(ServerContext serverContext, AppendResponse appendResponse) {return Transition.STEADY;}
    default Transition onTimeoutNow(ServerContext serverContext, TimeoutNow timeoutNow) {return Transition.STEADY;}
    default Transition onTimerEvent(ServerContext serverContext, TimerEvent timerEvent) {return Transition.STEADY;};
}
