package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DirectTimeoutNowTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectTimeoutNow.BYTE_LENGTH);
    private final DirectTimeoutNow directTimeoutNow = new DirectTimeoutNow();

    @Before
    public void init() {
        directTimeoutNow.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 9;
        final int candidateId = 5;

        //when
        directTimeoutNow.term(term).candidateId(candidateId);

        //then
        assertThat(directTimeoutNow.type()).isEqualTo(MessageType.TIMEOUT_NOW);
        assertThat(directTimeoutNow.term()).isEqualTo(term);
        assertThat(directTimeoutNow.candidateId()).isEqualTo(candidateId);
        assertThat(directTimeoutNow.byteLength()).isEqualTo(DirectTimeoutNow.BYTE_LENGTH);


    }
}