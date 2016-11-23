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

import org.tools4j.hoverraft.direct.AllocatingDirectFactory;
import org.tools4j.hoverraft.direct.RecyclingDirectFactory;

public enum MessageType {
    VOTE_REQUEST {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.voteRequest();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.voteRequest();
        }
    },
    VOTE_RESPONSE {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.voteResponse();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.voteResponse();
        }
    },
    APPEND_REQUEST {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.appendRequest();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.appendRequest();
        }
    },
    APPEND_RESPONSE {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.appendResponse();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.appendResponse();
        }
    },
    TIMEOUT_NOW {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.timeoutNow();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.timeoutNow();
        }
    },
    COMMAND_MESSAGE {
        @Override
        public Message create(final AllocatingDirectFactory factory) {
            return factory.commandMessage();
        }
        @Override
        public Message create(final RecyclingDirectFactory factory) {
            return factory.commandMessage();
        }
    };

    private static final MessageType[] VALUES = values();

    public static final MessageType valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    public static final int maxOrdinal() {
        return VALUES.length - 1;
    }

    abstract public Message create(AllocatingDirectFactory factory);

    abstract public Message create(RecyclingDirectFactory factory);

// It appears to be obsolete code, which requires ServerContext to provide RecyclingDirectFactory
// instead of DirectFactory.
//    public static Message createOrNull(final ServerContext serverContext,
//                                             final DirectBuffer buffer,
//                                             final int offset,
//                                             final int length) {
//        if (length >= 4) {
//            final int type = buffer.getInt(offset);
//            if (0 <= type & type <= VALUES.length) {
//                final Message message = valueByOrdinal(type).create(serverContext.messageFactory());
//                message.wrap(buffer, offset + 4);
//                return message;
//            }
//        }
//        return null;
//    }

}
