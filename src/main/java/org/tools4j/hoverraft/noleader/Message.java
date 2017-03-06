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
package org.tools4j.hoverraft.noleader;

/**
 * Created by terz on 5/3/17.
 */
public interface Message {
    int sourceId();
    long messageId();

    static Message create(final int sourceId, final long messageId) {
        return new Message() {
            @Override
            public int sourceId() {
                return sourceId;
            }

            @Override
            public long messageId() {
                return messageId;
            }

            @Override
            public String toString() {
                return "Message[sourceId=" + sourceId + ", messageId=" + messageId + "]";
            }

            @Override
            public int hashCode() {
                return (31 * sourceId) + Long.hashCode(messageId);
            }

            @Override
            public boolean equals(final Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != getClass()) {
                    return false;
                }
                final Message other = (Message)obj;
                return sourceId == other.sourceId() && messageId == other.messageId();
            }
        };
    }
}
