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
    //Including promote, demote and remove.
    EDIT_PERMISSIONS(4),
    //All GUI button interactions
    MANAGE_HUTS(5),
    RECEIVE_MESSAGES(6),
    USE_SCAN_TOOL(7),
    PLACE_BLOCKS(8),
    BREAK_BLOCKS(9),
    TOSS_ITEM(10),
    PICKUP_ITEM(11),
    FILL_BUCKET(12),
    OPEN_CONTAINER(13),
    RIGHTCLICK_BLOCK(14),
    RIGHTCLICK_ENTITY(15),
    THROW_POTION(16),
    SHOOT_ARROW(17),
    ATTACK_CITIZEN(18),
    ATTACK_ENTITY(19),
    //has access to allowed list, "hostile+" or "neutral+"
    ACCESS_FREE_BLOCKS(20),
    TELEPORT_TO_COLONY(21),
    EXPLODE(22),
    RECEIVE_MESSAGES_FAR_AWAY(23),
    CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY(24),
    RALLY_GUARDS(25),
    HURT_CITIZEN(26),
    HURT_VISITOR(27);

    private final int flag;

    /**
     * Stores the action as byte. {@link #ACCESS_HUTS} has value 0000 0000 has value 0100 0000
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
