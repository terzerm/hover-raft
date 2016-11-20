package org.tools4j.hoverraft.state;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;


public class InMemoryCommandLogTest {

    @Test
    public void append_should_add_new_logEntry() throws Exception {
        CommandLogEntry commandLogEntry1 = mock(CommandLogEntry.class);

        when(commandLogEntry1.term()).thenReturn(1);
        when(commandLogEntry1.index()).thenReturn(1L);


        InMemoryCommandLog commandLog = new InMemoryCommandLog();
        commandLog.append(commandLogEntry1);

        assertThat(commandLog.size()).isEqualTo(1);
    }
}