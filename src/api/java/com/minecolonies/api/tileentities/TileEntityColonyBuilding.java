package com.minecolonies.api.tileentities;

import com.minecolonies.api.IAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
    private IToken colonyId = StandardFactoryController.getInstance().getNewInstance(UUID.randomUUID());

    /**
     * The colony.
     */
    private IColony colony;

    /**
     * The building the tileEntity belongs to.
     */
    private IBuilding building;

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

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    public IToken getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    public IColony getColony()
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
        if (colony == null && world != null)
        {
            if (colonyId == null)
            {
                colony = IAPI.Holder.getApi().getColonyManager().getColony(world, this.getPos());
            }
            else
            {
                colony = IAPI.Holder.getApi().getColonyManager().getColony(colonyId);
            }

            if (colony == null)
            {
                //we tried to update the colony it is still missing... so we...
                if (world.isRemote)
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
                        world.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuilding(getPosition());
            if (building != null && (world == null || !world.isRemote))
            {
                building.setTileEntity(this);
            }
        }
    }

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    public void setColony(final IColony c)
    {
        colony = c;
        colonyId = c.getID();
        markDirty();
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if (building != null)
        {
            building.markDirty();
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(TAG_COLONY, StandardFactoryController.getInstance().serialize(colonyId));
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

        colonyId = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_COLONY));
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
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuilding} associated with the tile entity.
     */
    public IBuilding getBuilding()
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
     * @param b {@link IBuilding} to associate with the tile entity.
     */
    public void setBuilding(final IBuilding b)
    {
        building = b;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_COLONY))
        {
            NBTBase base = compound.getTag(TAG_COLONY);
            if (base instanceof NBTTagCompound)
            {
                colonyId = StandardFactoryController.getInstance().deserialize((NBTTagCompound) base);
            }
            else
            {
                colonyId = null;
            }
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
        compound.setTag(TAG_COLONY, StandardFactoryController.getInstance().serialize(colonyId));
        compound.setBoolean(TAG_MIRROR, mirror);
        compound.setString(TAG_STYLE, style);
        return compound;
    }

    @Override
    public void update()
    {
        super.update();

        if (!world.isRemote && colonyId == null)
        {
            final IColony tempColony = IAPI.Holder.getApi().getColonyManager().getColony(world, this.getPosition());
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(final EntityPlayer player)
    {
        return super.isUsableByPlayer(player) && this.hasAccessPermission(player);
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
     *
     * @param mirror true if so.
     */
    public void setMirror(final boolean mirror)
    {
        this.mirror = mirror;
    }

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    public boolean isMirrored()
    {
        return mirror;
    }

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    public String getStyle()
    {
        return this.style;
    }

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }
}
