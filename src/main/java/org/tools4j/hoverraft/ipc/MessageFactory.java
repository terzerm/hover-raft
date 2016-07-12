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
package org.tools4j.hoverraft.ipc;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.message.CommandMessage;

import java.nio.ByteBuffer;

/**
 * Factory for messages writing data into an internal readBuffer. Messages and readBuffer are reused accross
 * multiple calls.
 */
public final class MessageFactory {

    private final MutableDirectBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(Math.max(MessageType.maxSize(), CommandMessage.MESSAGE_SIZE)));

    private final AppendRequest appendRequest = wrap(new AppendRequest());
    private final AppendResponse appendResponse = wrap(new AppendResponse());
    private final VoteRequest voteRequest = wrap(new VoteRequest());
    private final VoteResponse voteResponse = wrap(new VoteResponse());
    private final CommandMessage commandMessage = wrap(new CommandMessage());

    public AppendRequest appendRequest() {
        return appendRequest;
    }

    public AppendResponse appendResponse() {
        return appendResponse;
    }

    public VoteRequest voteRequest() {
        return voteRequest;
    }

    public VoteResponse voteResponse() {
        return voteResponse;
    }

    public CommandMessage commandMessage() {
        return commandMessage;
    }

    private final <M extends Message> M wrap(final M message) {
        message.wrap(buffer, 0);
        return message;
    }

}
