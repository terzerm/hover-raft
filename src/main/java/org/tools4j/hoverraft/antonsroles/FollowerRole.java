package org.tools4j.hoverraft.antonsroles;

import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.message.VoteResponse;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * - Respond to RPCs from candidates and leaders
 * - If election timeout elapses without receiving AppendEntries
 *   RPC from current leader or granting onVoteRequest to candidate:
 *   convert to candidate
 */
public class FollowerRole implements Role {
    private final Consumer<VoteResponse> voteResponsePublisher;
    private final Consumer<AppendResponse> appendResponsePublisher;

    public FollowerRole(Consumer<VoteResponse> voteResponsePublisher, Consumer<AppendResponse> appendResponsePublisher) {
        this.voteResponsePublisher = Objects.requireNonNull(voteResponsePublisher);
        this.appendResponsePublisher = Objects.requireNonNull(appendResponsePublisher);
    }

    @Override
    public Role onTransitionTo(final ServerContext serverContext) {
        return this;
    }

    @Override
    public Role onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        return this;
    }

    @Override
    public Role onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
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
