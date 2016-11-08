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
package org.tools4j.hoverraft.state;

import io.aeron.logbuffer.FragmentHandler;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.server.*;

public enum Role {
    CANDIDATE(new CandidateActivity()),
    LEADER(new LeaderActivity()),
    FOLLOWER(new FollowerActivity());

    private final ServerActivity serverActivity;

    Role(final ServerActivity serverActivity) {
        this.serverActivity = serverActivity;
    }

    public FragmentHandler toFragmentHandler(final ServerContext serverContext) {
        return (buf, off, len, hdr) -> {
            final DirectMessage message = MessageType.createOrNull(serverContext, buf, off, len);
            if (message != null) {
                message.accept(serverContext, serverActivity.messageHandler());
            } else {
                //TODO log or handle properly
                System.err.println("unsupported message data: offset=" + off + ", length=" + len + ", type=" + (len >= 4 ? buf.getInt(0) : -1));
            }
        };
    };

}
