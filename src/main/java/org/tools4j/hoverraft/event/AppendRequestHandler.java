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
import org.tools4j.hoverraft.command.LogContainment;
import org.tools4j.hoverraft.command.LogEntry;
import org.tools4j.hoverraft.command.LogKey;
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Transition;
import org.tools4j.hoverraft.state.VolatileState;
import org.tools4j.hoverraft.util.MutatingIterator;

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

        if (appendRequestTerm < currentTerm) {
            transition = Transition.STEADY;
            successful = false;
        } else {
            //appendRequestTerm == currentTerm, as HigherTermHandler would
            transition = Transition.STEADY;
            successful = appendToLog(serverContext, appendRequest);
        }

        long matchLogIndex = -1;

        if (successful) {
            final MutatingIterator<LogEntry> logEntryIterator = appendRequest.logEntryIterator();
            final LogEntry lastLogEntry = serverContext.directFactory().logEntry();
            while(logEntryIterator.hasNext()) {
                logEntryIterator.next(lastLogEntry);
            }
            if (lastLogEntry.isWrapped()) {
                matchLogIndex = lastLogEntry.logKey().index();
            }
        }

        serverContext.directFactory().appendResponse()
                .term(currentTerm)
                .successful(successful)
                .serverId(serverContext.serverConfig().id())
                .matchLogIndex(matchLogIndex)
                .sendTo(serverContext.connections().serverSender(leaderId),
                        serverContext.resendStrategy());

        return transition;
    }

    private boolean appendToLog(final ServerContext serverContext, final AppendRequest appendRequest) {

        final LogKey prevLogKey = appendRequest.prevLogKey();

        final CommandLog commandLog = persistentState.commandLog();

        final LogContainment containment = commandLog.contains(prevLogKey);

        switch(containment) {
            case OUT: return false;
            case CONFLICT:
                commandLog.truncateIncluding(prevLogKey.index());
                return false;
            case IN:
                //Append any new entries not already in the log
                final MutatingIterator<LogEntry> logEntryIterator = appendRequest.logEntryIterator();
                final LogEntry logEntry = serverContext.directFactory().logEntry();
                while (logEntryIterator.hasNext()) {
                    logEntryIterator.next(logEntry);
                    commandLog.append(logEntry.logKey().term(), logEntry.command());
                }

                //From paper:  If leaderCommit > commitIndex, set commitIndex =
                // min(leaderCommit, index of last new entry).
                // I think, "index of last new entry" implies not empty log entries.
                if (logEntry.isWrapped() && appendRequest.leaderCommit() > volatileState.commitIndex()) {
                    volatileState.commitIndex(Long.min(appendRequest.leaderCommit(), logEntry.logKey().index()));
                }
                return true;
            default:
                throw new IllegalStateException("Unknown containment");
        }
    }

}
