/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 hover-raft (tools4j), Marco Terzer
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
package org.tools4j.hoverraft.command;

import org.tools4j.hoverraft.direct.DirectPayload;
import org.tools4j.hoverraft.event.Event;
import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.server.ServerContext;
import org.tools4j.hoverraft.state.Transition;

public interface Command extends DirectPayload, Event {

    CommandKey commandKey();

    CommandPayload commandPayload();

    default Command sourceId(final int sourceId) {
        commandKey().sourceId(sourceId);
        return this;
    }

    default Command commandIndex(final long commandIndex) {
        commandKey().commandIndex(commandIndex);
        return this;
    }

    default void copyFrom(final Command command) {
        writeBufferOrNull().putBytes(offset(), command.readBufferOrNull(), command.offset(), command.byteLength());
    }

    @Override
    default Transition accept(final ServerContext serverContext, final EventHandler eventHandler) {
        return eventHandler.onCommand(serverContext, this);
    }

}
