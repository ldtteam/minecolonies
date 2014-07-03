package com.minecolonies.tileentities;

import com.minecolonies.lib.IColony;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.UUID;

public abstract class TileEntityBuildable extends TileEntityChest implements IColony
{
    private int                buildingLevel;
    private TileEntityTownHall townhall;
    private int                townhallX, townhallY, townhallZ;

    public TileEntityBuildable()
    {
        this.buildingLevel = 0;
    }

    @Override
    public void updateEntity()
    {
        if(worldObj.isRemote) return;

        if(townhall == null)
        {
            townhall = (TileEntityTownHall) worldObj.getTileEntity(townhallX, townhallY, townhallZ);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.buildingLevel = compound.getInteger("buildingLevel");
        this.townhallX = compound.getInteger("townhall-x");
        this.townhallY = compound.getInteger("townhall-y");
        this.townhallZ = compound.getInteger("townhall-z");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("buildingLevel", buildingLevel);
        if(this.townhall != null)
        {
            compound.setInteger("townhall-x", townhall.xCoord);
            compound.setInteger("townhall-y", townhall.yCoord);
            compound.setInteger("townhall-z", townhall.zCoord);
        }
    }

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
        for(int[] key : getTownHall().getBuilderRequired().keySet())
        {
            if(Arrays.equals(new int[]{xCoord, yCoord, zCoord}, key))
            {
                return;
            }
        }
        if(!(buildingLevel >= 3)) //TODO maxLevel
            getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel + 1), xCoord, yCoord, zCoord);
    }

    public void requestRepair()
    {
        for(int[] key : getTownHall().getBuilderRequired().keySet())
        {
            if(Arrays.equals(new int[]{xCoord, yCoord, zCoord}, key))
            {
                return;
            }
        }
        if(buildingLevel == 0) return;
        getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel), xCoord, yCoord, zCoord);
    }

    public boolean isPlayerOwner(EntityPlayer player)
    {
        if(this.getTownHall() == null || this.getTownHall().getOwners().isEmpty()) return true;
        for(UUID id : this.getTownHall().getOwners())
        {
            if(player.getUniqueID().equals(id))
            {
                return true;
            }
        }
        return false;
    }

    public Vec3 getPosition()
    {
        return Vec3.createVectorHelper(xCoord, yCoord, zCoord);
    }
}
