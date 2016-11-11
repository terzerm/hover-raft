package org.tools4j.hoverraft.antonsroles;

import org.tools4j.hoverraft.message.*;

public interface Role {
    Role onTransitionTo(ServerContext serverContext);

    default Role onAppendRequest(ServerContext serverContext, AppendRequest appendRequest) {
        return this;
    }
    default Role onAppendResponse(ServerContext serverContext, AppendResponse appendResponse) {
        return this;
    }
    default Role onVoteRequest(ServerContext serverContext, VoteRequest voteRequest) {
        return this;
    }
    default Role onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {
        return this;
    }
    default Role onTimedEvent(ServerContext serverContext) {
        return this;
    }
    default Role onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
        return this;
    }
}
