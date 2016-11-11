package org.tools4j.hoverraft.antonsroles;

import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.state.ServerState;

import java.util.Objects;
import java.util.function.Consumer;

public class ServerContext {
    private final RoleProvider roleProvider;
    private final ServerState serverState;
    private volatile Role currentRole;

    public ServerContext(final RoleProvider roleProvider,
                         final ServerState serverState) {
        this.roleProvider = Objects.requireNonNull(roleProvider);
        this.serverState = Objects.requireNonNull(serverState);

        transitionToRole(roleProvider.getFollowerRole());
    }

    public RoleProvider getRoleProvider() {
        return roleProvider;
    }

    public ServerState serverState() {
        return serverState;
    }

    private void transitionToRole(final Role role) {
        if (role != this.currentRole) {
            this.currentRole = role;
            transitionToRole(this.currentRole.onTransitionTo(this));
        }
    }

    public Consumer<AppendRequest> consumerOfAppendRequest() {
        return appendRequest -> transitionToRole(currentRole.onAppendRequest(this, appendRequest));
    }

    public Consumer<VoteRequest> consumerOfVoteRequest() {
        return voteRequest -> transitionToRole(currentRole.onVoteRequest(this, voteRequest));
    }

    public Consumer<AppendResponse> consumerOfAppendResponse() {
        return appendResponse -> transitionToRole(currentRole.onAppendResponse(this, appendResponse));
    }

    public Consumer<VoteResponse> consumerOfVoteResponse() {
        return voteResponse -> transitionToRole(currentRole.onVoteResponse(this, voteResponse));
    }

}
