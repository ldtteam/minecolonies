package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class TileEntityColonyBuilding extends TileEntityChest
{
    private UUID                    colonyId;
    private WeakReference<Colony>   colony;
    private WeakReference<Building> building;

    private final String TAG_COLONY = "colony";

    public TileEntityColonyBuilding(){}

    @Override
    public void updateEntity()
    {
        if (worldObj.isRemote) return;

        if (colony == null && colonyId != null)
        {
            colony = new WeakReference<Colony>(ColonyManager.getColonyById(colonyId));
        }

        if (building == null && colony != null)
        {
            Colony c = colony.get();
            Building b = null;

            if (colony != null)
            {
                b = c.getBuilding(getPosition());
            }

            building = new WeakReference<Building>(b);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        colonyId = UUID.fromString(compound.getString(TAG_COLONY));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_COLONY, colonyId.toString());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.isPlayerOwner(player);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound compound = packet.func_148857_g();
        colonyId = UUID.fromString(compound.getString(TAG_COLONY));
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(TAG_COLONY, colonyId.toString());
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, compound);
    }

    public UUID getColonyId() { return colonyId; }
    public Colony getColony() { return colony != null ? colony.get() : null; }
    public void setColony(Colony colony)
    {
        this.colony = new WeakReference<Colony>(colony);
        colonyId = colony.getID();
    }

    public Building getBuilding() { return building != null ? building.get() : null; }
    public void setBuilding(Building b)
    {
        building = new WeakReference<Building>(b);
    }
//    public Building.View getBuildingView()
//    {
//        ColonyView colony = ColonyManager.getColonyViewById(colonyId);
//        return colony.getBuilding(getPosition());
//    }

    public boolean isPlayerOwner(EntityPlayer player)
    {
        if(building == null) return true;

        Building b = building.get();
        if (b == null) return true;

        return b.getColony().isOwner(player);
    }

    public ChunkCoordinates getPosition()
    {
        return new ChunkCoordinates(xCoord, yCoord, zCoord);
    }
}
