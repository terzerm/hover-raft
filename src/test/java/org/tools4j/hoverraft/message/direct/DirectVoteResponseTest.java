/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 hover-raft (tools4j), Marco Terzer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.MessageType;

import static org.assertj.core.api.Assertions.assertThat;

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