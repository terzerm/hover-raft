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

import org.tools4j.hoverraft.config.ConsensusConfig;

public final class VolatileState {

    private final TrackedFollowerState[] trackedFollowerStates;

    @Deprecated //REMOVE this is now storted as State in HoverRaftMachine
    private Role role = Role.FOLLOWER;
    private long commitIndex = -1;
    private long lastApplied = -1;

    public VolatileState(final int serverId, final ConsensusConfig consensusConfig) {
        this.trackedFollowerStates = initFollowerStates(serverId, consensusConfig);
    }

    public long commitIndex() {
        return commitIndex;
    }

    public VolatileState commitIndex(final long commitIndex) {
        this.commitIndex = commitIndex;
        return this;
    }

    public long lastApplied() {
        return lastApplied;
    }

    public VolatileState lastApplied(final long lastApplied) {
        this.lastApplied = lastApplied;
        return this;
    }

    public int followerCount() {
        return trackedFollowerStates.length;
    }

    public TrackedFollowerState followerState(int index) {
        return trackedFollowerStates[index];
    }

    public void resetFollowersState(final long nextIndex) {
        for (final TrackedFollowerState fs : trackedFollowerStates) {
            fs.nextIndex(nextIndex)
                    .resetMatchIndex();
        }
    }

    public TrackedFollowerState followerStateById(int id) {
        for (final TrackedFollowerState fs : trackedFollowerStates) {
            if (fs.serverId() == id) {
                return fs;
            }
        }
        throw new IllegalArgumentException("no follower state found for id " + id);
    }

    private static TrackedFollowerState[] initFollowerStates(final int serverId, final ConsensusConfig consensusConfig) {
        final TrackedFollowerState[] states = new TrackedFollowerState[consensusConfig.serverCount() - 1];
        int index = 0;
        for (int i = 0; i < consensusConfig.serverCount(); i++) {
            final int id = consensusConfig.serverConfig(i).id();
            if (id != serverId) {
                states[index] = new TrackedFollowerState(id);
                index++;
            }
        }
        return states;
    }
}
