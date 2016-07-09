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

    private final ElectionState electionState;
    private final FollowerState[] followerStates;

    private Role role = Role.CANDIDATE;
    private long commitIndex;
    private long lastApplied;

    public VolatileState(final int serverId, final ConsensusConfig consensusConfig) {
        this.electionState = new ElectionState(consensusConfig);
        this.followerStates = initFollowerStates(serverId, consensusConfig);
    }

    public Role role() {
        return role;
    }

    public void changeRoleTo(Role role) {
        this.role = role;
    }

    public long commitIndex() {
        return commitIndex;
    }

    public long lastApplied() {
        return lastApplied;
    }

    public int followerCount() {
        return followerStates.length;
    }

    public ElectionState electionState() {
        return electionState;
    }

    public FollowerState followerState(int index) {
        return followerStates[index];
    }

    public FollowerState followerStateById(int id) {
        for (final FollowerState fs : followerStates) {
            if (fs.serverId() == id) {
                return fs;
            }
        }
        throw new IllegalArgumentException("no follower state found for id " + id);
    }

    private static FollowerState[] initFollowerStates(final int serverId, final ConsensusConfig consensusConfig) {
        final FollowerState[] states = new FollowerState[consensusConfig.serverCount() - 1];
        int index = 0;
        for (int i = 0; i < consensusConfig.serverCount(); i++) {
            final int id = consensusConfig.serverConfig(i).id();
            if (id != serverId) {
                states[index] = new FollowerState(id);
                index++;
            }
        }
        return states;
    }
}
