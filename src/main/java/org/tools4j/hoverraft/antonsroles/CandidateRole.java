package org.tools4j.hoverraft.antonsroles;

import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.util.Clock;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * • On conversion to candidate, start election:
 *      • Increment currentTerm
 *      • Vote for self
 *      • Reset election timer
 *      • Send RequestVote RPCs to all other servers
 * • If votes received from majority of servers: become leader
 * • If AppendEntries RPC received from new leader: convert to
 * follower
 * • If election timeout elapses: start new election
 */
public class CandidateRole implements Role {
    private final Consumer<VoteRequest> voteRequestPublisher;
    private final Consumer<AppendResponse> appendResponsePublisher;
    private final Consumer<VoteResponse> voteResponsePublisher;

    public CandidateRole(final Consumer<VoteRequest> voteRequestPublisher,
                         final Consumer<AppendResponse> appendResponsePublisher,
                         final Consumer<VoteResponse> voteResponsePublisher) {
        this.voteRequestPublisher = Objects.requireNonNull(voteRequestPublisher);
        this.appendResponsePublisher = Objects.requireNonNull(appendResponsePublisher);
        this.voteResponsePublisher = Objects.requireNonNull(voteResponsePublisher);
    }

    @Override
    public Role onTransitionTo(final ServerContext serverContext) {
        return startElection(serverContext);
    }

    private Role startElection(final ServerContext serverContext) {
        serverContext.serverState().persistentState().clearVotedForAndIncCurrentTerm();
        serverContext.serverState().volatileState().electionState().initVoteCount();
        serverContext.serverState().volatileState().electionState().electionTimer().restart(Clock.DEFAULT);

        //Create voteRequest possibly for each server
        //and publsh them with
        //for each voteRequest voteRequestPublisher.accept(voteRequest);

//        serverContext.messageFactory().voteRe()
//                .term(serverContext.currentTerm())
//                .voteGranted(granted)
//                .sendTo(serverContext.connections().serverSender(candidateId),
//                        serverContext.resendStrategy());
        return this;
    }

    @Override
    public Role onAppendRequest(final ServerContext serverContext, final AppendRequest appendRequest) {
        //If AppendEntries RPC received from new leader: convert to
        //* follower
        //create and return voteResponse with appendResponsePublisher
        return this;
    }

    @Override
    public Role onVoteRequest(final ServerContext serverContext, final VoteRequest voteRequest) {
        return this;
    }

    @Override
    public Role onVoteResponse(ServerContext serverContext, VoteResponse voteResponse) {
        //If votes received from majority of servers: become leader
        //
        return this;
    }

    @Override
    public Role onTimedEvent(final ServerContext serverContext) {
        //If election timeout elapses: start new election
        return startElection(serverContext);
    }

    @Override
    public Role onCommandMessage(ServerContext serverContext, CommandMessage commandMessage) {
        return this;
    }
}
