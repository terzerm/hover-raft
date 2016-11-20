package org.tools4j.hoverraft.state;

import java.util.Comparator;

public class LogEntryComparator implements Comparator<LogEntry> {

    @Override
    public int compare(final LogEntry logEntry1, final LogEntry logEntry2) {
        int termCompare = Integer.compare(logEntry1.term(), logEntry2.term());
        if (termCompare == 0) {
            return Long.compare(logEntry1.index(), logEntry2.index());
        } else {
            return termCompare;
        }
    }
}
