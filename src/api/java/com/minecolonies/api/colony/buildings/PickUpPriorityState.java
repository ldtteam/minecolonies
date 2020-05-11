package com.minecolonies.api.colony.buildings;

public enum PickUpPriorityState
{
    NEVER(0),
    STATIC(1),
    AUTOMATIC(2);

    /**
     * The internal serializable int-representation of this enum.
     * We could use Java's ordinal(), but that is VERY error-prone.
     * So, do our own instead.
     */
    private final int intRepresentation;

    PickUpPriorityState(final int intRepresentation) {
        this.intRepresentation = intRepresentation;
    }

    /**
     * The integer representation for this enum. Used for message serialization.
     * @return The integer representation
     */
    public int getIntRepresentation() {
        return intRepresentation;
    }

    /**
     * Creates the enum from an int. Used for message deserialization.
     * This is not DRY, but we're talking about 3 to maybe later 4 values here.
     * @param intRepresentation The internal representation of the enum.
     * @return The corresponding enum.
     */
    public static PickUpPriorityState fromIntRepresentation(int intRepresentation) {
        switch (intRepresentation) {
            case 0:
                return NEVER;
            case 1:
                return STATIC;
            case 2:
                return AUTOMATIC;
            default:
                return null;
        }
    }
}
