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
package org.tools4j.hoverraft.transport;

import io.aeron.Publication;

/**
 * Reject reasons that may be returned by {@link Sender#offer(Object)}.
 */
public interface RejectReason {
    /**
     * Not yet connected to a receiver.
     */
    long NOT_CONNECTED = Publication.NOT_CONNECTED;

    /**
     * The offer failed due to back pressure from the connected sources preventing further transmission.
     */
    long BACK_PRESSURED = Publication.BACK_PRESSURED;

    /**
     * The offer failed due to an administration action and should be retried.
     */
    long ADMIN_ACTION = Publication.ADMIN_ACTION;

    /**
     * The transport has been closed and should no longer be used.
     */
    long CLOSED = Publication.CLOSED;
}
