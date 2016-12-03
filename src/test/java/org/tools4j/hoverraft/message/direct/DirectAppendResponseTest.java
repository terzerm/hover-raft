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
        final int serverId = 5;
        final long matchLogIndex = 564564;

        //when
        directAppendResponse
                .term(term)
                .successful(sucessful)
                .serverId(serverId)
                .matchLogIndex(matchLogIndex);

        //then
        assertThat(directAppendResponse.type()).isEqualTo(MessageType.APPEND_RESPONSE);
        assertThat(directAppendResponse.term()).isEqualTo(term);
        assertThat(directAppendResponse.successful()).isEqualTo(sucessful);
        assertThat(directAppendResponse.serverId()).isEqualTo(serverId);
        assertThat(directAppendResponse.matchLogIndex()).isEqualTo(matchLogIndex);
        assertThat(directAppendResponse.byteLength()).isEqualTo(DirectAppendResponse.BYTE_LENGTH);
    }
}