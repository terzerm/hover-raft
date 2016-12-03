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

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.hoverraft.command.CommandLog;
import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.config.ServerConfig;
import org.tools4j.hoverraft.util.Files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class DirectPersistentState implements PersistentState {

    private static final int STATE_SIZE = 4 + 4;

    private final MutableDirectBuffer state;
    private final CommandLog commandLog;

    public DirectPersistentState(final ServerConfig serverConfig, final ConsensusConfig consensusConfig) throws IOException {
        this.state = initState(serverConfig, consensusConfig);
        this.commandLog = initCommandLog(serverConfig, consensusConfig);
    }

    public int currentTerm() {
        return state.getInt(0);
    }

    public int votedFor() {
        return state.getInt(4);
    }

    @Override
    public CommandLog commandLog() {
        return commandLog;
    }

    public int clearVotedForAndSetCurrentTerm(int term) {
        state.putInt(0, term);
        state.putInt(4, NOT_VOTED_YET);
        return term;
    }

    public int clearVotedForAndIncCurrentTerm() {
        final int term = currentTerm() + 1;
        state.putInt(0, term);
        state.putInt(4, NOT_VOTED_YET);
        return term;
    }

    public void votedFor(final int candidateId) {
        state.putInt(4, candidateId);
    }


    private static MutableDirectBuffer initState(final ServerConfig serverConfig, final ConsensusConfig consensusConfig) throws IOException {
        final String path = Files.fileDirectory();
        final String file = Files.fileName(serverConfig.id(), "persistentState");
        final RandomAccessFile raf = new RandomAccessFile(new File(path, file), "rw");
        final ByteBuffer byteBuffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, STATE_SIZE);
        return new UnsafeBuffer(byteBuffer);
    }

    private static CommandLog initCommandLog(final ServerConfig serverConfig, final ConsensusConfig consensusConfig) throws IOException {
        return Files.commandLog(serverConfig.id(), "commandlog");
    }

}
