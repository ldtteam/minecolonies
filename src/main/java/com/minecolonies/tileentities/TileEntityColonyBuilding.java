package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class TileEntityColonyBuilding extends TileEntityChest
{
    private UUID                    colonyId;
    private WeakReference<Building> building;

    public TileEntityColonyBuilding(){}

    @Override
    public void updateEntity()
    {
        if (worldObj.isRemote) return;

        if (building == null && colonyId != null)
        {
            Colony c = ColonyManager.getColonyById(colonyId);
            Building b = null;

            if (c != null)
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
        colonyId = UUID.fromString(compound.getString("colony"));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("colony", colonyId.toString());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.isPlayerOwner(player);
    }

    //  For now, we don't need to sync the ColonyId
//    @Override
//    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
//    {
//        this.readFromNBT(packet.func_148857_g());
//    }
//
//    @Override
//    public S35PacketUpdateTileEntity getDescriptionPacket()
//    {
//        NBTTagCompound nbtTagCompound = new NBTTagCompound();
//        this.writeToNBT(nbtTagCompound);
//        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbtTagCompound);
//    }

    public UUID getColonyId() { return colonyId; }
    public Building getBuilding() { return building != null ? building.get() : null; }
    public Building.View getBuildingView()
    {
        ColonyView colony = ColonyManager.getColonyViewById(colonyId);
        return colony.getBuilding(getPosition());
    }

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
