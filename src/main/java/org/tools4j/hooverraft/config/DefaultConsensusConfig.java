/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 hoover-raft (tools4j), Marco Terzer
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
package org.tools4j.hooverraft.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Default immutable implementation of {@link ConsensusConfig}.
 */
public final class DefaultConsensusConfig implements ConsensusConfig {

    private final long minElectionTimeoutMillis;
    private final long maxElectionTimeoutMillis;
    private final Optional<String> ipcMulticastChannel;
    private final ThreadingMode threadingMode;
    private final List<ServerConfig> serverConfigs;
    private final List<SourceConfig> sourceConfigs;

    public DefaultConsensusConfig(final long minElectionTimeoutMillis,
                                  final long maxElectionTimeoutMillis,
                                  final Optional<String> ipcMulticastChannel,
                                  final ThreadingMode threadingMode,
                                  final List<ServerConfig> serverConfigs,
                                  final List<SourceConfig> sourceConfigs) {
        if (minElectionTimeoutMillis > maxElectionTimeoutMillis) {
            throw new IllegalArgumentException("minElectionTimeoutMillis must not be greater than maxElectionTimeoutMillis: " + minElectionTimeoutMillis + " > " + maxElectionTimeoutMillis);
        }
        if (serverConfigs.isEmpty()) {
            throw new IllegalArgumentException("serverConfigs must not be empty");
        }
        this.minElectionTimeoutMillis = minElectionTimeoutMillis;
        this.maxElectionTimeoutMillis = maxElectionTimeoutMillis;
        this.ipcMulticastChannel = Objects.requireNonNull(ipcMulticastChannel, "ipcMulticastChannel cannot be null");
        this.threadingMode = Objects.requireNonNull(threadingMode, "threadingMode cannot be null");
        this.serverConfigs = Objects.requireNonNull(serverConfigs, "serverConfigs cannot be null");
        this.sourceConfigs = Objects.requireNonNull(sourceConfigs, "sourceConfigs cannot be null");
    }

    @Override
    public long minElectionTimeoutMillis() {
        return minElectionTimeoutMillis;
    }

    @Override
    public long maxElectionTimeoutMillis() {
        return maxElectionTimeoutMillis;
    }

    @Override
    public Optional<String> ipcMulticastChannel() {
        return ipcMulticastChannel;
    }

    @Override
    public int serverCount() {
        return serverConfigs.size();
    }

    @Override
    public ServerConfig serverConfig(int index) {
        return serverConfig(index);
    }

    @Override
    public int sourceCount() {
        return sourceConfigs.size();
    }

    @Override
    public SourceConfig sourceConfig(int index) {
        return sourceConfigs.get(index);
    }

    @Override
    public ThreadingMode threadingMode() {
        return threadingMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DefaultConsensusConfig that = (DefaultConsensusConfig) o;

        if (minElectionTimeoutMillis != that.minElectionTimeoutMillis) return false;
        if (maxElectionTimeoutMillis != that.maxElectionTimeoutMillis) return false;
        if (!ipcMulticastChannel.equals(that.ipcMulticastChannel)) return false;
        if (threadingMode != that.threadingMode) return false;
        if (!serverConfigs.equals(that.serverConfigs)) return false;
        return sourceConfigs.equals(that.sourceConfigs);

    }

    @Override
    public int hashCode() {
        int result = (int) (minElectionTimeoutMillis ^ (minElectionTimeoutMillis >>> 32));
        result = 31 * result + (int) (maxElectionTimeoutMillis ^ (maxElectionTimeoutMillis >>> 32));
        result = 31 * result + ipcMulticastChannel.hashCode();
        result = 31 * result + threadingMode.hashCode();
        result = 31 * result + serverConfigs.hashCode();
        result = 31 * result + sourceConfigs.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultConsensusConfig{" +
                "minElectionTimeoutMillis=" + minElectionTimeoutMillis +
                ", maxElectionTimeoutMillis=" + maxElectionTimeoutMillis +
                ", ipcMulticastChannel=" + ipcMulticastChannel +
                ", threadingMode=" + threadingMode +
                ", serverConfigs=" + serverConfigs +
                ", sourceConfigs=" + sourceConfigs +
                '}';
    }

}
