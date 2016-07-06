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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Creates an instance of {@link ConsensusConfig}.
 */
public class ConfigBuilder {

    private static final ToIntFunction<ServerConfig> SERVER_CONFIG_ID_EXTRACTOR = (c) -> c.id();
    private static final ToIntFunction<SourceConfig> SOURCE_CONFIG_ID_EXTRACTOR = (c) -> c.id();

    private long minTimeoutMillis = 150;
    private long maxTimeoutMillis = 300;
    private String ipcMulticastChannel = null;
    private ThreadingMode threadingMode = ThreadingMode.SHARED;
    private final List<ServerConfig> serverConfigs = new ArrayList<>();
    private final List<SourceConfig> sourceConfigs = new ArrayList<>();

    public ConfigBuilder minElectionTimeoutMillis(final long minTimeoutMillis) {
        this.minTimeoutMillis = minTimeoutMillis;
        return this;
    }

    public ConfigBuilder maxElectionTimeoutMillis(final long maxTimeoutMillis) {
        this.maxTimeoutMillis = maxTimeoutMillis;
        return this;
    }

    public ConfigBuilder ipcMulticastChannel(final String ipcMulticastChannel) {
        this.ipcMulticastChannel = ipcMulticastChannel;
        return this;
    }

    public ConfigBuilder threadingMode(final ThreadingMode threadingMode) {
        this.threadingMode = Objects.requireNonNull(threadingMode, "threadingMode cannot be null");
        return this;
    }

    public ConfigBuilder addServer(final String channel) {
        return addServerConfig(nextId(serverConfigs, SERVER_CONFIG_ID_EXTRACTOR), channel);
    }

    public ConfigBuilder addServer(final int id, final String channel) {
        return addServerConfig(checkId("server", serverConfigs, SERVER_CONFIG_ID_EXTRACTOR, id), channel);
    }

    private ConfigBuilder addServerConfig(final int id, final String channel) {
        serverConfigs.add(new DefaultServerConfig(id, channel));
        return this;
    }

    public ConfigBuilder addSource(final String channel) {
        return addSourceConfig(nextId(sourceConfigs, SOURCE_CONFIG_ID_EXTRACTOR), channel);
    }

    public ConfigBuilder addSource(final int id, final String channel) {
        return addSourceConfig(checkId("source", sourceConfigs, SOURCE_CONFIG_ID_EXTRACTOR, id), channel);
    }

    private ConfigBuilder addSourceConfig(final int id, final String channel) {
        serverConfigs.add(new DefaultSourceConfig(id, channel));
        return this;
    }

    public ConsensusConfig build() {
        return new DefaultConsensusConfig(
                minTimeoutMillis, maxTimeoutMillis,
                Optional.ofNullable(ipcMulticastChannel), threadingMode,
                new ArrayList<>(serverConfigs), new ArrayList<>(sourceConfigs));
    }

    private static <E> int nextId(final List<E> list, final ToIntFunction<E> idExtracter) {
        int next = 0;
        for (int i = 0; i < list.size(); i++) {
            next = Math.max(next, idExtracter.applyAsInt(list.get(i)) + 1);
        }
        return next;
    }

    private static <E> int checkId(final String name, final List<E> list, final ToIntFunction<E> idExtracter, final int id) {
        for (int i = 0; i < list.size(); i++) {
            if (id == idExtracter.applyAsInt(list.get(i))) {
                throw new IllegalArgumentException("Duplicate " + name + " ID: " + id);
            }
        }
        return id;
    }

}
