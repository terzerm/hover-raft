package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectLogKeyTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectLogKey.BYTE_LENGTH);
    private final DirectLogKey directLogKey = new DirectLogKey();

    @Before
    public void init() {
        directLogKey.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 9;
        final long index = 34766;

        //when
        directLogKey.term(term).index(index);

        //then
        assertThat(directLogKey.term()).isEqualTo(term);
        assertThat(directLogKey.index()).isEqualTo(index);
        assertThat(directLogKey.byteLength()).isEqualTo(DirectLogKey.BYTE_LENGTH);
    }
}