package org.tools4j.hoverraft.state;

import org.tools4j.hoverraft.message.CommandMessage;

public interface CommandLogEntry extends LogEntry {
    CommandMessage commandMessage();
}
