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
package org.tools4j.hoverraft.message;

import org.agrona.DirectBuffer;
import org.tools4j.hoverraft.server.Server;

public enum MessageType {
    VOTE_REQUEST {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.voteRequest();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onVoteRequest(server, messageFactory.voteRequest());
        }
    },
    VOTE_RESPONSE {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.voteResponse();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onVoteResponse(server, messageFactory.voteResponse());
        }
    },
    APPEND_REQUEST {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.appendRequest();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onAppendRequest(server, messageFactory.appendRequest());
        }
    },
    APPEND_RESPONSE {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.appendResponse();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onAppendResponse(server, messageFactory.appendResponse());
        }
    },
    TIMEOUT_NOW {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.timeoutNow();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onTimeoutNow(server, messageFactory.timeoutNow());
        }
    },
    COMMAND_MESSAGE {
        @Override
        public Message create(final MessageFactory factory) {
            return factory.commandMessage();
        }

        @Override
        protected void accept(final Server server,
                              final MessageFactory messageFactory,
                              final MessageHandler messageHandler) {
            messageHandler.onCommandMessage(server, messageFactory.commandMessage());
        }
    };


    private static final MessageType[] VALUES = values();

    public static final MessageType valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    public static final int maxOrdinal() {
        return VALUES.length - 1;
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

    abstract public Message create(MessageFactory factory);

    abstract protected void accept(Server server,
                                   MessageFactory messageFactory,
                                   MessageHandler messageHandler);

}
