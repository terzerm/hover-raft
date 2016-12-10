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
import org.tools4j.hoverraft.command.DirectLogEntry;
import org.tools4j.hoverraft.command.LogEntry;
import org.tools4j.hoverraft.direct.AllocatingDirectFactory;
import org.tools4j.hoverraft.direct.DirectFactory;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.util.MutatingIterator;


import static org.assertj.core.api.Assertions.assertThat;

public class DirectAppendRequestTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectAppendRequest.EMPTY_LOG_BYTE_LENGTH);
    private final DirectAppendRequest directAppendRequest = new DirectAppendRequest();
    private final DirectFactory directFactory = new AllocatingDirectFactory();

    private final LogEntry logEntry1 = directFactory.logEntry();
    private final LogEntry logEntry2 = directFactory.logEntry();
    private final LogEntry iteratingLogEntry = directFactory.logEntry();


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

        final int newEntryTerm1 = 9;
        final long newEntryIndex1 = 34766;
        final long newEntryCommandIndex1 = 45674;
        final int newEntryCommandSourceId1 = 34;

        final String myCommand1 = "Command: XXXXXX";
        final byte[] commandBytes1 = myCommand1.getBytes();

        final int newEntryTerm2 = 10;
        final long newEntryIndex2 = 34767;
        final long newEntryCommandIndex2 = 45675;
        final int newEntryCommandSourceId2 = 34;

        final String myCommand2 = "Command: YYYYYYYYYYY";
        final byte[] commandBytes2 = myCommand2.getBytes();

        //when
        directAppendRequest
                .term(term)
                .leaderId(leaderId)
                .leaderCommit(leaderCommit)
                .prevLogKey().term(prevLogEntryTerm)
                                .index(prevLogEntryIndex);


        final MutatingIterator<LogEntry> logEntryIterator = directAppendRequest.logEntryIterator();
        assertThat(logEntryIterator.hasNext()).isEqualTo(false);

        logEntry1
                .term(newEntryTerm1)
                .index(newEntryIndex1)
                .command()
                    .sourceId(newEntryCommandSourceId1)
                    .commandIndex(newEntryCommandIndex1)
                    .commandPayload().bytesFrom(commandBytes1, 0, commandBytes1.length);

        logEntry2
                .term(newEntryTerm2)
                .index(newEntryIndex2)
                .command()
                .sourceId(newEntryCommandSourceId2)
                .commandIndex(newEntryCommandIndex2)
                .commandPayload().bytesFrom(commandBytes2, 0, commandBytes2.length);


        directAppendRequest.appendLogEntry(logEntry1);
        directAppendRequest.appendLogEntry(logEntry2);

        assertThat(logEntryIterator.hasNext()).isEqualTo(true);


        // then
        assertThat(directAppendRequest.type()).isEqualTo(MessageType.APPEND_REQUEST);
        assertThat(directAppendRequest.term()).isEqualTo(term);
        assertThat(directAppendRequest.leaderId()).isEqualTo(leaderId);
        assertThat(directAppendRequest.leaderCommit()).isEqualTo(leaderCommit);


        final int retrievedPrevLogEntryTerm = directAppendRequest.prevLogKey().term();
        final long retrievedPrevLogEntryIndex = directAppendRequest.prevLogKey().index();

        assertThat(retrievedPrevLogEntryTerm).isEqualTo(prevLogEntryTerm);
        assertThat(retrievedPrevLogEntryIndex).isEqualTo(prevLogEntryIndex);


        ///// LogEntry 1 ////////

        logEntryIterator.next(iteratingLogEntry);

        final int retrievedCommandLogEntryTerm1 = iteratingLogEntry.logKey().term();
        final long retrievedCommandLogEntryIndex1 = iteratingLogEntry.logKey().index();

        assertThat(retrievedCommandLogEntryTerm1).isEqualTo(newEntryTerm1);
        assertThat(retrievedCommandLogEntryIndex1).isEqualTo(newEntryIndex1);

        final int retrievedCommandSourceId1 = iteratingLogEntry.command().commandKey().sourceId();
        final long retrieveCommandIndex1 = iteratingLogEntry.command().commandKey().commandIndex();
        final int retrievedCommandByteLength1 = iteratingLogEntry.command().commandPayload().commandByteLength();

        assertThat(retrievedCommandSourceId1).isEqualTo(newEntryCommandSourceId1);
        assertThat(retrieveCommandIndex1).isEqualTo(newEntryCommandIndex1);
        assertThat(retrievedCommandByteLength1).isEqualTo(commandBytes1.length);

        final byte[] retrievedCommandBytes1 = new byte[retrievedCommandByteLength1];
        iteratingLogEntry.command().commandPayload().bytesTo(retrievedCommandBytes1, 0);

        assertThat(new String(retrievedCommandBytes1)).isEqualTo(myCommand1);


        ///// LogEntry 2 ////////

        assertThat(logEntryIterator.hasNext()).isEqualTo(true);

        logEntryIterator.next(iteratingLogEntry);

        final int retrievedCommandLogEntryTerm2 = iteratingLogEntry.logKey().term();
        final long retrievedCommandLogEntryIndex2 = iteratingLogEntry.logKey().index();

        assertThat(retrievedCommandLogEntryTerm2).isEqualTo(newEntryTerm2);
        assertThat(retrievedCommandLogEntryIndex2).isEqualTo(newEntryIndex2);

        final int retrievedCommandSourceId2 = iteratingLogEntry.command().commandKey().sourceId();
        final long retrieveCommandIndex2 = iteratingLogEntry.command().commandKey().commandIndex();
        final int retrievedCommandByteLength2 = iteratingLogEntry.command().commandPayload().commandByteLength();

        assertThat(retrievedCommandSourceId2).isEqualTo(newEntryCommandSourceId2);
        assertThat(retrieveCommandIndex2).isEqualTo(newEntryCommandIndex2);
        assertThat(retrievedCommandByteLength2).isEqualTo(commandBytes2.length);

        final byte[] retrievedCommandBytes2 = new byte[retrievedCommandByteLength2];
        iteratingLogEntry.command().commandPayload().bytesTo(retrievedCommandBytes2, 0);

        assertThat(new String(retrievedCommandBytes2)).isEqualTo(myCommand2);

        ///// LogEntry 3 - does not exist ////////
        assertThat(logEntryIterator.hasNext()).isEqualTo(false);

        final int extectedAppendRequestBytes = DirectAppendRequest.EMPTY_LOG_BYTE_LENGTH +
                DirectLogEntry.EMPTY_COMMAND_BYTE_LENGTH * 2 +
                commandBytes1.length + commandBytes2.length;

        assertThat(directAppendRequest.byteLength()).isEqualTo(extectedAppendRequestBytes);


    }


    @Test
    public void should_get_the_data_that_has_been_set_when_no_log_entries() throws Exception {

        //given
        final int term = 10;
        final int leaderId = 2;
        final long leaderCommit = 34564;
        final int prevLogEntryTerm = 8;
        final long prevLogEntryIndex = 34765;

        //when
        directAppendRequest
                .term(term)
                .leaderId(leaderId)
                .leaderCommit(leaderCommit)
                .prevLogKey().term(prevLogEntryTerm)
                .index(prevLogEntryIndex);


        final MutatingIterator<LogEntry> logEntryIterator = directAppendRequest.logEntryIterator();
        assertThat(logEntryIterator.hasNext()).isEqualTo(false);


        // then
        assertThat(directAppendRequest.type()).isEqualTo(MessageType.APPEND_REQUEST);
        assertThat(directAppendRequest.term()).isEqualTo(term);
        assertThat(directAppendRequest.leaderId()).isEqualTo(leaderId);
        assertThat(directAppendRequest.leaderCommit()).isEqualTo(leaderCommit);


        final int retrievedPrevLogEntryTerm = directAppendRequest.prevLogKey().term();
        final long retrievedPrevLogEntryIndex = directAppendRequest.prevLogKey().index();

        assertThat(retrievedPrevLogEntryTerm).isEqualTo(prevLogEntryTerm);
        assertThat(retrievedPrevLogEntryIndex).isEqualTo(prevLogEntryIndex);


        final int extectedAppendRequestBytes = DirectAppendRequest.EMPTY_LOG_BYTE_LENGTH;

        assertThat(directAppendRequest.byteLength()).isEqualTo(extectedAppendRequestBytes);


    }
}