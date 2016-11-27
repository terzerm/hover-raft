/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 hover-raft (tools4j), Marco Terzer
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
                .lastLogKey()
                    .term(lastLogTerm)
                    .index(lastLogIndex);

        //then
        assertThat(directVoteRequest.type()).isEqualTo(MessageType.VOTE_REQUEST);
        assertThat(directVoteRequest.term()).isEqualTo(term);
        assertThat(directVoteRequest.candidateId()).isEqualTo(candidateId);

        final int retrievedLastLogTerm = directVoteRequest.lastLogKey().term();
        final long retrievedLastLogIndex = directVoteRequest.lastLogKey().index();

        assertThat(retrievedLastLogTerm).isEqualTo(lastLogTerm);
        assertThat(retrievedLastLogIndex).isEqualTo(lastLogIndex);

        assertThat(directVoteRequest.byteLength()).isEqualTo(DirectVoteRequest.BYTE_LENGTH);
    }
}