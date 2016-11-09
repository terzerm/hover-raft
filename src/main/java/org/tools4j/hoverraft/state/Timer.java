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

import org.tools4j.hoverraft.config.ConsensusConfig;
import org.tools4j.hoverraft.util.Clock;

import java.util.Random;

public final class Timer {

    private final Random rnd = new Random();
    private final long minTimeoutMillis;
    private final long maxTimeoutMillis;

    private long timerStartMillis;
    private long timeoutMillis;

    public Timer(final ConsensusConfig consensusConfig) {
        this(consensusConfig.minElectionTimeoutMillis(), consensusConfig.maxElectionTimeoutMillis());
    }

    public Timer(final long minTimeoutMillis, final long maxTimeoutMillis) {
        if (minTimeoutMillis > maxTimeoutMillis) {
            throw new IllegalArgumentException("minTimeoutMillis must not be greater than maxTimeoutMillis: " + minTimeoutMillis + " > " + maxTimeoutMillis);
        }
        if (maxTimeoutMillis - minTimeoutMillis > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Difference between minTimeoutMillis and maxTimeoutMillis exceeds integer range: " + maxTimeoutMillis + " - " + maxTimeoutMillis + " > " + Integer.MAX_VALUE);
        }
        this.minTimeoutMillis = minTimeoutMillis;
        this.maxTimeoutMillis = maxTimeoutMillis;
    }

    public long minTimeoutMillis() {
        return minTimeoutMillis;
    }

    public long maxTimeoutMillis() {
        return maxTimeoutMillis;
    }

    /**
     * Starts a new timeout. The timeout is random between minTimeout and maxTimeout.
     * @param clock the clock used to get the current time
     */
    public void restart(final Clock clock) {
        timeoutMillis = newTimeoutMillis();
        reset(clock);
    }

    /**
     * Resets the current timeout to the start without calculating a new
     * random timout.
     * @param clock the clock used to get the current time
     */
    public void reset(final Clock clock) {
        timerStartMillis = clock.currentTimeMillis();
    }

    /**
     * Forced timeout after receiving a DirectTimeoutNow.
     */
    public void timeoutNow() {
        timeoutMillis = 0;
    }

    /**
     * Returns true if the timeout has elapsed if compared with the current time.
     * @param clock the clock used to get the current time
     * @return true if timeout has elapsed
     */
    public boolean hasTimeoutElapsed(final Clock clock) {
        return clock.currentTimeMillis() - timerStartMillis >= timeoutMillis;
    }

    private long newTimeoutMillis() {
        final int diff = (int)(maxTimeoutMillis - minTimeoutMillis);
        long timeout = minTimeoutMillis;
        if (diff > 0) {
            timeout += rnd.nextInt(diff);
        }
        return timeout;
    }
}
