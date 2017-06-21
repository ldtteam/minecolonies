package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityColonyBuilding extends TileEntityChest
{
    /**
     * NBTTag to store the colony id.
     */
    private static final String TAG_COLONY = "colony";
    private static final String TAG_MIRROR = "mirror";
    private static final String TAG_STYLE  = "style";

    /**
     * The colony id.
     */
    private int colonyId = 0;

    /**
     * The colony.
     */
    private Colony colony;

    /**
     * The building the tileEntity belongs to.
     */
    private AbstractBuilding building;

    /**
     * Check if the building has a mirror.
     */
    private boolean mirror;

    /**
     * The style of the building.
     */
    private String style = "";

    /**
     * Empty standard constructor.
     */
    public TileEntityColonyBuilding()
    {
        super();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_COLONY, colonyId);
        return new SPacketUpdateTileEntity(this.getPosition(), 0, compound);
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        colonyId = compound.getInteger(TAG_COLONY);
    }

    @Override
    public void onChunkUnload()
    {
        if (building != null)
        {
            building.setTileEntity(null);
        }
    }

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    public BlockPos getPosition()
    {
        return pos;
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    public Colony getColony()
    {
        if (colony == null)
        {
            updateColonyReferences();
        }
        return colony;
    }

    /**
     * Synchronises colony references from the tile entity.
     */
    private void updateColonyReferences()
    {
        if (colony == null && getWorld() != null)
        {
            if (colonyId == 0)
            {
                colony = ColonyManager.getColony(getWorld(), this.getPos());
            }
            else
            {
                colony = ColonyManager.getColony(colonyId);
            }

            if (colony == null)
            {
                //we tried to update the colony it is still missing... so we...
                if (getWorld().isRemote)
                {
                    /*
                     * It's most probably previewed building, please don't spam it here.
                     */
                }
                else
                {
                    //log on the server
                    Log.getLogger()
                      .warn(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] had colony.",
                        getWorld().getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuilding(getPosition());
            if (building != null && (getWorld() == null || !getWorld().isRemote))
            {
                building.setTileEntity(this);
            }
        }
    }

    @Override
    public void update()
    {
        super.update();

        if (!getWorld().isRemote && colonyId == 0)
        {
            final Colony tempColony = ColonyManager.getColony(getWorld(), this.getPosition());
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }
        }
    }

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    public void setColony(final Colony c)
    {
        colony = c;
        colonyId = c.getID();
        markDirty();
    }

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link AbstractBuilding} associated with the tile entity.
     */
    public AbstractBuilding getBuilding()
    {
        if (building == null)
        {
            updateColonyReferences();
        }
        return building;
    }

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link AbstractBuilding} to associate with the tile entity.
     */
    public void setBuilding(final AbstractBuilding b)
    {
        building = b;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if (building!=null)
        {
            building.markDirty();
        }
    }


    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link AbstractBuilding.View} the tile entity is associated with.
     */
    public AbstractBuilding.View getBuildingView()
    {
        final ColonyView c = ColonyManager.getColonyView(colonyId);
        return c == null ? null : c.getBuilding(getPosition());
    }



    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_COLONY))
        {
            colonyId = compound.getInteger(TAG_COLONY);
        }

        updateColonyReferences();
        mirror = compound.getBoolean(TAG_MIRROR);
        style = compound.getString(TAG_STYLE);
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        /*
        if (colonyId == 0 && colony == null)
        {
            //todo: actually do something about it and not spam the server
        }
        */
        compound.setInteger(TAG_COLONY, colonyId);
        compound.setBoolean(TAG_MIRROR, mirror);
        compound.setString(TAG_STYLE, style);
        return compound;
    }

    @Override
    public boolean isUseableByPlayer(@NotNull final EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.hasAccessPermission(player);
    }

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    public boolean hasAccessPermission(final EntityPlayer player)
    {
        //TODO This is called every tick the GUI is open. Is that bad?
        return building == null || building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    /**
     * Set if the entity is mirrored.
     * @param mirror true if so.
     */
    public void setMirror(final boolean mirror)
    {
        this.mirror = mirror;
    }

    /**
     * Check if building is mirrored.
     * @return true if so.
     */
    public boolean isMirrored()
    {
        return mirror;
    }

    /**
     * Set the style of the tileEntity.
     * @param style the style to set.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Getter for the style.
     * @return the string of it.
     */
    public String getStyle()
    {
        return this.style;
    }
}
