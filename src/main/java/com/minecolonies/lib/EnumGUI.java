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

    public static int getGuiIdByInstance(TileEntity tileEntity)
    {
        if(tileEntity instanceof TileEntityTownHall) return TOWNHALL.getID();
        else if(tileEntity instanceof TileEntityHutBaker) return BAKER.getID();
        else if(tileEntity instanceof TileEntityHutBlacksmith) return BLACKSMITH.getID();
        else if(tileEntity instanceof TileEntityHutBuilder) return BUILDER.getID();
        else if(tileEntity instanceof TileEntityHutCitizen) return CITIZEN.getID();
        else if(tileEntity instanceof TileEntityHutFarmer) return FARMER.getID();
        else if(tileEntity instanceof TileEntityHutLumberjack) return LUMBERJACK.getID();
        else if(tileEntity instanceof TileEntityHutMiner) return MINER.getID();
        else if(tileEntity instanceof TileEntityHutWarehouse) return WAREHOUSE.getID();
        else if(tileEntity instanceof TileEntityHutStonemason) return STONEMASON.getID();

        return 0;
    }
}
