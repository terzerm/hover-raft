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
import org.tools4j.hoverraft.message.AppendRequest;
import org.tools4j.hoverraft.message.AppendResponse;
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.direct.DirectMessageFactory;
import org.tools4j.hoverraft.state.*;
import org.tools4j.hoverraft.transport.Sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FollowerStateTest {

    //under test
    private FollowerState followerState;

    private ServerContext serverContext;
    private PersistentState persistentState;
    private VolatileState volatileState;

    @Mock
    private Sender<Message> sender;

    @Before
    public void init() {
        serverContext = Mockery.simple(1);
        persistentState = serverContext.persistentState();
        volatileState = serverContext.volatileState();

        followerState = new FollowerState();
    }

    @Test
    public void onAppendRequest() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int serverId = serverContext.id();
        final int leaderId = serverId + 1;

        volatileState.commitIndex(50);

        final AppendRequest appendRequest = DirectMessageFactory.createForWriting()
                .appendRequest()
                .term(term)
                .leaderId(leaderId)
                .leaderCommit(50);


        appendRequest.commandLogEntry().index(101L)
                                       .term(term);

        //make appendRequest prevLogEntry to match the end of commandLog
        appendRequest.prevLogEntry().term(term)
                                    .index(100);

        when(persistentState.commandLog().contains(appendRequest.prevLogEntry())).thenReturn(CommandLog.CONTAINMENT.IN);

        when(serverContext.connections().serverSender(leaderId)).thenReturn(sender);

        //when
        final Transition transition = followerState.onEvent(serverContext, appendRequest);

        //then
        verify(persistentState.commandLog()).append(appendRequest.commandLogEntry());

        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(AppendResponse.class);
        final AppendResponse response = (AppendResponse)captor.getValue();
        assertThat(response.successful()).isTrue();
        assertThat(response.term()).isEqualTo(term);
        assertThat(transition).isEqualTo(Transition.STEADY);
    }

    @Test
    public void onAppendRequest_wrongTerm() throws Exception {
        //given
        final int term = persistentState.currentTerm();
        final int badTerm = term - 1;
        final int serverId = serverContext.id();
        final int leaderId = serverId + 1;
        final AppendRequest appendRequest = DirectMessageFactory.createForWriting()
                .appendRequest()
                .term(badTerm)
                .leaderId(leaderId);
        when(serverContext.connections().serverSender(leaderId)).thenReturn(sender);

        //when
        final Transition transition = followerState.onEvent(serverContext, appendRequest);

        //then
        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(AppendResponse.class);
        final AppendResponse response = (AppendResponse)captor.getValue();
        assertThat(response.successful()).isFalse();
        assertThat(response.term()).isEqualTo(term);
        assertThat(transition).isEqualTo(Transition.STEADY);
    }
}