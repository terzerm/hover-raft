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
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
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

    private Server server;

    @Mock
    private Sender<Message> sender;

    @Before
    public void init() {
        server = Mockery.simple(1);

        handler = new VoteRequestHandler();
    }

    private int candidateId() {
        return server.id() + 1;
    }

    private int differentCandidateId() {
        return server.id() + 2;
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
        final int term = server.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final boolean granted = previouslyVotedFor == PersistentState.NOT_VOTED_YET || previouslyVotedFor == candidateId;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId)
                .lastLogTerm(lastLogTerm)
                .lastLogIndex(lastLogIndex);
        server.state().volatileState().changeRoleTo(currentRole);
        when(server.connections().serverSender(candidateId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(previouslyVotedFor);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, granted);
    }

    @Test
    public void onVoteRequest_validCandidate_newerLastLogTerm() throws Exception {
        //given
        final int term = server.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final int newerLastLogTerm = lastLogTerm + 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId)
                .lastLogTerm(newerLastLogTerm)
                .lastLogIndex(lastLogIndex);
        server.state().volatileState().changeRoleTo(Role.FOLLOWER);
        when(server.connections().serverSender(candidateId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, GRANTED);
    }

    @Test
    public void onVoteRequest_validCandidate_newerLastLogIndex() throws Exception {
        //given
        final int term = server.currentTerm();
        final int candidateId = candidateId();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final long newerLastLogIndex = lastLogIndex + 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(candidateId)
                .lastLogTerm(lastLogTerm)
                .lastLogIndex(newerLastLogIndex);
        server.state().volatileState().changeRoleTo(Role.FOLLOWER);
        when(server.connections().serverSender(candidateId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, GRANTED);
    }

    @Test
    public void onVoteRequest_wrongTerm() throws Exception {
        //given
        final int term = server.currentTerm();
        final int badTerm = term - 1;
        final int serverId = server.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(badTerm)
                .candidateId(serverId)
                .lastLogTerm(lastLogTerm)
                .lastLogIndex(lastLogIndex);
        when(server.connections().serverSender(serverId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    @Test
    public void onVoteRequest_invalidCandidate_badLastLogTerm() throws Exception {
        //given
        final int term = server.currentTerm();
        final int serverId = server.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final int badLastLogTerm = lastLogTerm - 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(serverId)
                .lastLogTerm(badLastLogTerm)
                .lastLogIndex(lastLogIndex);
        when(server.connections().serverSender(serverId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    @Test
    public void onVoteRequest_invalidCandidate_badLastLogIndex() throws Exception {
        //given
        final int term = server.currentTerm();
        final int serverId = server.id();
        final int lastLogTerm = term;
        final long lastLogIndex = 1234;
        final long badLastLogIndex = lastLogIndex - 1;
        final VoteRequest voteRequest = DirectMessageFactory.createForWriting()
                .voteRequest()
                .term(term)
                .candidateId(serverId)
                .lastLogTerm(lastLogTerm)
                .lastLogIndex(badLastLogIndex);
        when(server.connections().serverSender(serverId)).thenReturn(sender);
        when(server.state().persistentState().votedFor()).thenReturn(PersistentState.NOT_VOTED_YET);
        when(server.state().persistentState().lastLogTerm()).thenReturn(lastLogTerm);
        when(server.state().persistentState().lastLogIndex()).thenReturn(lastLogIndex);

        //when + then
        onVoteRequest(term, voteRequest, REJECTED);
    }

    private void onVoteRequest(final int term,
                               final VoteRequest voteRequest,
                               final boolean expectGranted) {

        //when
        handler.onVoteRequest(server, voteRequest);

        //then
        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(VoteResponse.class);
        final VoteResponse response = (VoteResponse)captor.getValue();
        assertThat(response.voteGranted()).isEqualTo(expectGranted);
        assertThat(response.term()).isEqualTo(term);
    }
}