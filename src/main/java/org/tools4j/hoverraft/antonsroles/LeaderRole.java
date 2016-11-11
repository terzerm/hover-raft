package org.tools4j.hoverraft.antonsroles;

import org.tools4j.hoverraft.message.*;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * • Upon election: send initial empty AppendEntries RPCs
 *   (heartbeat) to each server; repeat during idle periods to
 *   prevent election timeouts (§5.2)
 * • If command received from client: append entry to local log,
 *   respond after entry applied to state machine (§5.3)
 * • If last log index ≥ nextIndex for a follower: send
 *   AppendEntries RPC with log entries starting at nextIndex
 * • If successful: update nextIndex and matchIndex for
 *   follower (§5.3)
 * • If AppendEntries fails because of log inconsistency:
 *   decrement nextIndex and retry (§5.3)
 * • If there exists an N such that N > commitIndex, a majority
 * of matchIndex[i] ≥ N, and log[N].term == currentTerm:
 * set commitIndex = N (§5.3, §5.4).
 */
public class LeaderRole implements Role {
    private final Consumer<AppendRequest> appendRequestPublisher;

    public LeaderRole(final Consumer<AppendRequest> appendRequestPublisher) {
        this.appendRequestPublisher = Objects.requireNonNull(appendRequestPublisher);
    }


    @Override
    public Role onTransitionTo(final ServerContext serverContext) {
        //means election
        //send initial empty AppendEntries RPCs
        // *   (heartbeat) to each server
        //with appendRequestPublisher
        return this;
    }

    @Override
    public Role onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        return this;
    }

    @Override
    public Role onAppendResponse(ServerContext serverContext, AppendResponse appendResponse) {
        return this;
    }

    @Override
    public Role onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        return this;
    }

    @Override
    public Role onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {
        return this;
    }

    @Override
    public Role onTimedEvent(final ServerContext serverContext) {
        return this;
    }

    @Override
    public Role onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
        return this;
    }
}
