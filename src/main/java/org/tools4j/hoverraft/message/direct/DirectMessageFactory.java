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
package org.tools4j.hoverraft.message.direct;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.message.MessageFactory;

import java.nio.ByteBuffer;

/**
 * Factory for messages writing data into an internal readBuffer. Messages and readBuffer are reused accross
 * multiple calls.
 */
public final class DirectMessageFactory implements MessageFactory {

    public static final int MAX_BYTE_LENGTH = 4096;//FIXME enforce this somehow

    private final MutableDirectBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(MAX_BYTE_LENGTH));

    private final DirectAppendRequest appendRequest = wrap(new DirectAppendRequest());
    private final DirectAppendResponse appendResponse = wrap(new DirectAppendResponse());
    private final DirectVoteRequest voteRequest = wrap(new DirectVoteRequest());
    private final DirectVoteResponse voteResponse = wrap(new DirectVoteResponse());
    private final DirectTimeoutNow timeoutNow = wrap(new DirectTimeoutNow());
    private final DirectCommandMessage commandMessage = wrap(new DirectCommandMessage());

    public DirectAppendRequest appendRequest() {
        return appendRequest;
    }

    public DirectAppendResponse appendResponse() {
        return appendResponse;
    }

    public DirectVoteRequest voteRequest() {
        return voteRequest;
    }

    public DirectVoteResponse voteResponse() {
        return voteResponse;
    }

    public DirectTimeoutNow timeoutNow() {
        return timeoutNow;
    }

    public DirectCommandMessage commandMessage() {
        return commandMessage;
    }

    private final <M extends AbstractMessage> M wrap(final M message) {
        message.wrap(buffer, 0);
        return message;
    }

}
