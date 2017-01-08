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
package org.tools4j.hoverraft.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tools4j.hoverraft.command.Command;
import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.event.HigherTermHandler;
import org.tools4j.hoverraft.message.*;
import org.tools4j.hoverraft.state.PersistentState;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HigherTermHandlerTest {

    //under test
    private EventHandler handler;

    private ServerContext serverContext;
    private PersistentState persistentState;

    @Mock
    private VoteRequest voteRequest;
    @Mock
    private VoteResponse voteResponse;
    @Mock
    private AppendRequest appendRequest;
    @Mock
    private AppendResponse appendResponse;
    @Mock
    private TimeoutNow timeoutNow;
    @Mock
    private Command command;

    @Before
    public void init() {
        serverContext = Mockery.simple(1);
        persistentState = Mockery.persistentState();

        //under test
        handler = new HigherTermHandler(persistentState);
    }

    @Test
    public void onVoteRequest() throws Exception {
        final int term = 43;
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(voteRequest.term()).thenReturn(term - 1);
        handler.onVoteRequest(serverContext, voteRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(voteRequest.term()).thenReturn(term);
        handler.onVoteRequest(serverContext, voteRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(voteRequest.term()).thenReturn(term + 1);
        handler.onVoteRequest(serverContext, voteRequest);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 1);
    }

    @Test
    public void onVoteResponse() throws Exception {
        final int term = 3;
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(voteResponse.term()).thenReturn(term - 2);
        handler.onVoteResponse(serverContext, voteResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(voteResponse.term()).thenReturn(term);
        handler.onVoteResponse(serverContext, voteResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(voteResponse.term()).thenReturn(term + 2);
        handler.onVoteResponse(serverContext, voteResponse);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 2);
    }

    @Test
    public void onAppendRequest() throws Exception {
        final int term = 33;
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(appendRequest.term()).thenReturn(term - 3);
        handler.onAppendRequest(serverContext, appendRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(appendRequest.term()).thenReturn(term);
        handler.onAppendRequest(serverContext, appendRequest);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(appendRequest.term()).thenReturn(term + 100);
        handler.onAppendRequest(serverContext, appendRequest);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 100);
    }

    @Test
    public void onAppendResponse() throws Exception {
        final int term = 101;
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(appendResponse.term()).thenReturn(-99);
        handler.onAppendResponse(serverContext, appendResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(appendResponse.term()).thenReturn(term);
        handler.onAppendResponse(serverContext, appendResponse);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(appendResponse.term()).thenReturn(10001);
        handler.onAppendResponse(serverContext, appendResponse);
        verify(persistentState).clearVotedForAndSetCurrentTerm(10001);
    }

    @Test
    public void onTimeoutNow() throws Exception {
        final int term = 43;
        when(persistentState.currentTerm()).thenReturn(term);

        //message term < current term
        when(timeoutNow.term()).thenReturn(term - 1);
        handler.onTimeoutNow(serverContext, timeoutNow);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term == current term
        when(timeoutNow.term()).thenReturn(term);
        handler.onTimeoutNow(serverContext, timeoutNow);
        verify(persistentState, never()).clearVotedForAndSetCurrentTerm(anyInt());

        //message term > current term
        when(timeoutNow.term()).thenReturn(term + 1);
        handler.onTimeoutNow(serverContext, timeoutNow);
        verify(persistentState).clearVotedForAndSetCurrentTerm(term + 1);
    }

}