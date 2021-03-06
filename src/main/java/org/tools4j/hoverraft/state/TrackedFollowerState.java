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
package org.tools4j.hoverraft.state;

public final class TrackedFollowerState {

    private final int serverId;

    private long nextIndex;
    private long matchIndex;

    public TrackedFollowerState(final int serverId) {
        this.serverId = serverId;
    }

    public int serverId() {
        return serverId;
    }

    public long nextIndex() {
        return nextIndex;
    }

    public long matchIndex() {
        return matchIndex;
    }

    public TrackedFollowerState nextIndex(final long index) {
        this.nextIndex = index;
        return this;
    }

    public TrackedFollowerState decrementNextIndex() {
        this.nextIndex--;
        return this;
    }

    public TrackedFollowerState resetMatchIndex() {
        this.matchIndex = -1;
        return this;
    }

    public TrackedFollowerState updateMatchIndex(final long index) {
        this.matchIndex = Long.max(index, this.matchIndex);
        return this;
    }
}
