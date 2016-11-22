package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;

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
                .prevLogEntry().term(prevLogEntryTerm)
                                .index(prevLogEntryIndex);
        directAppendRequest.commandLogEntry()
                .term(newEntryTerm)
                .index(newEntryIndex);
        directAppendRequest.commandLogEntry()
                .commandMessage()
                    .commandIndex(newEntryCommandIndex)
                    .commandSourceId(newEntryCommandSourceId)
                    .command().bytesFrom(commandBytes, 0, commandBytes.length);

        // then
        assertThat(directAppendRequest.term()).isEqualTo(term);
        assertThat(directAppendRequest.leaderId()).isEqualTo(leaderId);
        assertThat(directAppendRequest.leaderCommit()).isEqualTo(leaderCommit);


        final int retrievedPrevLogEntryTerm = directAppendRequest.prevLogEntry().term();
        final long retrievedPrevLogEntryIndex = directAppendRequest.prevLogEntry().index();

        assertThat(retrievedPrevLogEntryTerm).isEqualTo(prevLogEntryTerm);
        assertThat(retrievedPrevLogEntryIndex).isEqualTo(prevLogEntryIndex);


        final int retrievedCommandLogEntryTerm = directAppendRequest.commandLogEntry().term();
        final long retrievedCommandLogEntryIndex = directAppendRequest.commandLogEntry().index();

        assertThat(retrievedCommandLogEntryTerm).isEqualTo(newEntryTerm);
        assertThat(retrievedCommandLogEntryIndex).isEqualTo(newEntryIndex);

        final int retrievedCommandSourceId = directAppendRequest.commandLogEntry().commandMessage().commandSourceId();
        final long retrieveCommandIndex = directAppendRequest.commandLogEntry().commandMessage().commandIndex();
        final int retrievedCommandByteLength = directAppendRequest.commandLogEntry().commandMessage().command().byteLength();

        assertThat(retrievedCommandSourceId).isEqualTo(newEntryCommandSourceId);
        assertThat(retrieveCommandIndex).isEqualTo(newEntryCommandIndex);
        assertThat(retrievedCommandByteLength).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[retrievedCommandByteLength];
        directAppendRequest.commandLogEntry().commandMessage().command().bytesTo(retrievedCommandBytes, 0);

        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);


    }
}