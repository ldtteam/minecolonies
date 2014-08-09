package com.minecolonies.tileentities;

import com.minecolonies.lib.IColony;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import java.util.UUID;

public abstract class TileEntityBuildable extends TileEntityChest implements IColony
{
    private int                buildingLevel;
    private TileEntityTownHall townhall;
    private ChunkCoordinates               townhallPos;

    public TileEntityBuildable()
    {
        this.buildingLevel = 0;
    }

    @Override
    public void updateEntity()
    {
        if(worldObj.isRemote) return;

        if(townhall == null && townhallPos != null)
        {
            townhall = (TileEntityTownHall) ChunkCoordUtils.getTileEntity(worldObj, townhallPos);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.buildingLevel = compound.getInteger("buildingLevel");

        if(compound.hasKey("townhall"))
        {
            townhallPos = ChunkCoordUtils.readFromNBT(compound, "townhall");
        }
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
}
