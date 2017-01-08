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
package org.tools4j.hoverraft.message.direct;

import org.tools4j.hoverraft.command.CommandKey;
import org.tools4j.hoverraft.direct.AbstractDirectPayload;

public class DirectCommandKey extends AbstractDirectPayload implements CommandKey {
    private static final int SOURCE_ID_OFF = 0;
    private static final int SOURCE_ID_LEN = 4;

    private static final int COMMAND_INDEX_OFF = SOURCE_ID_OFF + SOURCE_ID_LEN;
    private static final int COMMAND_INDEX_LEN = 8;

    public static final int BYTE_LENGTH = COMMAND_INDEX_OFF + COMMAND_INDEX_LEN;

    @Override
    public int byteLength() {
        return BYTE_LENGTH;
    }

    @Override
    public int sourceId() {
        return readBuffer.getInt(offset + SOURCE_ID_OFF);
    }

    @Override
    public long commandIndex() {
        return readBuffer.getLong(offset + COMMAND_INDEX_OFF);
    }

    @Override
    public DirectCommandKey sourceId(final int sourceId) {
        writeBuffer.putInt(offset + SOURCE_ID_OFF, sourceId);
        return this;
    }

    @Override
    public DirectCommandKey commandIndex(final long commandIndex) {
        writeBuffer.putLong(offset + COMMAND_INDEX_OFF, commandIndex);
        return this;
    }
}
