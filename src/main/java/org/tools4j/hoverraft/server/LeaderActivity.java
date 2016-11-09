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
package org.tools4j.hoverraft.server;

import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.MessageHandler;

public class LeaderActivity implements ServerActivity {

    private final MessageHandler messageHandler = new HigherTermHandler()
            .thenHandleVoteRequest(new VoteRequestHandler()::onVoteRequest)
            .thenHandleAppendRequest(new AppendRequestHandler()::onAppendRequest)
            .thenHandleAppendResponse(this::handleAppendResponse)
            .thenHandleCommandMessage(this::handleCommandMessage);

    @Override
    public MessageHandler messageHandler() {
        return messageHandler;
    }

    @Override
    public void perform(final ServerContext serverContext) {
        updateCommitIndex(serverContext);
        sendAppendRequest(serverContext);
    }

    private void handleCommandMessage(final ServerContext serverContext, final CommandMessage commandMessage) {
        serverContext.messageLog().append(commandMessage);
    }

    private void updateCommitIndex(final ServerContext serverContext) {
        //FIXME impl
    }

    private void sendAppendRequest(final ServerContext serverContext) {
        //FIXME impl
    }

    private void handleAppendResponse(final ServerContext serverContext, final AppendResponse appendResponse) {
        //FIXME impl
    }
}
