package org.tools4j.hoverraft.state.direct;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.message.CommandMessage;
import org.tools4j.hoverraft.message.direct.DirectCommandMessage;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.state.CommandLogEntry;
import org.tools4j.hoverraft.state.LogEntry;

import java.util.Objects;

public class DirectCommandLogEntry implements CommandLogEntry, DirectMessage {
    protected static final int TERM_OFF = 0;
    protected static final int TERM_LEN = 4;

    protected static final int INDEX_OFF = TERM_OFF + TERM_LEN;
    protected static final int INDEX_LEN = 8;

    protected static final int COMMAND_MSG_OFF = INDEX_OFF + INDEX_LEN;

    protected DirectBuffer readBuffer;
    protected MutableDirectBuffer writeBuffer;
    protected int offset;

    private DirectCommandMessage directCommandMessage = new DirectCommandMessage();

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public DirectBuffer buffer() {
        return readBuffer;
    }

    public void wrap(final DirectBuffer buffer, final int offset) {
        this.readBuffer = Objects.requireNonNull(buffer);
        this.writeBuffer = null;
        this.offset = offset;
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        Objects.requireNonNull(buffer);
        this.readBuffer = buffer;
        this.writeBuffer = buffer;
        this.offset = offset;
        directCommandMessage.wrap(buffer, offset + COMMAND_MSG_OFF);
    }

    public void unwrap() {
        this.readBuffer = null;
        this.writeBuffer = null;
        this.offset = 0;
        directCommandMessage.unwrap();
    }


    @Override
    public CommandMessage commandMessage() {
        return directCommandMessage;
    }

    @Override
    public int byteLength() {
        return COMMAND_MSG_OFF + directCommandMessage.byteLength();
    }


    @Override
    public int term() {
        return readBuffer.getInt(offset + TERM_OFF);
    }

    @Override
    public LogEntry term(int term) {
        writeBuffer.putInt(offset + TERM_OFF, term);
        return this;
    }

    @Override
    public long index() {
        return readBuffer.getLong(offset + INDEX_OFF);
    }

    @Override
    public LogEntry index(long index) {
        writeBuffer.putLong(offset + INDEX_OFF, index);
        return this;
    }
}
