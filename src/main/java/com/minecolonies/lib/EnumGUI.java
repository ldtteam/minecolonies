package com.minecolonies.lib;

/**
 * Simple Enum for GUI ids
 *
 * @author Colton
 */
public enum EnumGUI
{
    TOWNHALL(0),
    TOWNHALL_RENAME(1),
    BUILDER(2),
    WAREHOUSE(3),
    WAREHOUSE_SETTINGS(4),
    BAKER(5),
    BLACKSMITH(6),
    CITIZEN(7),
    FARMER(8),
    LUMBERJACK(9),
    MINER(10),
    STONEMASON(11);

    private final int id;

    EnumGUI(int id)
    {
        this.id = id;
    }

    public int getID()
    {
        return this.id;
    }
}
