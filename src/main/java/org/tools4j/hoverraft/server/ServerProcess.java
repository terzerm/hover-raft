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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ServerProcess {

    private enum Status {
        IDLE, RUNNING, TERMINATED;
    }

    private final Thread thread = new Thread(this::run);
    private final AtomicReference<Status> status = new AtomicReference<>(Status.IDLE);

    private final Server server;

    public ServerProcess(final Server server) {
        this.server = Objects.requireNonNull(server);
    }

    public void start() {
        if (status.compareAndSet(Status.IDLE, Status.RUNNING)) {
            thread.start();
        }
    }

    public boolean isRunning() {
        return status.get() == Status.RUNNING;
    }

    public boolean isTerminated() {
        return status.get() == Status.TERMINATED;
    }

    public void terminate() {
        status.compareAndSet(Status.RUNNING, Status.TERMINATED);
    }

    public void awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException{
        if (status.get() == Status.TERMINATED) {
            thread.join(unit.toMillis(timeout));
        } else {
            throw new IllegalStateException("status is not " + Status.TERMINATED);
        }
    }

    private void run() {
        while (isRunning()) {
            server.perform();
        }
    }
}
