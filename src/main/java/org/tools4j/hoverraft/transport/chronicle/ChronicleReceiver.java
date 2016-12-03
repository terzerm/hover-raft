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
package org.tools4j.hoverraft.transport.chronicle;

import net.openhft.chronicle.ExcerptTailer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.direct.DirectFactory;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.MessageType;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Subscription reading from a chronicle queue.
 */
public class ChronicleReceiver implements Receiver<Message> {

    private final DirectFactory directFactory;
    private final MutableDirectBuffer mutableDirectBuffer;
    private final ExcerptTailer tailer;

    public ChronicleReceiver(final DirectFactory directFactory, final MutableDirectBuffer mutableDirectBuffer, final ExcerptTailer tailer) {
        this.directFactory = Objects.requireNonNull(directFactory);
        this.mutableDirectBuffer = Objects.requireNonNull(mutableDirectBuffer);
        this.tailer = Objects.requireNonNull(tailer);
    }

    @Override
    public int poll(final Consumer<? super Message> messageMandler, final int limit) {
        int messagesRead = 0;
        while (messagesRead < limit && tailer.nextIndex()) {
            final int len = tailer.readInt();
            for (int i = 0; i < len; ) {
                if (i + 8 <= len) {
                    mutableDirectBuffer.putLong(i, tailer.readLong());
                    i += 8;
                } else {
                    mutableDirectBuffer.putByte(i, tailer.readByte());
                    i++;
                }
            }
            consume(messageMandler);
            messagesRead++;
        }
        return messagesRead;
    }

    private void consume(final Consumer<? super Message> messageMandler) {
        final MessageType messageType = MessageType.readFrom(mutableDirectBuffer, 0);
        final Message message = messageType.create(directFactory);
        message.wrap(mutableDirectBuffer, 0);
        messageMandler.accept(message);
        message.unwrap();
    }
}
