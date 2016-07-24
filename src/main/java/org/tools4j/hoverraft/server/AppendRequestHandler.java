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

import io.aeron.Publication;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.state.VolatileState;

public final class AppendRequestHandler {

    private static final int MAX_TRIES = 10;//TODO how often should we retry sending?

    public void onAppendRequest(final Server server, final AppendRequest appendRequest) {
        final int term = appendRequest.term();
        final int leaderId = appendRequest.leaderId();
        final boolean successful;
        if (server.currentTerm() == term) /* should never be larger */ {
            final VolatileState vstate = server.state().volatileState();
            if (vstate.role() == Role.FOLLOWER) {
                vstate.electionState().electionTimer().reset();
            } else {
                vstate.changeRoleTo(Role.FOLLOWER);
                vstate.electionState().electionTimer().restart();
            }
            successful = appendToLog(server, appendRequest);
        } else {
            successful = false;
        }
        final Publication serverPublication = server.connections().serverPublication(leaderId);
        final DirectMessage message = server.messageFactory().appendResponse()
                .term(server.currentTerm())
                .successful(successful);
        server.send(serverPublication, message);
    }

    private boolean appendToLog(final Server server, final AppendRequest appendRequest) {
        //FIXME append log entries here
        return false;
    }
}
