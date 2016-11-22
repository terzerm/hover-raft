package org.tools4j.hoverraft.state.direct;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.direct.DirectCommandMessage;
import org.tools4j.hoverraft.message.direct.DirectLogEntry;
import org.tools4j.hoverraft.state.CommandLogEntry;

public class DirectCommandLogEntry extends DirectLogEntry implements CommandLogEntry {
    private static final int COMMAND_MSG_OFF = DirectLogEntry.BYTE_LENGTH;

    private DirectCommandMessage directCommandMessage = new DirectCommandMessage();

    @Override
    public void wrap(final DirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    @Override
    public void unwrap() {
        directCommandMessage.unwrap();
        super.unwrap();
    }


    @Override
    public CommandMessage commandMessage() {
        return directCommandMessage;
    }

    @Override
    public int byteLength() {
        return COMMAND_MSG_OFF + directCommandMessage.byteLength();
    }

}
