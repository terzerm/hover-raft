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

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EmbeddedAsyncTransport implements Transport {

    private final OutputConsumer outputConsumer;
    private final ExecutorService executorService;
    private final AtomicInteger messageCount = new AtomicInteger();
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final Map<Consumer<? super Envelope>, Queue<Envelope>> envelopeConsumersWithQueue = new ConcurrentHashMap<>();
    private final Map<Consumer<? super Message>, ConcurrentMap<Integer, Queue<Message>>> messageConsumersWithQueuePerSourceId = new ConcurrentHashMap<>();

    public EmbeddedAsyncTransport(final ExecutorService executorService) {
        this(OutputConsumer.SYSTEM_OUT, executorService);
    }
    public EmbeddedAsyncTransport(final OutputConsumer outputConsumer, final ExecutorService executorService) {
        this.outputConsumer = Objects.requireNonNull(outputConsumer);
        this.executorService = Objects.requireNonNull(executorService);
    }

    public void shutdownAndWait(final int timeout, final TimeUnit unit) throws TimeoutException, InterruptedException {
        if (shutdownLatch.await(timeout, unit)) {
            executorService.shutdown();
            if (!executorService.awaitTermination(timeout, unit)) {
                throw new TimeoutException("shutdown timeout after " + timeout + " " + unit);
            }
            System.out.println(getClass().getSimpleName() + ": Shutdown succeeded");
        } else {
            executorService.shutdownNow();
            throw new TimeoutException("shutdown latch timeout after " + timeout + " " + unit);
        }
    }

    @Override
    public void addServerListener(final Consumer<? super Envelope> envelopeConsumer) {
        envelopeConsumersWithQueue.put(envelopeConsumer, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void addSourceListener(final Consumer<? super Message> messageConsumer) {
        messageConsumersWithQueuePerSourceId.put(messageConsumer, new ConcurrentHashMap<>());
    }

    @Override
    public void notifyServers(final Envelope envelope) {
        for (final Map.Entry<Consumer<? super Envelope>, Queue<Envelope>> e : envelopeConsumersWithQueue.entrySet()) {
            final Consumer<? super Envelope> consumer = e.getKey();
            final Queue<Envelope> queue = e.getValue();
            queue.add(envelope);
            executorService.submit(() -> consumer.accept(queue.poll()));
        }
    }

    @Override
    public void receiveInput(final Message message) {
        for (final Map.Entry<Consumer<? super Message>, ConcurrentMap<Integer, Queue<Message>>> e : messageConsumersWithQueuePerSourceId.entrySet()) {
            final Consumer<? super Message> consumer = e.getKey();
            final ConcurrentMap<Integer, Queue<Message>> queueBySourceId = e.getValue();
            final Queue<Message> queue = queueBySourceId.computeIfAbsent(message.sourceId(), k -> new ConcurrentLinkedQueue<>());
            queue.add(message);
            messageCount.incrementAndGet();
            executorService.submit(() -> {
                consumer.accept(queue.poll());
                if (0 == messageCount.decrementAndGet()) {
                    shutdownLatch.countDown();
                }
            });
        }
    }

    @Override
    public void sendOutput(final int serverId, final long sequenceNo, final Message message) {
        outputConsumer.onOutput(serverId, sequenceNo, message);
    }
}
