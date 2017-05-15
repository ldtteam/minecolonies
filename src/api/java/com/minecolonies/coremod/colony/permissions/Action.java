package com.minecolonies.coremod.colony.permissions;

/**
 * Actions that can be performed in a colony.
 */
public enum Action
{
    ACCESS_HUTS(0),
    GUARDS_ATTACK(1),
    PLACE_HUTS(2),
    BREAK_HUTS(3),
    CAN_PROMOTE(4),
    CAN_DEMOTE(5),
    SEND_MESSAGES(6),
    EDIT_PERMISSIONS(7),
    MANAGE_HUTS(8);

    private final int flag;

    /**
     * Stores the action as byte.
     * {@link #ACCESS_HUTS} has value 0000 0000
     * {@link #SEND_MESSAGES} has value 0100 0000
     *
     * @param bit how many bits should be shifted and set
     */
    Action(final int bit)
    {
        this.flag = 0x1 << bit;
    }

    public int getFlag()
    {
        return flag;
    }
}
