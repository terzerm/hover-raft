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

import org.tools4j.hoverraft.server.Server;

import java.util.Objects;

public final class CompositeMessageHandler implements MessageHandler {

    private final MessageHandler[] handlers;

    protected CompositeMessageHandler(final MessageHandler... handlers) {
        this.handlers = Objects.requireNonNull(handlers);
    }

    public static CompositeMessageHandler compose(final MessageHandler... handlers) {
        return new CompositeMessageHandler(handlers);
    }

    public void onVoteRequest(Server server, VoteRequest voteRequest) {
        for (final MessageHandler handler : handlers) {
            handler.onVoteRequest(server, voteRequest);
        }
    }

    public void onVoteResponse(Server server, VoteResponse voteResponse) {
        for (final MessageHandler handler : handlers) {
            handler.onVoteResponse(server, voteResponse);
        }
    }

    public void onAppendRequest(Server server, AppendRequest appendRequest) {
        for (final MessageHandler handler : handlers) {
            handler.onAppendRequest(server, appendRequest);
        }
    }

    public void onAppendResponse(Server server, AppendResponse appendResponse) {
        for (final MessageHandler handler : handlers) {
            handler.onAppendResponse(server, appendResponse);
        }
    }

    @Override
    public void onTimeoutNow(final Server server, final TimeoutNow timeoutNow) {
        for (final MessageHandler handler : handlers) {
            handler.onTimeoutNow(server, timeoutNow);
        }
    }
}
