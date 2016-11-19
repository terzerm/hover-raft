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

import org.tools4j.hoverraft.event.Event;
import org.tools4j.hoverraft.event.EventHandler;
import org.tools4j.hoverraft.server.ServerContext;

/**
 * Transition from current state, either STEADY (no change) or into a new {@link Role}.
 */
public enum Transition implements Event {
    STEADY(null, false),
    TO_FOLLOWER(Role.FOLLOWER, true),
    TO_CANDIDATE(Role.CANDIDATE, false),
    TO_LEADER(Role.LEADER, false);

    private final Role targetRole;
    private final boolean replayEvent;

    Transition(final Role targetRole, final boolean replayEvent) {
        this.targetRole = targetRole;//nullable
        this.replayEvent = replayEvent;
    }

    public final Role targetRole(final Role currentRole) {
        return this == STEADY ? currentRole : targetRole;
    }

    public final boolean replayEvent() {
        return replayEvent;
    }

    @Override
    public Transition accept(final ServerContext serverContext, final EventHandler eventHandler) {
        return eventHandler.onTransition(serverContext, this);
    }

    public static Transition startWith(final ServerContext serverContext, final Event event, final EventHandler eventHandler) {
        return event.accept(serverContext, eventHandler);
    }
    public final Transition ifSteadyThen(final ServerContext serverContext, final Event event, final EventHandler eventHandler) {
        if (this == STEADY) {
            return event.accept(serverContext, eventHandler);
        }
        return this;
    }
}
