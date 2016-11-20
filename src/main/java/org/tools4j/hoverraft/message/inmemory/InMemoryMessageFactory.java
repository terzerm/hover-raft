package org.tools4j.hoverraft.message.inmemory;

import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.state.CommandLogEntry;

public class InMemoryMessageFactory implements MessageFactory {
    @Override
    public AppendRequest appendRequest() {
        return null;
    }

    @Override
    public AppendResponse appendResponse() {
        return null;
    }

    @Override
    public VoteRequest voteRequest() {
        return null;
    }

    @Override
    public VoteResponse voteResponse() {
        return null;
    }

    @Override
    public TimeoutNow timeoutNow() {
        return null;
    }

    @Override
    public CommandMessage commandMessage() {
        return null;
    }

    @Override
    public CommandLogEntry commandLogEntry() {
        return null;
    }
}
