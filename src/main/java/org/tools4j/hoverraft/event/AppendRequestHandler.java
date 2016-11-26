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
package org.tools4j.hoverraft.event;

import org.tools4j.hoverraft.command.CommandLog;
import org.tools4j.hoverraft.command.LogEntry;
import org.tools4j.hoverraft.command.LogContainment;
import org.tools4j.hoverraft.command.LogKey;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Transition;
import org.tools4j.hoverraft.state.VolatileState;

import java.util.Objects;

public class AppendRequestHandler {
    private final PersistentState persistentState;
    private final VolatileState volatileState;

    public AppendRequestHandler(final PersistentState persistentState, final VolatileState volatileState) {
        this.persistentState = Objects.requireNonNull(persistentState);
        this.volatileState = Objects.requireNonNull(volatileState);
    }

    public Transition onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        final int appendRequestTerm = appendRequest.term();
        final int leaderId = appendRequest.leaderId();
        final int currentTerm = persistentState.currentTerm();
        final Transition transition;
        final boolean successful;
        final long matchLogEntryIndex;

        if (appendRequestTerm < currentTerm) {
            transition = Transition.STEADY;
            successful = false;
            matchLogEntryIndex = 0;
        } else {
            //appendRequestTerm == currentTerm, as HigherTermHandler would
            transition = Transition.STEADY;
            successful = appendToLog(serverContext, appendRequest);

            //assume that NULL command log entry will have 0 command log entry index
            matchLogEntryIndex = successful ? Long.max(appendRequest.logEntry().logKey().index(),
                    appendRequest.prevLogKey().index()) : 0;
        }

        serverContext.directFactory().appendResponse()
                .term(currentTerm)
                .successful(successful)
                .serverId(serverContext.serverConfig().id())
                .matchLogIndex(matchLogEntryIndex)
                .sendTo(serverContext.connections().serverSender(leaderId),
                        serverContext.resendStrategy());

        return transition;
    }

    private boolean appendToLog(final ServerContext serverContext, final AppendRequest appendRequest) {

        final LogKey prevLogEntry = appendRequest.prevLogKey();
        final LogEntry newLogEntry = appendRequest.logEntry();

        final CommandLog commandLog = persistentState.commandLog();

        final LogContainment containment = commandLog.contains(prevLogEntry);

        switch(containment) {
            case OUT: return false;
            case CONFLICT:
                commandLog.truncateIncluding(prevLogEntry.index());
                return false;
            case IN:
                //Append any new entries not already in the log
                //FIXme need to enable null appendRequest.logEntry()
                commandLog.append(newLogEntry);

                if (appendRequest.leaderCommit() > volatileState.commitIndex()) {
                    volatileState.commitIndex(Long.min(appendRequest.leaderCommit(), newLogEntry.logKey().index()));
                }
                return true;
            default:
                throw new IllegalStateException("Unknown containment");
        }
    }

}
