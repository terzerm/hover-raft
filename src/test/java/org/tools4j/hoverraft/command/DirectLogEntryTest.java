package org.tools4j.hoverraft.command;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.message.direct.DirectCommand;
import org.tools4j.hoverraft.message.direct.DirectCommandMessage;
import org.tools4j.hoverraft.message.direct.DirectLogKey;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectLogEntryTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectLogKey.BYTE_LENGTH);
    private final DirectLogEntry directLogEntry = new DirectLogEntry();

    @Before
    public void init() {
        directLogEntry.wrap(buffer, 0);
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
        directLogEntry.logKey()
                .term(term)
                .index(index);
        directLogEntry.commandMessage()
                .commandSourceId(sourceId)
                .commandIndex(commandIndex)
                .command().bytesFrom(commandBytes, 0, commandBytes.length);



        //then
        assertThat(directLogEntry.logKey().term()).isEqualTo(term);
        assertThat(directLogEntry.logKey().index()).isEqualTo(index);

        final CommandMessage commandMessage = directLogEntry.commandMessage();

        assertThat(commandMessage.type()).isEqualTo(MessageType.COMMAND_MESSAGE);
        assertThat(commandMessage.commandSourceId()).isEqualTo(sourceId);
        assertThat(commandMessage.commandIndex()).isEqualTo(commandIndex);
        assertThat(commandMessage.command().byteLength()).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[commandMessage.command().byteLength()];
        commandMessage.command().bytesTo(retrievedCommandBytes, 0);


        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);

        final int expectedLogEntryBytesLength = DirectLogKey.BYTE_LENGTH +
                DirectCommandMessage.EMPTY_COMMAND_BYTE_LENGTH +
                commandBytes.length;

        assertThat(directLogEntry.byteLength()).isEqualTo(expectedLogEntryBytesLength);

    }

    @Test
    public void should_copy_command_from_other_command() throws Exception {
        //given
        final String myCommand = "Command: XXXXXX";
        final byte[] commandBytes = myCommand.getBytes();
        DirectCommand otherCommand = new DirectCommand();
        final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
        otherCommand.wrap(buffer, 0);
        otherCommand.bytesFrom(commandBytes, 0, commandBytes.length);


        //when
        directLogEntry.commandMessage()
                .command().copyFrom(otherCommand);

        //then
        final CommandMessage commandMessage = directLogEntry.commandMessage();

        assertThat(commandMessage.command().byteLength()).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[commandMessage.command().byteLength()];
        commandMessage.command().bytesTo(retrievedCommandBytes, 0);

        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);
    }
}