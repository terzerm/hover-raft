package org.tools4j.hoverraft.state.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.direct.DirectCommandMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DirectCommandLogEntryTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectCommandLogEntry.BYTE_LENGTH);
    private final DirectCommandLogEntry directCommandLogEntry = new DirectCommandLogEntry();

    @Before
    public void init() {
        directCommandLogEntry.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final int term = 5;
        final long index = 342444;
        final String myCommand = "Command: XXXXXX";
        final int sourceId = 2342434;
        final long commandIndex = 234234353;
        final byte[] commandBytes = myCommand.getBytes();

        //when
        directCommandLogEntry
                .term(term)
                .index(index);
        directCommandLogEntry.commandMessage()
                .commandSourceId(sourceId)
                .commandIndex(commandIndex)
                .command().bytesFrom(commandBytes, 0, commandBytes.length);



        //then
        assertThat(directCommandLogEntry.term()).isEqualTo(term);
        assertThat(directCommandLogEntry.index()).isEqualTo(index);

        final CommandMessage commandMessage = directCommandLogEntry.commandMessage();

        assertThat(commandMessage.type()).isEqualTo(MessageType.COMMAND_MESSAGE);
        assertThat(commandMessage.commandSourceId()).isEqualTo(sourceId);
        assertThat(commandMessage.commandIndex()).isEqualTo(commandIndex);
        assertThat(commandMessage.command().byteLength()).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[commandMessage.command().byteLength()];
        commandMessage.command().bytesTo(retrievedCommandBytes, 0);


        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);

        final int expectedLogEntryBytesLength = DirectCommandLogEntry.BYTE_LENGTH +
                DirectCommandMessage.EMPTY_COMMAND_BYTE_LENGTH +
                commandBytes.length;

        assertThat(directCommandLogEntry.byteLength()).isEqualTo(expectedLogEntryBytesLength);

    }
}