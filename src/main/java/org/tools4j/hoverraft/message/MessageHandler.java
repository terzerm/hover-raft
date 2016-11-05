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

import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.tools4j.hoverraft.server.Server;

import java.util.function.BiConsumer;

public interface MessageHandler {
    void onVoteRequest(Server server, VoteRequest voteRequest);
    void onVoteResponse(Server server, VoteResponse voteResponse);
    void onAppendRequest(Server server, AppendRequest appendRequest);
    void onAppendResponse(Server server, AppendResponse appendResponse);
    void onTimeoutNow(Server server, TimeoutNow timeoutNow);
    void onCommandMessage(Server server, CommandMessage commandMessage);

    MessageHandler NOOP = new MessageHandler() {
        @Override
        public void onVoteRequest(Server server, VoteRequest voteRequest) {
            // no op
        }

        @Override
        public void onVoteResponse(Server server, VoteResponse voteResponse) {
            // no op
        }

        @Override
        public void onAppendRequest(Server server, AppendRequest appendRequest) {
            // no op
        }

        @Override
        public void onAppendResponse(Server server, AppendResponse appendResponse) {
            // no op
        }

        @Override
        public void onTimeoutNow(Server server, TimeoutNow timeoutRequest) {
            // no op
        }

        @Override
        public void onCommandMessage(Server server, CommandMessage commandMessage) {
            // no op
        }
    };

    static MessageHandler handle(final BiConsumer<Server, VoteRequest> voteRequestHandler,
                                 final BiConsumer<Server, VoteResponse> voteResponseHandler,
                                 final BiConsumer<Server, AppendRequest> appendRequestHandler,
                                 final BiConsumer<Server, AppendResponse> appendResponseHandler,
                                 final BiConsumer<Server, TimeoutNow> timeoutNowHandler,
                                 final BiConsumer<Server, CommandMessage> commandMessageHandler) {
        return new MessageHandler() {
            @Override
            public void onVoteRequest(final Server server, final VoteRequest voteRequest) {
                voteRequestHandler.accept(server, voteRequest);
            }

            @Override
            public void onVoteResponse(final Server server, final VoteResponse voteResponse) {
                voteResponseHandler.accept(server, voteResponse);
            }

            @Override
            public void onAppendRequest(final Server server, final AppendRequest appendRequest) {
                appendRequestHandler.accept(server, appendRequest);
            }

            @Override
            public void onAppendResponse(final Server server, final AppendResponse appendResponse) {
                appendResponseHandler.accept(server, appendResponse);
            }

            @Override
            public void onTimeoutNow(final Server server, final TimeoutNow timeoutNow) {
                timeoutNowHandler.accept(server, timeoutNow);
            }

            @Override
            public void onCommandMessage(Server server, CommandMessage commandMessage) {
                commandMessageHandler.accept(server, commandMessage);
            }

            @Override
            public MessageHandler thenHandleVoteRequest(final BiConsumer<Server, VoteRequest> handler) {
                return handle(voteRequestHandler.andThen(handler), voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleVoteResponse(final BiConsumer<Server, VoteResponse> handler) {
                return handle(voteRequestHandler, voteResponseHandler.andThen(handler), appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleAppendRequest(final BiConsumer<Server, AppendRequest> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler.andThen(handler), appendResponseHandler, timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleAppendResponse(final BiConsumer<Server, AppendResponse> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler.andThen(handler), timeoutNowHandler, commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleTimeoutNow(final BiConsumer<Server, TimeoutNow> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler.andThen(handler), commandMessageHandler);
            }

            @Override
            public MessageHandler thenHandleCommandMessage(final BiConsumer<Server, CommandMessage> handler) {
                return handle(voteRequestHandler, voteResponseHandler, appendRequestHandler, appendResponseHandler, timeoutNowHandler, commandMessageHandler.andThen(handler));
            }
        };
    }

    static MessageHandler handleVoteRequest(final BiConsumer<Server, VoteRequest> handler) {
        return handle(handler, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleVoteResponse(final BiConsumer<Server, VoteResponse> handler) {
        return handle(NOOP::onVoteRequest, handler, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleAppendRequest(final BiConsumer<Server, AppendRequest> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, handler, NOOP::onAppendResponse, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleAppendResponse(final BiConsumer<Server, AppendResponse> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, handler, NOOP::onTimeoutNow, NOOP::onCommandMessage);
    }

    static MessageHandler handleTimeoutNow(final BiConsumer<Server, TimeoutNow> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, handler, NOOP::onCommandMessage);
    }

    static MessageHandler handleCommandMessage(final BiConsumer<Server, CommandMessage> handler) {
        return handle(NOOP::onVoteRequest, NOOP::onVoteResponse, NOOP::onAppendRequest, NOOP::onAppendResponse, NOOP::onTimeoutNow, handler);
    }

    default MessageHandler thenHandleVoteRequest(final BiConsumer<Server, VoteRequest> handler) {
        return handle(((BiConsumer<Server, VoteRequest>)this::onVoteRequest).andThen(handler), this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleVoteResponse(final BiConsumer<Server, VoteResponse> handler) {
        return handle(this::onVoteRequest, ((BiConsumer<Server, VoteResponse>)this::onVoteResponse).andThen(handler), this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleAppendRequest(final BiConsumer<Server, AppendRequest> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, ((BiConsumer<Server, AppendRequest>)this::onAppendRequest).andThen(handler), this::onAppendResponse, this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleAppendResponse(final BiConsumer<Server, AppendResponse> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, ((BiConsumer<Server, AppendResponse>)this::onAppendResponse).andThen(handler), this::onTimeoutNow, this::onCommandMessage);
    }

    default MessageHandler thenHandleTimeoutNow(final BiConsumer<Server, TimeoutNow> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, ((BiConsumer<Server, TimeoutNow>)this::onTimeoutNow).andThen(handler), this::onCommandMessage);
    }

    default MessageHandler thenHandleCommandMessage(final BiConsumer<Server, CommandMessage> handler) {
        return handle(this::onVoteRequest, this::onVoteResponse, this::onAppendRequest, this::onAppendResponse, this::onTimeoutNow, ((BiConsumer<Server, CommandMessage>)this::onCommandMessage).andThen(handler));
    }

    default MessageHandler thenHandle(final MessageHandler next) {
        return new MessageHandler() {
            @Override
            public void onVoteRequest(Server server, VoteRequest voteRequest) {
                MessageHandler.this.onVoteRequest(server, voteRequest);
                next.onVoteRequest(server, voteRequest);
            }

            @Override
            public void onVoteResponse(Server server, VoteResponse voteResponse) {
                MessageHandler.this.onVoteResponse(server, voteResponse);
                next.onVoteResponse(server, voteResponse);
            }

            @Override
            public void onAppendRequest(Server server, AppendRequest appendRequest) {
                MessageHandler.this.onAppendRequest(server, appendRequest);
                next.onAppendRequest(server, appendRequest);
            }

            @Override
            public void onAppendResponse(Server server, AppendResponse appendResponse) {
                MessageHandler.this.onAppendResponse(server, appendResponse);
                next.onAppendResponse(server, appendResponse);
            }

            @Override
            public void onTimeoutNow(Server server, TimeoutNow timeoutNow) {
                MessageHandler.this.onTimeoutNow(server, timeoutNow);
                next.onTimeoutNow(server, timeoutNow);
            }

            @Override
            public void onCommandMessage(Server server, CommandMessage commandMessage) {
                MessageHandler.this.onCommandMessage(server, commandMessage);
                next.onCommandMessage(server, commandMessage);
            }
        };
    }
}
