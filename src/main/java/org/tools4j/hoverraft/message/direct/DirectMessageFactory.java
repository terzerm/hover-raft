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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.message.MessageFactory;
import org.tools4j.hoverraft.message.MessageType;

import java.nio.ByteBuffer;

/**
 * Factory for messages writing data into an internal readBuffer. Messages and readBuffer are reused accross
 * multiple calls.
 */
public final class DirectMessageFactory implements MessageFactory {

    private final DirectAppendRequest appendRequest = new DirectAppendRequest();
    private final DirectAppendResponse appendResponse = new DirectAppendResponse();
    private final DirectVoteRequest voteRequest = new DirectVoteRequest();
    private final DirectVoteResponse voteResponse = new DirectVoteResponse();
    private final DirectTimeoutNow timeoutNow = new DirectTimeoutNow();
    private final DirectCommandMessage commandMessage = new DirectCommandMessage();

    private DirectMessageFactory() {
        super();
    }

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

    public static DirectMessageFactory createForReading() {
        return new DirectMessageFactory();
    }

    public static DirectMessageFactory createForWriting() {
        final DirectMessageFactory factory = new DirectMessageFactory();
        int len = 0;
        len = Math.max(len, factory.appendRequest.byteLength());
        len = Math.max(len, factory.appendResponse.byteLength());
        len = Math.max(len, factory.voteRequest.byteLength());
        len = Math.max(len, factory.voteResponse.byteLength());
        len = Math.max(len, factory.timeoutNow.byteLength());
        len = Math.max(len, factory.commandMessage.byteLength());
        return createForWriting(new UnsafeBuffer(ByteBuffer.allocateDirect(len)), 0);
    }

    public static DirectMessageFactory createForWriting(final MutableDirectBuffer buffer, final int offset) {
        final DirectMessageFactory factory = new DirectMessageFactory();
        factory.wrapForWriting(buffer, offset);
        return factory;
    }

    public DirectMessage wrapForReading(final DirectBuffer directBuffer, final int offset) {
        final int type = directBuffer.getInt(offset);
        if (type >= 0 & type <= MessageType.maxOrdinal()) {
            final MessageType messageType = MessageType.valueByOrdinal(type);
            final DirectMessage message = (DirectMessage)messageType.create(this);
            message.wrap(directBuffer, offset);
            return message;
        }
        throw new IllegalArgumentException("Illegal message type: " + type);
    }

    public DirectMessageFactory wrapForWriting(final MutableDirectBuffer mutableDirectBuffer, final int offset) {
        appendRequest.wrap(mutableDirectBuffer, offset);
        appendResponse.wrap(mutableDirectBuffer, offset);
        voteRequest.wrap(mutableDirectBuffer, offset);
        voteResponse.wrap(mutableDirectBuffer, offset);
        timeoutNow.wrap(mutableDirectBuffer, offset);
        commandMessage.wrap(mutableDirectBuffer, offset);
        return this;
    }
}
