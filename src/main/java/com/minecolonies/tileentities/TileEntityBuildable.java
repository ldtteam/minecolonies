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
    private boolean            hasWorker;
    private TileEntityTownHall townHall;

    public TileEntityBuildable()
    {
        this.buildingLevel = 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagCompound nbtTagCompound = (NBTTagCompound) compound.getTag("nbtTagCompound");
        this.buildingLevel = nbtTagCompound.getInteger("buildingLvl");
        this.hasWorker = nbtTagCompound.getBoolean("hasWorker");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setInteger("buildingLvl", buildingLevel);
        nbtTagCompound.setBoolean("hasWorker", hasWorker);
        compound.setTag("nbtTagCompound", nbtTagCompound);
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

    public boolean hasWorker()
    {
        return hasWorker;
    }

    public void setHasWorker(boolean hasWorker)
    {
        this.hasWorker = hasWorker;
    }

    public TileEntityTownHall getTownHall()
    {
        return townHall;
    }

    public void setTownHall(TileEntityTownHall townHall)
    {
        this.townHall = townHall;
    }

    public void requestBuilding(EntityPlayer player)
    {
        if(!(buildingLevel >= 3)) //TODO
            getTownHall().addHutForUpgrade(Schematic.getName(this, buildingLevel), xCoord, yCoord, zCoord);
    }

    public boolean isPlayerOwner(EntityPlayer player)
    {
        if(this.getTownHall() == null || this.getTownHall().getOwners().isEmpty() || this.getTownHall().getOwners().contains(player.getUniqueID())) return true;
        return false;
    }

    public Vec3 getPosition()
    {
        return Vec3.createVectorHelper(xCoord, yCoord, zCoord);
    }
}
