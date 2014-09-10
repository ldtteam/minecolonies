package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.lib.IColony;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import java.lang.ref.WeakReference;
import java.util.UUID;

public abstract class TileEntityBuildable extends TileEntityChest implements IColony
{
    //  OLD CODE
    private int buildingLevel = 0;
    private TileEntityTownHall townhall;
    private ChunkCoordinates   townhallPos;
    //  END OLD CODE

    private UUID                    colonyId;
    private WeakReference<Building> building;

    public TileEntityBuildable(){}

    @Override
    public void updateEntity()
    {
        if (worldObj.isRemote) return;

        //  OLD CODE
        if (townhall == null && townhallPos != null)
        {
            townhall = (TileEntityTownHall) ChunkCoordUtils.getTileEntity(worldObj, townhallPos);
        }
        //  END OLD CODE

//        if (colony == null && colonyId != null)
//        {
//            colony = ColonyManager.getColonyById(colonyId);
//        }
//
//        if (building == null && colony != null)
//        {
//            building = colony.getBuilding(this.getPosition());
//        }

        if (building == null)
        {
            Colony c = ColonyManager.getColonyById(colonyId);
            Building b = (c != null) ? c.getBuilding(getPosition()) : null;
            building = new WeakReference<Building>(b);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        //  OLD CODE
        this.buildingLevel = compound.getInteger("buildingLevel");

        if(compound.hasKey("townhall"))
        {
            townhallPos = ChunkCoordUtils.readFromNBT(compound, "townhall");
        }
        //  END OLD CODE
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("buildingLevel", buildingLevel);
        if(this.townhall != null)
        {
            ChunkCoordUtils.writeToNBT(compound, "townhall", townhall.getPosition());
        }
    }

    public Building getBuilding() { return building.get(); }

    public int getBuildingLevel()
    {
        return buildingLevel;
    }
    public void setBuildingLevel(int buildingLevel)
    {
        this.buildingLevel = buildingLevel;
    }

    public TileEntityTownHall getTownHall()
    {
        return townhall;
    }
    public void setTownHall(TileEntityTownHall townhall)
    {
        this.townhall = townhall;
    }

    public void requestBuilding()
    {
        for(ChunkCoordinates key : getTownHall().getBuilderRequired().keySet())
        {
            if(getPosition().equals(key))
            {
                return;
            }
        }
        if(!(buildingLevel >= 3)) //TODO maxLevel
            getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel + 1), getPosition());
    }

    public void requestRepair()
    {
        for(ChunkCoordinates key : getTownHall().getBuilderRequired().keySet())
        {
            if(getPosition().equals(key))
            {
                return;
            }
        }
        if(buildingLevel == 0) return;
        getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel), getPosition());
    }

    public boolean isPlayerOwner(EntityPlayer player)
    {
        if(building == null) return true;

        Building b = building.get();
        if (b == null) return true;

        return b.getColony().isOwner(player);

        //  OLD CODE
//        if(this.getTownHall() == null || this.getTownHall().getOwners().isEmpty()) return true;
//        for(UUID id : this.getTownHall().getOwners())
//        {
//            if(player.getUniqueID().equals(id))
//            {
//                return true;
//            }
//        }
//        return false;
        //  END OLD CODE
    }

    public ChunkCoordinates getPosition()
    {
        return new ChunkCoordinates(xCoord, yCoord, zCoord);
    }

    public double getDistanceFrom(ChunkCoordinates coords)
    {
        return getDistanceFrom(coords.posX, coords.posY, coords.posZ);
    }

    public double getDistanceFrom(Vec3 pos)
    {
        return getDistanceFrom(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    public void setInventoryName(String name)
    {
        func_145976_a(name);
    }
}
