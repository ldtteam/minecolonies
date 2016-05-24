package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.Field;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

public class TileEntityField extends TileEntityBanner
{
    private              int        colonyId    = 0;
    private              Colony     colony;
    private              Field      field;

    private final static String     TAG_COLONY  = "colony";

    public TileEntityField(){}

    /**
     * Synchronises colony references from the tile entity
     */
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
                        worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
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

        if (field == null && colony != null)
        {
            field = colony.getField(getPosition());
            if (field != null)
            {
                field.setTileEntity(this);
            }
        }
    }

    @Override
    public void onChunkUnload()
    {
        if (field != null)
        {
            field.setTileEntity(null);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (!compound.hasKey(TAG_COLONY))
        {
            throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] missing COLONY tag.",
                    worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
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
                    worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ(),
                    colony == null ? "NO" : "valid"));
        }
        compound.setInteger(TAG_COLONY, colonyId);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound compound = packet.getNbtCompound();
        colonyId = compound.getInteger(TAG_COLONY);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_COLONY, colonyId);
        return new S35PacketUpdateTileEntity(this.getPosition(), 0, compound);
    }

    /**
     * Returns the colony ID
     *
     * @return      ID of the colony
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity
     *
     * @return    Colony of the tile entity
     */
    public Colony getColony()
    {
        if (colony == null) updateColonyReferences();
        return colony;
    }

    /**
     * Sets the colony of the tile entity
     *
     * @param c     Colony to set in references
     */
    public void setColony(Colony c)
    {
        colony = c;
        colonyId = c.getID();
        markDirty();
    }

    /**
     * Returns the building associated with the tile entity
     *
     * @return      {@link Building} associated with the tile entity
     */
    public Field getField()
    {
        if (field == null) updateColonyReferences();
        return field;
    }

    /**
     *  Sets the building associated with the tile entity
     *
     * @param f     {@link Field} to associate with the tile entity
     */
    public void setField(Field f)
    {
        field = f;
    }

    /**
     * Returns the view of the building associated with the tile entity
     *
     * @return      {@link Building.View} the tile entity is associated with
     */
    public Building.View getBuildingView()
    {
        ColonyView c = ColonyManager.getColonyView(colonyId);
        return c!= null ? c.getBuilding(getPosition()) : null;
    }

    /**
     * Returns the position of the tile entity
     *
     * @return      Block Coordinates of the tile entity
     */
    public BlockPos getPosition()
    {
        return pos;
    }
}
