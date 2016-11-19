package org.tools4j.hoverraft.event;

import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.*;

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

        serverContext.messageFactory().appendResponse()
                .term(currentTerm)
                .successful(successful)
                .sendTo(serverContext.connections().serverSender(leaderId),
                        serverContext.resendStrategy());

        return transition;
    }

    private boolean appendToLog(final ServerContext serverContext, final AppendRequest appendRequest) {
        final LogEntry prevLogEntry = appendRequest.prevLogEntry();
        final CommandLogEntry newCommandLogEntry = appendRequest.commandLogEntry();
        final CommandLog commandLog = persistentState.commandLog();

        CommandLog.CONTAINMENT containment =  commandLog.contains(prevLogEntry);

        switch(containment) {
            case OUT: return false;
            case CONFLICT:
                commandLog.truncate(prevLogEntry.index());
                return false;
            case IN:
                //Append any new entries not already in the log
                //FIXme need to enable null appendRequest.commandLogEntry()
                commandLog.append(newCommandLogEntry);

                if (appendRequest.leaderCommit() > volatileState.commitIndex()) {
                    volatileState.commitIndex(Long.min(appendRequest.leaderCommit(), newCommandLogEntry.index()));
                }
                return true;
            default:
                throw new IllegalStateException("Unknown containment");
        }
    }

}
