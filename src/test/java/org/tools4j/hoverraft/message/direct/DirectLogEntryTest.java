package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DirectLogEntryTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectLogEntry.BYTE_LENGTH);
    private final DirectLogEntry directLogEntry = new DirectLogEntry();

    @Before
    public void init() {
        directLogEntry.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 9;
        final long index = 34766;

        //when
        directLogEntry.term(term).index(index);

        //then
        assertThat(directLogEntry.term()).isEqualTo(term);
        assertThat(directLogEntry.index()).isEqualTo(index);
        assertThat(directLogEntry.byteLength()).isEqualTo(DirectLogEntry.BYTE_LENGTH);
    }
}