package com.minecolonies.lib;

import com.minecolonies.tileentities.*;
import net.minecraft.tileentity.TileEntity;

/**
 * EnumHut
 * MineColonies
 * License: GPL v3
 *
 * @author MartijnWoudstra
 */
public enum EnumHut {
    TOWNHALL(0, EnumGUI.TOWNHALL.getID()),
    BAKER(1, EnumGUI.BAKER.getID()),
    BLACKSMITH(2, EnumGUI.BLACKSMITH.getID()),
    BUILDER(3, EnumGUI.BUILDER.getID()),
    CITIZEN(4, EnumGUI.CITIZEN.getID()),
    FARMER(5, EnumGUI.FARMER.getID()),
    LUMBERJACK(6, EnumGUI.LUMBERJACK.getID()),
    MINER(7, EnumGUI.MINER.getID()),
    STONEMASON(8, EnumGUI.STONEMASON.getID()),
    WAREHOUSE(9, EnumGUI.WAREHOUSE.getID());


    private final int id;
    private final int guiID;

    EnumHut(int id, int guiID){
        this.id = id;
        this.guiID = guiID;
    }

    public int getGuiIDByHutID(int ID){
        if(ID == TOWNHALL.getId()) return TOWNHALL.getGuiID();
        else if (ID == BAKER.getId()) return BAKER.getGuiID();
        else if (ID == BLACKSMITH.getId()) return BLACKSMITH.getGuiID();
        else if (ID == BUILDER.getId()) return BUILDER.getGuiID();
        else if (ID == CITIZEN.getId()) return CITIZEN.getGuiID();
        else if (ID == FARMER.getId()) return FARMER.getGuiID();
        else if (ID == LUMBERJACK.getId()) return LUMBERJACK.getGuiID();
        else if (ID == MINER.getId()) return MINER.getGuiID();
        else if (ID == STONEMASON.getId()) return STONEMASON.getGuiID();
        else if (ID == WAREHOUSE.getId()) return WAREHOUSE.getGuiID();
        else return 0;
    }

    public final int getId() {
        return id;
    }

    public int getGuiID() {
        return guiID;
    }

    public static int getGuiIdByInstance(TileEntity tileEntity){
        if(tileEntity instanceof TileEntityTownHall) return TOWNHALL.getGuiID();
        else if (tileEntity instanceof TileEntityHutBaker) return BAKER.getGuiID();
        else if (tileEntity instanceof TileEntityHutBlacksmith) return  BLACKSMITH.getGuiID();
        else if (tileEntity instanceof TileEntityHutBuilder) return  BUILDER.getGuiID();
        else if (tileEntity instanceof TileEntityHutCitizen) return  CITIZEN.getGuiID();
        else if (tileEntity instanceof TileEntityHutFarmer) return  FARMER.getGuiID();
        else if (tileEntity instanceof TileEntityHutLumberjack) return  LUMBERJACK.getGuiID();
        else if (tileEntity instanceof TileEntityHutMiner) return  MINER.getGuiID();
        else if (tileEntity instanceof TileEntityHutWarehouse) return  WAREHOUSE.getGuiID();
        else if (tileEntity instanceof TileEntityHutStonemason) return  STONEMASON.getGuiID();

        return 0;
    }
}
