package com.minecolonies.lib;

/**
 * Created by Colton on 5/18/2014.
 */
public enum EnumGUI
{
    TOWNHALL(0),
    TOWNHALL_RENAME(1),
    BUILDER(2),
    WAREHOUSE(3),
    WAREHOUSE_SETTINGS(4);

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
