package org.tools4j.hoverraft.message.direct;

import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectCommandMessageTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectCommandMessage.EMPTY_COMMAND_BYTE_LENGTH);
    private final DirectCommandMessage directCommandMessage = new DirectCommandMessage();

    @Before
    public void init() {
        directCommandMessage.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final String myCommand = "Command: XXXXXX";
        final int sourceId = 2342434;
        final long commandIndex = 234234353;
        final byte[] commandBytes = myCommand.getBytes();

        //when
        directCommandMessage
                .commandSourceId(sourceId)
                .commandIndex(commandIndex)
                .command().bytesFrom(commandBytes, 0, commandBytes.length);

        //then
        assertThat(directCommandMessage.commandSourceId()).isEqualTo(sourceId);
        assertThat(directCommandMessage.commandIndex()).isEqualTo(commandIndex);
        assertThat(directCommandMessage.command().byteLength()).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[directCommandMessage.command().byteLength()];
        directCommandMessage.command().bytesTo(retrievedCommandBytes, 0);

        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);
    }
}