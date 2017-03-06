package org.tools4j.hoverraft.noleader;

@FunctionalInterface
public interface OutputConsumer {

    void onOutput(int serverId, long sequenceNo, Message message);

    default OutputConsumer andThen(final OutputConsumer next) {
        return (srv, seq, msg) -> {
            OutputConsumer.this.onOutput(srv, seq, msg);
            next.onOutput(srv, seq, msg);
        };
    }

    OutputConsumer SYSTEM_OUT = (srv, seq, msg) -> {System.out.println("[" + srv + "] " + seq + ":\t" + msg);};
}
