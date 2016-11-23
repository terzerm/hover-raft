package org.tools4j.hoverraft.message.inmemory;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.message.direct.*;
import org.tools4j.hoverraft.state.CommandLogEntry;
import org.tools4j.hoverraft.state.direct.DirectCommandLogEntry;

public class InMemoryMessageFactory implements MessageFactory {

    private MutableDirectBuffer newBuffer(final int initialCapacity) {
        return new ExpandableArrayBuffer(initialCapacity);
    }

    @Override
    public AppendRequest appendRequest() {
        final DirectAppendRequest directAppendRequest = new DirectAppendRequest();
        directAppendRequest.wrap(newBuffer(DirectAppendRequest.BYTE_LENGTH), 0);
        return directAppendRequest;
    }

    @Override
    public AppendResponse appendResponse() {
        final DirectAppendResponse directAppendResponse = new DirectAppendResponse();
        directAppendResponse.wrap(newBuffer(DirectAppendResponse.BYTE_LENGTH), 0);
        return directAppendResponse;
    }

    @Override
    public VoteRequest voteRequest() {
        final DirectVoteRequest directVoteRequest = new DirectVoteRequest();
        directVoteRequest.wrap(newBuffer(DirectVoteRequest.BYTE_LENGTH), 0);
        return directVoteRequest;
    }

    @Override
    public VoteResponse voteResponse() {
        final DirectVoteResponse directVoteResponse = new DirectVoteResponse();
        directVoteResponse.wrap(newBuffer(DirectVoteResponse.BYTE_LENGTH), 0);
        return directVoteResponse;
    }

    @Override
    public TimeoutNow timeoutNow() {
        final DirectTimeoutNow directTimeoutNow = new DirectTimeoutNow();
        directTimeoutNow.wrap(newBuffer(DirectTimeoutNow.BYTE_LENGTH), 0);
        return null;
    }

    @Override
    public CommandMessage commandMessage() {
        final DirectCommandMessage directCommandMessage = new DirectCommandMessage();
        directCommandMessage.wrap(newBuffer(DirectCommandMessage.EMPTY_COMMAND_BYTE_LENGTH), 0);
        return directCommandMessage;
    }

    @Override
    public CommandLogEntry commandLogEntry() {
        final DirectCommandLogEntry directCommandLogEntry = new DirectCommandLogEntry();
        directCommandLogEntry.wrap(newBuffer(DirectLogEntry.BYTE_LENGTH), 0);
        return directCommandLogEntry;
    }
}
