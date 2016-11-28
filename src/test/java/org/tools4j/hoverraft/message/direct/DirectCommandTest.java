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

import static org.assertj.core.api.Assertions.assertThat;

public class DirectCommandTest {
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(DirectCommand.EMPTY_COMMAND_BYTE_LENGTH);
    private final DirectCommand directCommandMessage = new DirectCommand();

    @Before
    public void init() {
        directCommandMessage.wrap(buffer, 0);
    }

    @Test
    public void should_get_the_data_that_has_been_set() throws Exception {
        //given
        final String myCommand = "CommandPayload: XXXXXX";
        final int sourceId = 2342434;
        final long commandIndex = 234234353;
        final byte[] commandBytes = myCommand.getBytes();

        //when
        directCommandMessage
                .sourceId(sourceId)
                .commandIndex(commandIndex)
                .commandPayload().bytesFrom(commandBytes, 0, commandBytes.length);

        //then
        assertThat(directCommandMessage.commandKey().sourceId()).isEqualTo(sourceId);
        assertThat(directCommandMessage.commandKey().commandIndex()).isEqualTo(commandIndex);
        assertThat(directCommandMessage.commandPayload().byteLength()).isEqualTo(commandBytes.length);

        final byte[] retrievedCommandBytes = new byte[directCommandMessage.commandPayload().byteLength()];
        directCommandMessage.commandPayload().bytesTo(retrievedCommandBytes, 0);


        assertThat(new String(retrievedCommandBytes)).isEqualTo(myCommand);

        final int expectedCommandMessageBytesLength = DirectCommand.EMPTY_COMMAND_BYTE_LENGTH + commandBytes.length;
        assertThat(directCommandMessage.byteLength()).isEqualTo(expectedCommandMessageBytesLength);

    }
}