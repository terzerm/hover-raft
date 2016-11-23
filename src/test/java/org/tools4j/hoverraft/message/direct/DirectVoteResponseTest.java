package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DirectVoteResponseTest {

    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectVoteResponse.BYTE_LENGTH);
    private final DirectVoteResponse directVoteResponse = new DirectVoteResponse();

    @Before
    public void init() {
        directVoteResponse.wrap(buffer, 0);
    }


    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 10;
        final boolean voteGranted = true;

        //when
        directVoteResponse.term(term).voteGranted(voteGranted);

        //then
        assertThat(directVoteResponse.type()).isEqualTo(MessageType.VOTE_RESPONSE);
        assertThat(directVoteResponse.term()).isEqualTo(term);
        assertThat(directVoteResponse.voteGranted()).isEqualTo(voteGranted);
        assertThat(directVoteResponse.byteLength()).isEqualTo(DirectVoteResponse.BYTE_LENGTH);
    }
}