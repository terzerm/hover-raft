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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public interface Envelope {
    int senderServerId();
    long sequenceNo();
    Message message();
    Set<Integer> serverIds();
    default Envelope confirm(final int serverId) {
        if (senderServerId() == serverId && serverIds().contains(serverId)) {
            return this;
        }
        return new Envelope() {
            private final Set<Integer> serverIds = new TreeSet<>(Envelope.this.serverIds());

            {
                serverIds.add(serverId);
            }

            @Override
            public int senderServerId() {
                return serverId;
            }

            @Override
            public long sequenceNo() {
                return Envelope.this.sequenceNo();
            }

            @Override
            public Message message() {
                return Envelope.this.message();
            }

            @Override
            public Set<Integer> serverIds() {
                return serverIds;
            }

            @Override
            public String toString() {
                return "Envelope{senderServerId=" + serverId + ", sequenceNo=" + sequenceNo() +
                        ", message=" + message() + ", serverIds=" + serverIds() + "}";
            }
        };
    }

    static Envelope create(final long sequenceNo, final Message message, final int serverId) {
        return new Envelope() {
            @Override
            public int senderServerId() {
                return serverId;
            }
            @Override
            public long sequenceNo() {
                return sequenceNo;
            }

            @Override
            public Message message() {
                return message;
            }

            @Override
            public Set<Integer> serverIds() {
                return Collections.singleton(serverId);
            }

            @Override
            public String toString() {
                return "Envelope{senderServerId=" + serverId + ", sequenceNo=" + sequenceNo +
                        ", message=" + message + ", serverIds=[" + serverId + "]}";
            }
        };
    }
}
