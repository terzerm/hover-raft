package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectAppendResponseTest {

    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectAppendResponse.BYTE_LENGTH);
    private final DirectAppendResponse directAppendResponse = new DirectAppendResponse();

    @Before
    public void init() {
        directAppendResponse.wrap(buffer, 0);
    }


    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 10;
        final boolean sucessful = true;

        //when
        directAppendResponse.term(term).successful(sucessful);

        //then
        assertThat(directAppendResponse.type()).isEqualTo(MessageType.APPEND_RESPONSE);
        assertThat(directAppendResponse.term()).isEqualTo(term);
        assertThat(directAppendResponse.successful()).isEqualTo(sucessful);
        assertThat(directAppendResponse.byteLength()).isEqualTo(DirectAppendResponse.BYTE_LENGTH);
    }
}