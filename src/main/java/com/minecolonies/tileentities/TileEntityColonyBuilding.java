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

public class TileEntityColonyBuilding extends TileEntityChest
{
    private int      colonyId = 0;
    private Colony   colony;
    private Building building;

    private final static String TAG_COLONY = "colony";

    public TileEntityColonyBuilding(){}

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (!worldObj.isRemote)
        {
            if (colonyId == 0)
            {
                throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId", worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord));
            }
        }
    }

    private void updateColonyReferences()
    {
        if (colony == null)
        {
            if (colonyId != 0)
            {
                colony = ColonyManager.getColony(colonyId);
            }
            else
            {
                throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId",
                        worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord));
            }
//            else if (worldObj != null)
//            {
//                throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId",
//                        worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord));
//
//                colony = ColonyManager.getColony(worldObj, xCoord, yCoord, zCoord);
//
//                if (colony != null)
//                {
//                    colonyId = colony.getID();
//                }
//            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuilding(getPosition());
            if (building != null)
            {
                building.setTileEntity(this);
            }
        }
    }

    @Override
    public void onChunkUnload()
    {
        if (building != null)
        {
            building.setTileEntity(null);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (!compound.hasKey(TAG_COLONY))
        {
            throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] missing COLONY tag.",
                    worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord));
        }
        colonyId = compound.getInteger(TAG_COLONY);
        updateColonyReferences();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (colonyId == 0)
        {
            throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId; %s colony reference.",
                    worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord,
                    colony == null ? "NO" : "valid"));
        }
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
    public Colony getColony()
    {
        if (colony == null) updateColonyReferences();
        return colony;
    }
    public void setColony(Colony c)
    {
        colony = c;
        colonyId = c.getID();
        markDirty();
    }

    public Building getBuilding()
    {
        if (building == null) updateColonyReferences();
        return building;
    }
    public void setBuilding(Building b)
    {
        building = b;
    }
    public Building.View getBuildingView()
    {
        ColonyView c = ColonyManager.getColonyView(colonyId);
        return c!= null ? c.getBuilding(getPosition()) : null;
    }

    public boolean hasAccessPermission(EntityPlayer player)//This is called every tick the GUI is open. Is that bad?
    {
        if(building == null) return true;

        return building.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    public ChunkCoordinates getPosition()
    {
        return new ChunkCoordinates(xCoord, yCoord, zCoord);
    }
}
