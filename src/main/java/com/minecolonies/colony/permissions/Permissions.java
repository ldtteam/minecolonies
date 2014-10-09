package com.minecolonies.colony.permissions;

/**
 * CLASS DESCRIPTION
 * Created: October 08, 2014
 *
 * @author Colton
 */
public class Permissions {

    public enum Rank
    {
        OWNER,
        OFFICER,
        FRIEND,
        NEUTRAL,
        HOSTILE
    }

    private static int nextBit = 0;

    public enum Action
    {
        ACCESS_HUTS,
        GUARDS_ATTACK,
        PLACE_HUTS,
        BREAK_HUTS,
        CAN_PROMOTE,
        CAN_DEMOTE,
        SEND_MESSAGES;
        //TODO grief control?

        public int flag;

        Action()
        {
            this.flag = 0x1 << nextBit++;//Gives each successive Action a bitflag, ex: GUARDS_ATTACK.flag = 0b0010 || 0x2
        }
    }
}
