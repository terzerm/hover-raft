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
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.transport.Receiver;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Subscription reading from a chronicle queue.
 */
public class ChronicleReceiver implements Receiver<DirectMessage> {

    private final ExcerptTailer tailer;
    private final MutableDirectBuffer mutableDirectBuffer;
    private final DirectMessageFactory directMessageFactory;

    public ChronicleReceiver(final ExcerptTailer tailer, MutableDirectBuffer buffer) {
        this.tailer = Objects.requireNonNull(tailer);
        this.mutableDirectBuffer = Objects.requireNonNull(buffer);
        this.directMessageFactory = DirectMessageFactory.createForWriting(buffer, 0);
    }

    @Override
    public Poller poller(final Consumer<? super DirectMessage> messageMandler) {
        return limit -> poll(messageMandler, limit);
    }

    private int poll(final Consumer<? super DirectMessage> messageMandler, final int limit) {
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

    private void consume(final Consumer<? super DirectMessage> messageMandler) {
        final DirectMessage message = directMessageFactory.wrapForReading((DirectBuffer) mutableDirectBuffer, 0);
        messageMandler.accept(message);
    }
}
