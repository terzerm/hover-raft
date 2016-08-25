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
package org.tools4j.hoverraft.message.simple;

import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.UserMessage;

public final class SimpleAppendRequest extends AbstractSimpleMessage implements AppendRequest {

    private int term;
    private int leaderId;
    private int prevLogTerm;
    private long prevLogIndex;
    private long leaderCommit;
    private UserMessage userMessage;

    @Override
    public MessageType type() {
        return MessageType.APPEND_REQUEST;
    }

    @Override
    public int term() {
        return term;
    }

    @Override
    public SimpleAppendRequest term(final int term) {
        this.term = term;
        return this;
    }

    @Override
    public int leaderId() {
        return leaderId;
    }

    @Override
    public SimpleAppendRequest leaderId(final int leaderId) {
        this.leaderId = leaderId;
        return this;
    }

    @Override
    public int prevLogTerm() {
        return prevLogTerm;
    }

    @Override
    public SimpleAppendRequest prevLogTerm(final int prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
        return this;
    }

    @Override
    public long prevLogIndex() {
        return prevLogIndex;
    }

    @Override
    public SimpleAppendRequest prevLogIndex(final long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
        return this;
    }

    @Override
    public long leaderCommit() {
        return leaderCommit;
    }

    @Override
    public SimpleAppendRequest leaderCommit(final long leaderCommit) {
        this.leaderCommit = leaderCommit;
        return this;
    }

    @Override
    public UserMessage userMessage() {
        return userMessage;
    }

    @Override
    public String toString() {
        return "SimpleAppendRequest{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", prevLogTerm=" + prevLogTerm +
                ", prevLogIndex=" + prevLogIndex +
                ", leaderCommit=" + leaderCommit +
                ", userMessage=" + userMessage +
                '}';
    }
}
