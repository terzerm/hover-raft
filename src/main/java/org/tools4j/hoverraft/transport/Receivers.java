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
package org.tools4j.hoverraft.transport;

import org.tools4j.hoverraft.direct.DirectPayload;
import org.tools4j.hoverraft.message.Message;

import java.util.function.Consumer;

public final class Receivers {

    public static <M extends DirectPayload> Receiver<M> roundRobinReceiver(final Receiver<? extends M>... receivers) {
        return new Receiver<M>() {
            private int index = -1;

            @Override
            public int poll(final Consumer<? super M> messageMandler, final int limit) {
                final Receiver<? extends M>[] r = receivers;
                final int len = r.length;
                int cnt = 0;
                for (int i = 0; i < len && cnt < limit; i++) {
                    index++;
                    if (index >= len) {
                        index -= len;
                    }
                    cnt += r[index].poll(messageMandler, limit - cnt);
                }
                return cnt;
            }
        };
    }

}
