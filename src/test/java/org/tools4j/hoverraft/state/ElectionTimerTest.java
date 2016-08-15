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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.hoverraft.util.Clock;

public class ElectionTimerTest {

    private static final long MIN_TIMEOUT = 10;
    private static final long MAX_TIMEOUT = 20;

    private ElectionTimer electionTimer;

    @Before
    public void init() {
        electionTimer = new ElectionTimer(MIN_TIMEOUT, MAX_TIMEOUT);
        electionTimer.restart(Clock.fixed(0));
        assertThatTimeoutIsBetweenMinAndMax(0);
    }

    @Test
    public void restart() throws Exception {
        //we check that restart sets a new random timeout
        //i.e. we try until the reset timeout was different from previous
        long previousTimeout = Long.MIN_VALUE;
        long startTime = 1;
        while (true) {
            electionTimer.restart(Clock.fixed(startTime));
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
            electionTimer.reset(Clock.fixed(startTime));
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
        electionTimer.timeoutNow();
        Assertions
                .assertThat(electionTimer.hasTimeoutElapsed(Clock.fixed(0)))
                .isTrue();
        electionTimer.restart(Clock.fixed(100));
        Assertions
                .assertThat(electionTimer.hasTimeoutElapsed(Clock.fixed(100)))
                .isFalse();
        electionTimer.timeoutNow();
        Assertions
                .assertThat(electionTimer.hasTimeoutElapsed(Clock.fixed(100)))
                .isTrue();
    }

    private void assertThatTimeoutIsBetweenMinAndMax(final long startTime) {
        Assertions
                .assertThat(electionTimer.hasTimeoutElapsed(Clock.fixed(startTime + MIN_TIMEOUT - 1)))
                .isFalse();
        Assertions
                .assertThat(electionTimer.hasTimeoutElapsed(Clock.fixed(startTime + MAX_TIMEOUT)))
                .isTrue();
    }

    private long findTimeout(final long startTime) {
        long currentTimeout = -1;
        for (long i = MIN_TIMEOUT; i <= MAX_TIMEOUT; i++) {
            if (electionTimer.hasTimeoutElapsed(Clock.fixed(startTime + i))) {
                return i;
            }
        }
        throw new IllegalStateException("timeout should be in [" + MIN_TIMEOUT + ", " + MAX_TIMEOUT + "]");
    }

}