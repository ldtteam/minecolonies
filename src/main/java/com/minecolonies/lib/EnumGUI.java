package com.minecolonies.lib;

import com.minecolonies.tileentities.*;
import net.minecraft.tileentity.TileEntity;

/**
 * Simple Enum for GUI ids
 *
 * @author Colton
 */
public enum EnumGUI
{
    TOWNHALL,
    TOWNHALL_RENAME,
    BUILDER,
    WAREHOUSE,
    BAKER,
    BLACKSMITH,
    CITIZEN,
    FARMER,
    LUMBERJACK,
    MINER,
    STONEMASON;

    public int getID()
    {
        return this.ordinal();
    }
}
