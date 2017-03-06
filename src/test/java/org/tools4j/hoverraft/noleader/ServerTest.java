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
package org.tools4j.hoverraft.noleader;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ServerTest {

    private static final Random RND = new Random();

    @Test
    public void syncServers3() throws Exception {
        testServers3(new EmbeddedSyncTransport(), () -> null);
    }

    @Ignore //FIXME fails currently
    @Test
    public void asyncServers3() throws Exception {
        final int nThreads = 5;
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final EmbeddedAsyncTransport transport = new EmbeddedAsyncTransport(executorService);
        final Callable<Void> terminator = () -> {
            transport.shutdownAndWait(5, TimeUnit.SECONDS);
            return null;
        };
        testServers3(transport, terminator);
    }

    private void testServers3(final Transport transport, final Callable<Void> terminationWaiter) throws Exception {
        //given
        final int nServers = 3;
        final int nMessages = 100;
        final int nSources = 3;
        final int[] messageCounts = new int[nSources];

        final Transport spiedTransport = spy(transport);
        for (int serverId = 0; serverId < nServers; serverId++) {
            final Server server = new Server(serverId, nServers, spiedTransport);
        }

        //when
        for (int i = 0; i < nMessages; i++) {
            final int sourceId = RND.nextInt(nSources);
            final int messageId = i;
            spiedTransport.receiveInput(Message.create(sourceId, messageId));
            messageCounts[sourceId]++;
        }
        terminationWaiter.call();

        //then
        for (int sourceId = 0; sourceId < nSources; sourceId++) {
            final int srcId = sourceId;
            for (int serverId = 0; serverId < nServers; serverId++) {
                verify(spiedTransport, times(messageCounts[sourceId])).sendOutput(eq(serverId), anyLong(),
                        argThat(new BaseMatcher<Message>() {
                            @Override
                            public boolean matches(final Object item) {
                                return item instanceof Message && ((Message)item).sourceId() == srcId;
                            }

                            @Override
                            public void describeTo(final Description description) {
                                description.appendValue("Message[*,sourceId=" + srcId + "]");
                            }
                        }));
            }
        }
    }

}