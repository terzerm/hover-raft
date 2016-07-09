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

import java.util.Objects;

/**
 * Reads messages from a {@link Subscription} and appends them to a {@link MessageLog} via
 * {@link #pollSubscriptionAppendToLog(int)}.
 */
public final class SubscriptionPollerLogAppender {

    private final Subscription subscription;
    private final MessageLog messageLog;
    private final FragmentHandler subscriptionPollerLogAppender;

    public SubscriptionPollerLogAppender(final Subscription subscription, final MessageLog messageLog) {
        this.subscription = Objects.requireNonNull(subscription);
        this.messageLog = Objects.requireNonNull(messageLog);
        this.subscriptionPollerLogAppender = (buf, off, len, hdr) -> messageLog.append(buf, off, len);
    }

    public Subscription subscription() {
        return subscription;
    }

    public MessageLog messageLog() {
        return messageLog;
    }

    public int pollSubscriptionAppendToLog(int fragmentLimit) {
        return subscription().poll(subscriptionPollerLogAppender, fragmentLimit);
    }
}
