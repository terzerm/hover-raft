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

import org.tools4j.hoverraft.server.ServerContext;

import java.util.function.BiConsumer;

public interface MessageHandler {
    void onVoteRequest(ServerContext serverContext, VoteRequest voteRequest);
    void onVoteResponse(ServerContext serverContext, VoteResponse voteResponse);
    void onAppendRequest(ServerContext serverContext, AppendRequest appendRequest);
    void onAppendResponse(ServerContext serverContext, AppendResponse appendResponse);
    void onTimeoutNow(ServerContext serverContext, TimeoutNow timeoutNow);
    void onCommandMessage(ServerContext serverContext, CommandMessage commandMessage);

    MessageHandler NOOP = new MessageHandler() {
        @Override
        public void onVoteRequest(ServerContext serverContext, VoteRequest voteRequest) {
            // no op
        }

        @Override
        public void onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {
            // no op
        }

        @Override
        public void onAppendRequest(ServerContext serverContext, AppendRequest appendRequest) {
            // no op
        }

        @Override
        public void onAppendResponse(ServerContext serverContext, AppendResponse appendResponse) {
            // no op
        }

        @Override
        public void onTimeoutNow(ServerContext serverContext, TimeoutNow timeoutRequest) {
            // no op
        }

        @Override
        public void onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
            // no op
        }
    };

    static MessageHandler handle(final BiConsumer<ServerContext, VoteRequest> voteRequestHandler,
                                 final BiConsumer<ServerContext, VoteResponse> voteResponseHandler,
                                 final BiConsumer<ServerContext, AppendRequest> appendRequestHandler,
                                 final BiConsumer<ServerContext, AppendResponse> appendResponseHandler,
                                 final BiConsumer<ServerContext, TimeoutNow> timeoutNowHandler,
                                 final BiConsumer<ServerContext, CommandMessage> commandMessageHandler) {
        return new MessageHandler() {
            @Override
            public void onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
                voteRequestHandler.accept(serverContext, voteRequest);
            }

            @Override
            public void onVoteResponse(final ServerContext serverContext, final VoteResponse voteResponse) {
                voteResponseHandler.accept(serverContext, voteResponse);
            }

            @Override
            public void onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
                appendRequestHandler.accept(serverContext, appendRequest);
            }

            @Override
            public void onAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
                appendResponseHandler.accept(serverContext, appendResponse);
            }

            @Override
            public void onTimeoutNow(final ServerContext serverContext, final TimeoutNow timeoutNow) {
                timeoutNowHandler.accept(serverContext, timeoutNow);
            }

            @Override
            public void onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
                commandMessageHandler.accept(serverContext, commandMessage);
            }

            @Override
            public MessageHandler thenHandleVoteRequest(final BiConsumer<ServerContext, VoteRequest> handler) {
                return handle(voteRequestHandler.andThen(handler), voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleVoteResponse(final BiConsumer<ServerContext, VoteResponse> handler) {
                return handle(voteRequestHandler, voteResponseHandler.andThen(handler), appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleAppendRequest(final BiConsumer<ServerContext, AppendRequest> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler.andThen(handler), appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleAppendResponse(final BiConsumer<ServerContext, AppendResponse> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler.andThen(handler), timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleTimeoutNow(final BiConsumer<ServerContext, TimeoutNow> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler.andThen(handler), commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleCommandMessage(final BiConsumer<ServerContext, CommandMessage> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler.andThen(handler));
            }
        };
    }

    static MessageHandler handleVoteRequest(final BiConsumer<ServerContext, VoteRequest> handler) {
        return handle(handler, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleVoteResponse(final BiConsumer<ServerContext, VoteResponse> handler) {
        return handle(NOOP::onVoteRequest, handler, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleAppendRequest(final BiConsumer<ServerContext, AppendRequest> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, handler, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleAppendResponse(final BiConsumer<ServerContext, AppendResponse> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, handler, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleTimeoutNow(final BiConsumer<ServerContext, TimeoutNow> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, handler, NOOP::onCommandMessage);
    }

    static MessageHandler handleCommandMessage(final BiConsumer<ServerContext, CommandMessage> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, handler);
    }

    default MessageHandler thenHandleVoteRequest(final BiConsumer<ServerContext, VoteRequest> handler) {
        return handle(((BiConsumer<ServerContext, VoteRequest>)this::onVoteRequest).andThen(handler), this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleVoteResponse(final BiConsumer<ServerContext, VoteResponse> handler) {
        return handle(this::onVoteRequest, ((BiConsumer<ServerContext, VoteResponse>)this::onVoteResponse).andThen(handler), this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleAppendRequest(final BiConsumer<ServerContext, AppendRequest> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, ((BiConsumer<ServerContext, AppendRequest>)this::onAppendRequest).andThen(handler), this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleAppendResponse(final BiConsumer<ServerContext, AppendResponse> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, ((BiConsumer<ServerContext, AppendResponse>)this::onAppendResponse).andThen(handler), this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleTimeoutNow(final BiConsumer<ServerContext, TimeoutNow> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, ((BiConsumer<ServerContext, TimeoutNow>)this::onTimeoutNow).andThen(handler), this::onCommandMessage);
    }

    default MessageHandler thenHandleCommandMessage(final BiConsumer<ServerContext, CommandMessage> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, ((BiConsumer<ServerContext, CommandMessage>)this::onCommandMessage).andThen(handler));
    }

    default MessageHandler thenHandle(final MessageHandler next) {
        return new MessageHandler() {
            @Override
            public void onVoteRequest(ServerContext serverContext, VoteRequest voteRequest) {
                MessageHandler.this.onVoteRequest(serverContext, voteRequest);
                next.onVoteRequest(serverContext, voteRequest);
            }

            @Override
            public void onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {
                MessageHandler.this.onVoteResponse(serverContext, voteResponse);
                next.onVoteResponse(serverContext, voteResponse);
            }

            @Override
            public void onAppendRequest(ServerContext serverContext, AppendRequest appendRequest) {
                MessageHandler.this.onAppendRequest(serverContext, appendRequest);
                next.onAppendRequest(serverContext, appendRequest);
            }

            @Override
            public void onAppendResponse(ServerContext serverContext, AppendResponse appendResponse) {
                MessageHandler.this.onAppendResponse(serverContext, appendResponse);
                next.onAppendResponse(serverContext, appendResponse);
            }

            @Override
            public void onTimeoutNow(ServerContext serverContext, TimeoutNow timeoutNow) {
                MessageHandler.this.onTimeoutNow(serverContext, timeoutNow);
                next.onTimeoutNow(serverContext, timeoutNow);
            }

            @Override
            public void onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
                MessageHandler.this.onCommandMessage(serverContext, commandMessage);
                next.onCommandMessage(serverContext, commandMessage);
            }
        };
    }
}
