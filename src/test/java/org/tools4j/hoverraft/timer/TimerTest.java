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
package org.tools4j.hoverraft.timer;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimerTest {

    private static final long MIN_TIMEOUT = 10;
    private static final long MAX_TIMEOUT = 20;

    @Mock
    private Clock clock;

    private Timer timer;

    @Before
    public void init() {
        when(clock.currentTimeMillis()).thenReturn(0L);
        timer = new Timer(clock);
        timer.restart(MIN_TIMEOUT, MAX_TIMEOUT);
        assertThatTimeoutIsBetweenMinAndMax(0);
    }

    @Test
    public void restart() throws Exception {
        //we check that restart sets a new random timeout
        //i.e. we try until the reset timeout was different from previous
        long previousTimeout = Long.MIN_VALUE;
        final long startTime = 1l;
        while (true) {
            when(clock.currentTimeMillis()).thenReturn(1L);
            timer.restart(MIN_TIMEOUT, MAX_TIMEOUT);
            assertThatTimeoutIsBetweenMinAndMax(startTime);
            final long currentTimeout = findTimeout(startTime);
            if (previousTimeout != Long.MIN_VALUE && previousTimeout != currentTimeout) {
                return;//test passed
            }
            previousTimeout = currentTimeout;
        }
    }

    @Test
    public void reset() throws Exception {
        //we check that reset does NOT set a new timeout
        //i.e. we try a couple of times and the timeout should always be the same
        long previousTimeout = Long.MIN_VALUE;
        long startTime = 1;
        for (int i = 0; i < 20; i++) {
            when(clock.currentTimeMillis()).thenReturn(startTime);
            timer.reset();
            assertThatTimeoutIsBetweenMinAndMax(startTime);
            final long currentTimeout = findTimeout(startTime);
            if (previousTimeout != Long.MIN_VALUE) {
                Assertions.assertThat(previousTimeout == currentTimeout);
            }
            previousTimeout = currentTimeout;
            startTime++;
        }
    }

    @Test
    public void timeoutNow() throws Exception {
        timer.timeoutNow();
        Assertions
                .assertThat(timer.hasTimeoutElapsed())
                .isTrue();
        when(clock.currentTimeMillis()).thenReturn(100L);
        timer.restart(MIN_TIMEOUT, MAX_TIMEOUT);
        Assertions
                .assertThat(timer.hasTimeoutElapsed())
                .isFalse();
        timer.timeoutNow();
        Assertions
                .assertThat(timer.hasTimeoutElapsed())
                .isTrue();
    }

    private void assertThatTimeoutIsBetweenMinAndMax(final long startTime) {
        when(clock.currentTimeMillis()).thenReturn(startTime + MIN_TIMEOUT - 1);
        Assertions
                .assertThat(timer.hasTimeoutElapsed())
                .isFalse();
        when(clock.currentTimeMillis()).thenReturn(startTime + MAX_TIMEOUT);
        Assertions
                .assertThat(timer.hasTimeoutElapsed())
                .isTrue();
    }

    private long findTimeout(final long startTime) {
        for (long i = MIN_TIMEOUT; i <= MAX_TIMEOUT; i++) {
            when(clock.currentTimeMillis()).thenReturn(startTime + i);
            if (timer.hasTimeoutElapsed()) {
                return i;
            }
        }
        throw new IllegalStateException("timeout should be in [" + MIN_TIMEOUT + ", " + MAX_TIMEOUT + "]");
    }

}