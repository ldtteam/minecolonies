package com.minecolonies.entity;

public enum EnumCitizenAction
{
    UNKNOWN(-1),
    UPDATE_INVENTORY(0), // Go home
    IDLE(1),
    GO_TO_WORK(3), // Should only be implemented by workers
    WAKING_UP(4),
    GO_TO_BED(5),
    SLEEP(6),
    DEFENDING(7),
    GET_NEEDING(8);

    private final int actionID;

    EnumCitizenAction(int actionID)
    {
        this.actionID = actionID;
    }

    public int getActionID()
    {
        return actionID;
    }

    public static EnumCitizenAction getActionById(int actionID)
    {
        EnumCitizenAction action = null;
        for(int i = 0; i < values().length; i++)
        {
            if(values()[i].getActionID() == actionID)
            {
                action = values()[i];
            }
        }
        return action;
    }
}