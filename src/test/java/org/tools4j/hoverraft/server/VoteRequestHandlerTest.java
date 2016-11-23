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
package org.tools4j.hoverraft.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tools4j.hoverraft.event.VoteRequestHandler;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.VoteRequest;
import org.tools4j.hoverraft.message.VoteResponse;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.state.CommandLog;
import org.tools4j.hoverraft.state.LogEntry;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.state.Role;
import org.tools4j.hoverraft.transport.Sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VoteRequestHandlerTest {

    private static final boolean GRANTED = true;
    private static final boolean REJECTED = false;

    //under test
    private VoteRequestHandler handler;

    private ServerContext serverContext;
    private PersistentState persistentState;

    @Mock
    private Sender<Message> sender;

    @Before
    public void init() {
        serverContext = Mockery.simple(1);
        persistentState = Mockery.persistentState();

        handler = new VoteRequestHandler(persistentState);
    }

    private int candidateId() {
        return serverContext.id() + 1;
    }

    private int differentCandidateId() {
        return serverContext.id() + 2;
    }

    @Test
    public void onVoteRequest_roleCandidate() throws Exception {
        onVoteRequest(Role.CANDIDATE, PersistentState.NOT_VOTED_YET);
    }

    @Test
    public void onVoteRequest_roleFollower() throws Exception {
        onVoteRequest(Role.FOLLOWER, PersistentState.NOT_VOTED_YET);
    }

    @Test
    public void onVoteRequest_roleLeader() throws Exception {
        onVoteRequest(Role.LEADER, PersistentState.NOT_VOTED_YET);
    }

    @Test
    public void onVoteRequest_votedForSameCandidate() throws Exception {
        onVoteRequest(Role.FOLLOWER, candidateId());
    }

    @Test
    public void onVoteRequest_votedForDifferentCandidate() throws Exception {
        onVoteRequest(Role.FOLLOWER, differentCandidateId());
    }

    private void onVoteRequest(final Role currentRole, final int previouslyVotedFor) throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final boolean granted = previouslyVotedFor == PersistentState.NOT_VOTED_YET || previouslyVotedFor == candidateId;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId);
        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(lastLogTerm)
                .index(lastLogIndex);

        when(serverContext.connections().serverSender(candidateId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(previouslyVotedFor);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, granted);
    }


    //FIXMe extract below scenarios to test log.compare(logEntry)
    @Test
    public void onVoteRequest_validCandidate_newerLastLogTerm() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final int newerLastLogTerm = lastLogTerm + 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId);
        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(newerLastLogTerm)
                .index(lastLogIndex);

        when(serverContext.connections().serverSender(candidateId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, GRANTED);
    }

    @Test
    public void onVoteRequest_validCandidate_newerLastLogIndex() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final long newerLastLogIndex = lastLogIndex + 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId);

        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(lastLogTerm)
                .index(newerLastLogIndex);

        final CommandLog commandLog = persistentState.commandLog();

        when(serverContext.connections().serverSender(candidateId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, GRANTED);
    }

    @Test
    public void onVoteRequest_wrongTerm() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int badTerm = term - 1;
        final int serverId = serverContext.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(badTerm)
                .candidateId(serverId);
        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(lastLogTerm)
                .index(lastLogIndex);

        when(serverContext.connections().serverSender(serverId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    @Test
    public void onVoteRequest_invalidCandidate_badLastLogTerm() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int serverId = serverContext.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final int badLastLogTerm = lastLogTerm - 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(serverId);
        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(badLastLogTerm)
                .index(lastLogIndex);

        when(serverContext.connections().serverSender(serverId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    @Test
    public void onVoteRequest_invalidCandidate_badLastLogIndex() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int serverId = serverContext.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final long badLastLogIndex = lastLogIndex - 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(serverId);
        //need LogEntry mutators to return parent object on done()
        voteRequest.lastLogEntry()
                .term(lastLogTerm)
                .index(badLastLogIndex);

        when(serverContext.connections().serverSender(serverId)).thenReturn(sender);
        when(persistentState.votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(persistentState.commandLog().lastEntry().term()).thenReturn(lastLogTerm);
        when(persistentState.commandLog().lastEntry().index()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    private void onVoteRequest(final int term,
                               final VoteRequest voteRequest,
                               final boolean expectGranted) {

        //when
        handler.onVoteRequest(serverContext, voteRequest);

        //then
        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(VoteResponse.class);
        final VoteResponse response = (VoteResponse)captor.getValue();
        assertThat(response.voteGranted()).isEqualTo(expectGranted);
        assertThat(response.term()).isEqualTo(term);
    }
}