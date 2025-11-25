package com.minecolonies.api.colony.permissions;

/**
 * Actions that can be performed in a colony.
 */
public enum Action
{
    //counts for citizen and huts.
    ACCESS_HUTS(0, true),
    //If guards can attack, player can attack back
    GUARDS_ATTACK(1, false),
    PLACE_HUTS(2, true),
    BREAK_HUTS(3, true),
    //Including promote, demote and remove.
    EDIT_PERMISSIONS(4, true),
    //All GUI button interactions
    MANAGE_HUTS(5, true),
    RECEIVE_MESSAGES(6, false),
    USE_SCAN_TOOL(7, true),
    PLACE_BLOCKS(8, true),
    BREAK_BLOCKS(9, true),
    TOSS_ITEM(10, true),
    PICKUP_ITEM(11, true),
    FILL_BUCKET(12, true),
    OPEN_CONTAINER(13, true),
    RIGHTCLICK_BLOCK(14, true),
    RIGHTCLICK_ENTITY(15, true),
    THROW_POTION(16, true),
    SHOOT_ARROW(17, true),
    ATTACK_CITIZEN(18, true),
    ATTACK_ENTITY(19, true),
    //has access to allowed list, "hostile+" or "neutral+"
    ACCESS_FREE_BLOCKS(20, true),
    TELEPORT_TO_COLONY(21, true),
    EXPLODE(22, true),
    RECEIVE_MESSAGES_FAR_AWAY(23, false),
    CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY(24, false),
    RALLY_GUARDS(25, true),
    HURT_CITIZEN(26, true),
    HURT_VISITOR(27, true),
    MAP_BORDER(28, true),
    MAP_DEATHS(29, true),
    ACCESS_TOGGLEABLES(30, true);

    // remember to update permissionsVersion and add some upgrade logic in upgradePermissions if you
    // add new actions that shouldn't just be off by default for everyone

    private final long    flag;
    private final boolean allowsOperatorBypass;

    /**
     * Stores the action as byte. {@link #ACCESS_HUTS} has value 0000 0000 has value 0100 0000
     *
     * @param bit                  how many bits should be shifted and set
     * @param allowsOperatorBypass whether this action may be bypassed by server operators.
     */
    Action(final int bit, final boolean allowsOperatorBypass)
    {
        this.flag = 0x1L << bit;
        this.allowsOperatorBypass = allowsOperatorBypass;
    }

    public long getFlag()
    {
        return flag;
    }

    public boolean allowsOperatorBypass()
    {
        return allowsOperatorBypass;
    }
}
