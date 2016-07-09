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

import org.agrona.DirectBuffer;
import org.tools4j.hoverraft.server.Server;

public enum MessageType {
    VOTE_REQUEST {

        private final VoteRequest voteRequest = new VoteRequest();

        @Override
        protected int messageSize() {
            return VoteRequest.MESSAGE_SIZE;
        }

        @Override
        protected void accept(final Server server,
                              final DirectBuffer buffer,
                              final int offset,
                              final int length,
                              final MessageHandler messageHandler) {
            voteRequest.wrap(buffer, offset, length);
            messageHandler.onVoteRequest(server, voteRequest);
        }
    },
    VOTE_RESPONSE {

        private final VoteResponse voteResponse = new VoteResponse();

        @Override
        protected int messageSize() {
            return VoteResponse.MESSAGE_SIZE;
        }

        @Override
        protected void accept(final Server server,
                              final DirectBuffer buffer,
                              final int offset,
                              final int length,
                              final MessageHandler messageHandler) {
            voteResponse.wrap(buffer, offset, length);
            messageHandler.onVoteResponse(server, voteResponse);
        }
    },
    APPEND_REQUEST {

        private final AppendRequest appendRequest = new AppendRequest();

        @Override
        protected int messageSize() {
            return AppendRequest.MESSAGE_SIZE;
        }

        @Override
        protected void accept(final Server server,
                              final DirectBuffer buffer,
                              final int offset,
                              final int length,
                              final MessageHandler messageHandler) {
            appendRequest.wrap(buffer, offset, length);
            messageHandler.onAppendRequest(server, appendRequest);
        }
    },
    APPEND_RESPONSE {

        private final AppendResponse appendResponse = new AppendResponse();

        @Override
        protected int messageSize() {
            return AppendResponse.MESSAGE_SIZE;
        }

        @Override
        protected void accept(final Server server,
                              final DirectBuffer buffer,
                              final int offset,
                              final int length,
                              final MessageHandler messageHandler) {
            appendResponse.wrap(buffer, offset, length);
            messageHandler.onAppendResponse(server, appendResponse);
        }
    },
    TIMEOUT_REQUEST {

        private final TimeoutNow timeoutNow = new TimeoutNow();

        @Override
        protected int messageSize() {
            return TimeoutNow.MESSAGE_SIZE;
        }

        @Override
        protected void accept(final Server server,
                              final DirectBuffer buffer,
                              final int offset,
                              final int length,
                              final MessageHandler messageHandler) {
            timeoutNow.wrap(buffer, offset, length);
            messageHandler.onTimeoutNow(server, timeoutNow);
        }
    };

    private static final MessageType[] VALUES = values();

    public static final MessageType valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    public static boolean dispatch(final Server server,
                                   final DirectBuffer buffer,
                                   final int offset,
                                   final int length,
                                   final MessageHandler messageHandler) {
        if (length >=4 ) {
            final int type = buffer.getInt(offset);
            if (0 <= type & type <= VALUES.length) {
                valueByOrdinal(type).dispatch(server, buffer, offset + 4, length - 4, messageHandler);
                return true;
            }
        }
        return false;
    }

    public static int maxSize() {
        int maxSize = 0;
        for (final MessageType type : VALUES) {
            maxSize = Math.max(maxSize, type.messageSize());
        }
        return maxSize;
    }

    abstract protected int messageSize();

    abstract protected void accept(Server server,
                                   DirectBuffer buffer,
                                   int offset,
                                   int length,
                                   MessageHandler messageHandler);
}
