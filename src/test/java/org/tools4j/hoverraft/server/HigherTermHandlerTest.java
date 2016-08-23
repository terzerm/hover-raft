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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.message.direct.DirectMessage;
import org.tools4j.hoverraft.state.PersistentState;
import org.tools4j.hoverraft.transport.Connections;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HigherTermHandlerTest {

    private static final int SERVER_ID = 12345;
    private static final int TERM = 42;

    private Server<DirectMessage> server;

    @Mock
    private MessageFactory<DirectMessage> messageFactory;
    @Mock
    private Connections<DirectMessage> connections;

    @Mock
    private VoteRequest<DirectMessage> voteRequest;
    @Mock
    private VoteResponse<DirectMessage> voteResponse;
    @Mock
    private AppendRequest<DirectMessage> appendRequest;
    @Mock
    private AppendResponse<DirectMessage> appendResponse;
    @Mock
    private TimeoutNow<DirectMessage> timeoutNow;
    @Mock
    private CommandMessage<DirectMessage> commandMessage;

    //under test
    private MessageHandler handler;

    @Before
    public void init() {
        server = Mockery.direct(1);

        //under test
        handler = new HigherTermHandler();
    }

    @Test
    public void onVoteRequest() throws Exception {
        final int term = 43;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(voteRequest.term()).thenReturn(term - 1);
        handler.onVoteRequest(server, voteRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(voteRequest.term()).thenReturn(term);
        handler.onVoteRequest(server, voteRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(voteRequest.term()).thenReturn(term + 1);
        handler.onVoteRequest(server, voteRequest);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 1);
    }

    @Test
    public void onVoteResponse() throws Exception {
        final int term = 3;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(voteResponse.term()).thenReturn(term - 2);
        handler.onVoteResponse(server, voteResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(voteResponse.term()).thenReturn(term);
        handler.onVoteResponse(server, voteResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(voteResponse.term()).thenReturn(term + 2);
        handler.onVoteResponse(server, voteResponse);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 2);
    }

    @Test
    public void onAppendRequest() throws Exception {
        final int term = 33;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(appendRequest.term()).thenReturn(term - 3);
        handler.onAppendRequest(server, appendRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(appendRequest.term()).thenReturn(term);
        handler.onAppendRequest(server, appendRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(appendRequest.term()).thenReturn(term + 100);
        handler.onAppendRequest(server, appendRequest);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 100);
    }

    @Test
    public void onAppendResponse() throws Exception {
        final int term = 101;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(appendResponse.term()).thenReturn(-99);
        handler.onAppendResponse(server, appendResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(appendResponse.term()).thenReturn(term);
        handler.onAppendResponse(server, appendResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(appendResponse.term()).thenReturn(10001);
        handler.onAppendResponse(server, appendResponse);
        verify(persistentState).clearVotedForAndSetCurrentTerm(10001);
    }

    @Test
    public void onTimeoutNow() throws Exception {
        final int term = 43;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(timeoutNow.term()).thenReturn(term - 1);
        handler.onTimeoutNow(server, timeoutNow);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(timeoutNow.term()).thenReturn(term);
        handler.onTimeoutNow(server, timeoutNow);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(timeoutNow.term()).thenReturn(term + 1);
        handler.onTimeoutNow(server, timeoutNow);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 1);
    }

    @Test
    public void onCommandMessage() throws Exception {
        final int term = 42;
        final PersistentState persistentState = server.state().persistentState();
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(commandMessage.term()).thenReturn(term - 1);
        handler.onCommandMessage(server, commandMessage);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(commandMessage.term()).thenReturn(term);
        handler.onCommandMessage(server, commandMessage);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(commandMessage.term()).thenReturn(term + 1);
        handler.onCommandMessage(server, commandMessage);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 1);
    }

}