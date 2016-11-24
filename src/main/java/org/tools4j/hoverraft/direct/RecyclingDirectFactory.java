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
package org.tools4j.hoverraft.direct;

import org.tools4j.hoverraft.command.CommandLogEntry;
import org.tools4j.hoverraft.command.DirectCommandLogEntry;
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.message.direct.*;

/**
 * Not a true factory since messages are recycled accross multiple calls.
 */
public final class RecyclingDirectFactory implements DirectFactory {

    private final AppendRequest appendRequest = new DirectAppendRequest();
    private final AppendResponse appendResponse = new DirectAppendResponse();
    private final VoteRequest voteRequest = new DirectVoteRequest();
    private final VoteResponse voteResponse = new DirectVoteResponse();
    private final TimeoutNow timeoutNow = new DirectTimeoutNow();
    private final CommandMessage commandMessage = new DirectCommandMessage();
    private final CommandLogEntry commandLogEntry = new DirectCommandLogEntry();

    @Override
    public AppendRequest appendRequest() {
        return appendRequest;
    }

    @Override
    public AppendResponse appendResponse() {
        return appendResponse;
    }

    @Override
    public VoteRequest voteRequest() {
        return voteRequest;
    }

    @Override
    public VoteResponse voteResponse() {
        return voteResponse;
    }

    @Override
    public TimeoutNow timeoutNow() {
        return timeoutNow;
    }

    @Override
    public CommandMessage commandMessage() {
        return commandMessage;
    }

    @Override
    public CommandLogEntry commandLogEntry() {
        return commandLogEntry;
    }

 }