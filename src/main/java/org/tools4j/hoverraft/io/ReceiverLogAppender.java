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
package org.tools4j.hoverraft.io;

import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.transport.MessageLog;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;

/**
 * Reads messages from a {@link Receiver} and appends them to a {@link MessageLog} via
 * {@link #receiveAndAppendToLog(int)}.
 */
public final class ReceiverLogAppender {

    private final Receiver<DirectMessage> receiver;
    private final MessageLog<DirectMessage> messageLog;
    private final Receiver.Poller poller;

    public ReceiverLogAppender(final Receiver<DirectMessage> receiver, final MessageLog<DirectMessage> directMessageLog) {
        this.receiver = Objects.requireNonNull(receiver);
        this.messageLog = Objects.requireNonNull(directMessageLog);
        this.poller = receiver.poller(msg -> messageLog.append(msg));
    }

    public Receiver<DirectMessage> receiver() {
        return receiver;
    }

    public MessageLog<DirectMessage> messageLog() {
        return messageLog;
    }

    public int receiveAndAppendToLog(final int limit) {
        return poller.poll(limit);
    }
}
