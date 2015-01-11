package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChunkCoordinates;

import java.lang.ref.WeakReference;

public class TileEntityColonyBuilding extends TileEntityChest
{
    private int                     colonyId = 0;
    private WeakReference<Colony>   colony;
    private WeakReference<Building> building;

    private final static String TAG_COLONY = "colony";

    public TileEntityColonyBuilding(){}

    @Override
    public void updateEntity()
    {
        if (worldObj.isRemote) return;

        if (colony == null && colonyId != 0)
        {
            colony = new WeakReference<Colony>(ColonyManager.getColonyById(colonyId));
        }

        if (building == null && colony != null)
        {
            Colony c = colony.get();
            Building b = null;

            if (c != null)
            {
                b = c.getBuilding(getPosition());
            }

            building = new WeakReference<Building>(b);

            if (b != null)
            {
                b.setTileEntity(this);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        colonyId = compound.getInteger(TAG_COLONY);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_COLONY, colonyId);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.hasAccessPermission(player);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound compound = packet.func_148857_g();
        colonyId = compound.getInteger(TAG_COLONY);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_COLONY, colonyId);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, compound);
    }

    public int getColonyId() { return colonyId; }
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
    public Building.View getBuildingView()
    {
        ColonyView colony = ColonyManager.getColonyView(colonyId);
        return colony != null ? colony.getBuilding(getPosition()) : null;
    }

    public boolean hasAccessPermission(EntityPlayer player)//This is called every tick the GUI is open. Is that bad?
    {
        if(building == null) return true;

        Building b = building.get();
        if (b == null) return true;

        return b.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    public ChunkCoordinates getPosition()
    {
        return new ChunkCoordinates(xCoord, yCoord, zCoord);
    }
}
