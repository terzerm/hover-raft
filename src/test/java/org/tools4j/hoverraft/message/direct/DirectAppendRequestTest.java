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

public class DirectAppendRequestTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectAppendRequest.BYTE_LENGTH);
    private final DirectAppendRequest directAppendRequest = new DirectAppendRequest();

    @Before
    public void init() {
        directAppendRequest.wrap(buffer, 0);
    }


    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {

        //given
        final int term = 10;
        final int leaderId = 2;
        final long leaderCommit = 34564;
        final int prevLogEntryTerm = 8;
        final long prevLogEntryIndex = 34765;
        final int newEntryTerm = 9;
        final long newEntryIndex = 34766;
        final long newEntryCommandIndex = 45674;
        final int newEntryCommandSourceId = 34;

        final String myCommand = "Command: XXXXXX";
        final byte[] commandBytes = myCommand.getBytes();

        //when
        directAppendRequest
                .term(term)
                .leaderId(leaderId)
                .leaderCommit(leaderCommit)
                .prevLogKey().term(prevLogEntryTerm)
                                .index(prevLogEntryIndex);
        directAppendRequest.logEntry().logKey()
                .term(newEntryTerm)
                .index(newEntryIndex);
        directAppendRequest.logEntry()
                .commandMessage()
                    .commandIndex(newEntryCommandIndex)
                    .commandSourceId(newEntryCommandSourceId)
                    .command().bytesFrom(commandBytes, 0, commandBytes.length);

        // then
        assertThat(directAppendRequest.type()).isEqualTo(MessageType.APPEND_REQUEST);
        assertThat(directAppendRequest.term()).isEqualTo(term);
        assertThat(directAppendRequest.leaderId()).isEqualTo(leaderId);
        assertThat(directAppendRequest.leaderCommit()).isEqualTo(leaderCommit);


        final int retrievedPrevLogEntryTerm = directAppendRequest.prevLogKey().term();
        final long retrievedPrevLogEntryIndex = directAppendRequest.prevLogKey().index();

        assertThat(retrievedPrevLogEntryTerm).isEqualTo(prevLogEntryTerm);
        assertThat(retrievedPrevLogEntryIndex).isEqualTo(prevLogEntryIndex);


        final int retrievedCommandLogEntryTerm = directAppendRequest.logEntry().logKey().term();
        final long retrievedCommandLogEntryIndex = directAppendRequest.logEntry().logKey().index();

        assertThat(retrievedCommandLogEntryTerm).isEqualTo(newEntryTerm);
        assertThat(retrievedCommandLogEntryIndex).isEqualTo(newEntryIndex);

        final int retrievedCommandSourceId = directAppendRequest.logEntry().commandMessage().commandSourceId();
        final long retrieveCommandIndex = directAppendRequest.logEntry().commandMessage().commandIndex();
        final int retrievedCommandByteLength = directAppendRequest.logEntry().commandMessage().command().byteLength();

        assertThat(retrievedCommandSourceId).isEqualTo(newEntryCommandSourceId);
        assertThat(retrieveCommandIndex).isEqualTo(newEntryCommandIndex);
        assertThat(retrievedCommandByteLength).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[retrievedCommandByteLength];
        directAppendRequest.logEntry().commandMessage().command().bytesTo(retrievedCommandBytes, 0);

        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);

        final int extectedAppendRequestBytes = DirectAppendRequest.BYTE_LENGTH +
                DirectLogKey.BYTE_LENGTH + DirectCommandMessage.EMPTY_COMMAND_BYTE_LENGTH +
                commandBytes.length;

        assertThat(directAppendRequest.byteLength()).isEqualTo(extectedAppendRequestBytes);


    }
}