package com.minecolonies.tileentities;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.IColony;
import com.minecolonies.network.packets.TileEntityPacket;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Vec3;

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

    public void sendPacket()
    {
        NBTTagCompound data = new NBTTagCompound();
        this.writeToNBT(data);
        TileEntityPacket packet = new TileEntityPacket(xCoord, yCoord, zCoord, data);

        MineColonies.packetPipeline.sendToServer(packet);
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

    public void requestBuilding()//TODO check that the list doesn't already contain this request
    {
        if(!(buildingLevel >= 3)) //TODO
            getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel + 1), xCoord, yCoord, zCoord);
    }

    public void requestRepair()//TODO check that the list doesn't already contain this request
    {
        if(buildingLevel == 0) return;
        getTownHall().addHutForUpgrade(Schematic.getNameFromHut(this, buildingLevel), xCoord, yCoord, zCoord);
    }

    public boolean isPlayerOwner(EntityPlayer player)
    {
        return this.getTownHall() == null || this.getTownHall().getOwners().isEmpty() || this.getTownHall().getOwners().contains(player.getUniqueID());
    }

    public Vec3 getPosition()
    {
        return Vec3.createVectorHelper(xCoord, yCoord, zCoord);
    }
}
