package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DirectVoteRequestTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectVoteRequest.BYTE_LENGTH);
    private final DirectVoteRequest directVoteRequest = new DirectVoteRequest();

    @Before
    public void init() {
        directVoteRequest.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 9;
        final int candidateId = 5;
        final int lastLogTerm = 8;
        final int lastLogIndex = 345244;

        //when
        directVoteRequest
                .term(term)
                .candidateId(candidateId)
                .lastLogEntry()
                    .term(lastLogTerm)
                    .index(lastLogIndex);

        //then
        assertThat(directVoteRequest.type()).isEqualTo(MessageType.VOTE_REQUEST);
        assertThat(directVoteRequest.term()).isEqualTo(term);
        assertThat(directVoteRequest.candidateId()).isEqualTo(candidateId);

        final int retrievedLastLogTerm = directVoteRequest.lastLogEntry().term();
        final long retrievedLastLogIndex = directVoteRequest.lastLogEntry().index();

        assertThat(retrievedLastLogTerm).isEqualTo(lastLogTerm);
        assertThat(retrievedLastLogIndex).isEqualTo(lastLogIndex);

        assertThat(directVoteRequest.byteLength()).isEqualTo(DirectVoteRequest.BYTE_LENGTH);
    }
}