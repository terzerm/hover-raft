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
import org.tools4j.hoverraft.message.Message;
import org.tools4j.hoverraft.message.simple.SimpleAppendRequest;
import org.tools4j.hoverraft.message.simple.SimpleAppendResponse;
import org.tools4j.hoverraft.transport.Sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppendRequestHandlerTest {

    //under test
    private AppendRequestHandler handler;

    private Server server;

    @Mock
    private Sender sender;

    @Before
    public void init() {
        server = Mockery.simple(1);

        handler = new AppendRequestHandler();
    }

    @Test
    public void onAppendRequest() throws Exception {
        //given
        final int term = server.currentTerm();
        final int serverId = server.id();
        final int leaderId = serverId + 1;
        final SimpleAppendRequest appendRequest = new SimpleAppendRequest()
                .term(term)
                .leaderId(leaderId);
        when(server.connections().serverSender(leaderId)).thenReturn(sender);

        //when
        handler.onAppendRequest(server, appendRequest);

        //then
        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(SimpleAppendResponse.class);
        final SimpleAppendResponse response = (SimpleAppendResponse)captor.getValue();
        assertThat(response.successful()).isTrue();
        assertThat(response.term()).isEqualTo(term);
    }

    @Test
    public void onAppendRequestWithWrongTerm() throws Exception {
        //given
        final int term = server.currentTerm();
        final int badTerm = term - 1;
        final int serverId = server.id();
        final int leaderId = serverId + 1;
        final SimpleAppendRequest appendRequest = new SimpleAppendRequest()
                .term(badTerm)
                .leaderId(leaderId);
        when(server.connections().serverSender(leaderId)).thenReturn(sender);

        //when
        handler.onAppendRequest(server, appendRequest);

        //then
        final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(sender).offer(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(SimpleAppendResponse.class);
        final SimpleAppendResponse response = (SimpleAppendResponse)captor.getValue();
        assertThat(response.successful()).isFalse();
        assertThat(response.term()).isEqualTo(term);
    }
}