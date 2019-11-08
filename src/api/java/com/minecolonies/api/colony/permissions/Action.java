package com.minecolonies.api.colony.permissions;

/**
 * Actions that can be performed in a colony.
 */
public enum Action
{
    //counts for citizen and huts.
    ACCESS_HUTS(0),
    //If guards can attack, player can attack back
    GUARDS_ATTACK(1),
    PLACE_HUTS(2),
    BREAK_HUTS(3),
    CAN_PROMOTE(4),
    CAN_DEMOTE(5),
    SEND_MESSAGES(6),
    //Including promote, demote and remove.
    EDIT_PERMISSIONS(7),
    //All GUI button interactions
    MANAGE_HUTS(8),
    RECEIVE_MESSAGES(9),
    USE_SCAN_TOOL(10),
    PLACE_BLOCKS(11),
    BREAK_BLOCKS(12),
    TOSS_ITEM(13),
    PICKUP_ITEM(14),
    FILL_BUCKET(15),
    OPEN_CONTAINER(16),
    RIGHTCLICK_BLOCK(17),
    RIGHTCLICK_ENTITY(18),
    THROW_POTION(19),
    SHOOT_ARROW(20),
    ATTACK_CITIZEN(21),
    ATTACK_ENTITY(22),
    //has access to allowed list, "hostile+" or "neutral+"
    ACCESS_FREE_BLOCKS(23),
    TELEPORT_TO_COLONY(24),
    EXPLODE(25),
    RECEIVE_MESSAGES_FAR_AWAY(26),
    CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY(27);

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
