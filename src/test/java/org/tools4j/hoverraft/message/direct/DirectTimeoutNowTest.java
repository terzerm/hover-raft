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