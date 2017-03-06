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

import org.agrona.collections.Int2ObjectHashMap;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Server {

    private final int serverId;
    private final int serverCount;
    private final Transport transport;
    private final AtomicReference<Envelope> currentEnvelope = new AtomicReference<>();
    private final Int2ObjectHashMap<AtomicLong> lastSentMessageIdPerSourceId = new Int2ObjectHashMap<>();
    private final Deque<Message> messagesToProcess = new ConcurrentLinkedDeque<>();
    private final AtomicLong sequenceNo = new AtomicLong();

    public Server(final int serverId, final int serverCount, final Transport transport) {
        this.serverId = serverId;
        this.serverCount = serverCount;
        this.transport = Objects.requireNonNull(transport);
        this.transport.addSourceListener(this::onNewSourceMessage);
        this.transport.addServerListener(this::onEnvelope);
    }

    public void awaitTermination(final long timeout, final TimeUnit unit) throws TimeoutException, InterruptedException {
        final long timeoutMillis = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!messagesToProcess.isEmpty() || currentEnvelope.get() != null) {
            Thread.sleep(100);
            if (System.currentTimeMillis() > timeoutMillis) {
                throw new TimeoutException("not terminated after " + timeout + " " + unit);
            }
        }
    }

    private synchronized void onNewSourceMessage(final Message message) {
        final AtomicLong lastMessageId = lastSentMessageIdPerSourceId.get(message.sourceId());
        if (lastMessageId == null || lastMessageId.get() < message.messageId()) {
            if (currentEnvelope.get() == null) {
                newEnvelopeForMessage(message);
            } else {
                messagesToProcess.add(message);
            }
        }//else: we have already handled this message
    }

    private synchronized void onEnvelope(final Envelope envelope) {
        if (envelope.senderServerId() == serverId) {
            //don't react to our own envelope
//          FIXME return;
            System.gc();
        }
        long seq = sequenceNo.get();
        if (seq > envelope.sequenceNo()) {
            //ignore envelope, it is outdated
            return;
        }
        if (seq < envelope.sequenceNo()) {
            //if we have a current envelope, send it now as it has obviously been agreed
            final Envelope toSend = currentEnvelope.getAndSet(null);
            if (toSend != null) {
                transport.sendOutput(serverId, toSend.sequenceNo(), toSend.message());
                updateLastSentMessage(toSend.message());
            }
            seq = sequenceNo.incrementAndGet();
        }
        if (seq < envelope.sequenceNo()) {
            handleMissingMessage();
        } else {
            onEnvelopeWithSameSequence(envelope);
        }
    }
    private void onEnvelopeWithSameSequence(final Envelope envelope) {
        if (isMajority(envelope)) {
            handleMajorityEnvelope(envelope);
        } else {
            handleMinorityEnvelope(envelope);
        }
    }
    private void handleMinorityEnvelope(final Envelope envelope) {
        //no majority yet, send envelope to other servers
        final Envelope myEnvelope = currentEnvelope.get();
        if (myEnvelope == null) {
            final Envelope confirmation = envelope.confirm(serverId);
            currentEnvelope.set(confirmation);
            transport.notifyServers(confirmation);
        } else {
            if (myEnvelope == envelope) {
                return;
                //throw new IllegalArgumentException(serverId + " / " + envelope);
            }
            if (isMajority(myEnvelope) || myEnvelope.message().sourceId() < envelope.message().sourceId()) {
                //we win, don't confirm
                transport.notifyServers(myEnvelope);
            } else {
                //envelope wins, confirm
                if (!myEnvelope.message().equals(envelope.message())) {
                    messagesToProcess.addFirst(myEnvelope.message());
                }
                final Envelope confirmation = envelope.confirm(serverId);
                currentEnvelope.set(confirmation);
                transport.notifyServers(confirmation);
            }
        }
    }

    private void handleMajorityEnvelope(final Envelope envelope) {
        //majority, send envelope now
        transport.sendOutput(serverId, envelope.sequenceNo(), envelope.message());
        updateLastSentMessage(envelope.message());
        final Envelope old = currentEnvelope.getAndSet(null);
        sequenceNo.incrementAndGet();
        if (old == null || old.message().equals(envelope.message())) {
            scheduleNext();
        } else {
            newEnvelopeForMessage(old.message());
        }
    }

    private void updateLastSentMessage(final Message message) {
        lastSentMessageIdPerSourceId.computeIfAbsent(message.sourceId(), AtomicLong::new)
                .set(message.messageId());
    }

    private boolean isMajority(final Envelope envelope) {
        return isMajority(envelope.serverIds().size());
    }
    private boolean isMajority(final int count) {
        return count * 2 > serverCount;
    }

    private void scheduleNext() {
        final Message message = messagesToProcess.poll();
        if (message != null) {
            newEnvelopeForMessage(message);
        }
    }

    private void newEnvelopeForMessage(final Message message) {
        final Envelope envelope = Envelope.create(sequenceNo.get(), message, serverId);
        currentEnvelope.set(envelope);
        messagesToProcess.remove(message);
        transport.notifyServers(envelope);
    }

    private void handleMissingMessage() {
        throw new IllegalStateException("not implemented: handling of missing messaage");
    }
}
