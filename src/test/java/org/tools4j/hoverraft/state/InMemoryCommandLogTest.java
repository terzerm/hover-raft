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
package org.tools4j.hoverraft.state;

import org.junit.Test;
import org.tools4j.hoverraft.command.CommandLogEntry;
import org.tools4j.hoverraft.command.InMemoryCommandLog;
import org.tools4j.hoverraft.direct.AllocatingDirectFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;


public class InMemoryCommandLogTest {

    @Test
    public void append_should_add_new_logEntry() throws Exception {
        final AllocatingDirectFactory factory = new AllocatingDirectFactory();
        CommandLogEntry commandLogEntry1 = factory.commandLogEntry();

        commandLogEntry1.term(1);
        commandLogEntry1.index(1L);
        commandLogEntry1.commandMessage().command().bytesFrom(new byte[] {}, 0, 0);

        InMemoryCommandLog commandLog = new InMemoryCommandLog();
        commandLog.append(commandLogEntry1);

        assertThat(commandLog.size()).isEqualTo(1);
    }
}