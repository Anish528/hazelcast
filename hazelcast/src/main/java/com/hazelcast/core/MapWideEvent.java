package com.hazelcast.core;

/**
 * Used for map-wide events like {@link com.hazelcast.core.EntryEventType#EVICT_ALL}.
 */
public class MapWideEvent extends AbstractMapEvent {

    private static final long serialVersionUID = -4948640313865667023L;

    /**
     * Number of entries affected by this event.
     */
    private final int numberOfEntriesAffected;

    public MapWideEvent(Object source, Member member, int eventType, int numberOfEntriesAffected) {
        super(source, member, eventType);
        this.numberOfEntriesAffected = numberOfEntriesAffected;
    }

    /**
     * Returns the number of entries affected by this event.
     *
     * @return number of entries affected.
     */
    public int getNumberOfEntriesAffected() {
        return numberOfEntriesAffected;
    }


    @Override
    public String toString() {
        return "MapWideEvent{"
                + super.toString()
                + ", numberOfEntriesAffected=" + numberOfEntriesAffected +
                '}';
    }
}
