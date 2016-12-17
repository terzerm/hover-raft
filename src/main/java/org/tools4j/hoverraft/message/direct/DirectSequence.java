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

import org.agrona.MutableDirectBuffer;
import org.tools4j.hoverraft.direct.AbstractDirectPayload;
import org.tools4j.hoverraft.direct.DirectPayload;
import org.tools4j.hoverraft.message.Sequence;

public class DirectSequence<T extends DirectPayload> extends AbstractDirectPayload implements Sequence<T> {

    private static final int SEQ_LENGTH_OFFSET  = 0;
    private static final int SEQ_LENGTH_LEN     = 4;
    private static final int SEQ_ENTRIES_OFF    = SEQ_LENGTH_OFFSET + SEQ_LENGTH_LEN;

    public static final int EMPTY_SEQUENCE_BYTE_LENGTH = SEQ_ENTRIES_OFF;

    private final DirectSequenceIterator<T> iterator = new DirectSequenceIterator<>();

    @Override
    public int byteLength() {
        return SEQ_LENGTH_LEN + sequenceByteLength();
    }

    public int sequenceByteLength() {
        return readBuffer.getInt(offset + SEQ_LENGTH_OFFSET);
    }

    private Sequence<T> sequenceByteLength(final int seqByteLen) {
        writeBuffer.putInt(offset + SEQ_LENGTH_OFFSET, seqByteLen);
        return this;
    }

    @Override
    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        super.wrap(buffer, offset);
        sequenceByteLength(0);
    }

    @Override
    public SequenceIterator<T> iterator() {
        return iterator.reset();
    }

    @Override
    public Sequence<T> append(final T source) {
        final int sequenceLen = sequenceByteLength();
        writeBuffer.putBytes(offset + SEQ_ENTRIES_OFF + sequenceLen, source.readBufferOrNull(), source.offset(), source.byteLength());
        return sequenceByteLength(sequenceLen + source.byteLength());
    }

    private final class DirectSequenceIterator<T extends DirectPayload> implements SequenceIterator<T> {

        private int nextOffset = 0;

        @Override
        public boolean hasNext() {
            return nextOffset < sequenceByteLength();
        }

        @Override
        public SequenceIterator<T> readNextTo(final T target) {
            target.wrap(readBuffer, offset + SEQ_ENTRIES_OFF + nextOffset);
            nextOffset += target.byteLength();
            return this;
        }

        public SequenceIterator<T> reset() {
            nextOffset = 0;
            return this;
        }
    }

}
