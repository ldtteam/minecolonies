package com.minecolonies.coremod.tileentities;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.inventory.AbstractInteractiveItemStackHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityColonyBuilding extends TileEntity implements ITickable
{
    /**
     * NBT tag definitions.
     */
    private static final String TAG_COLONY      = "colony";
    private static final String TAG_MIRROR      = "mirror";
    private static final String TAG_CUSTOM_NAME = "CustomName";
    private static final String TAG_INVENTORY   = "inventory";

    /**
     * The item handler.
     */
    private final ItemStackHandler itemHandler = new AbstractInteractiveItemStackHandler(27) {
        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return TileEntityColonyBuilding.this.isUseableByPlayer(player);
        }

        @Override
        public String getName() {
            return TileEntityColonyBuilding.this.getName();
        }
    };

    /**
     * The colony id.
     */
    private int colonyId = 0;

    /**
     * The colony.
     */
    private Colony colony;

    /**
     * The custom name.
     */
    private String customName;

    /**
     * The building the tileEntity belongs to.
     */
    private AbstractBuilding building;

    /**
     * Check if the building has a mirror.
     */
    private boolean mirror;

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
        if (customName != null)
        {
            compound.setString(TAG_CUSTOM_NAME, customName);
        }
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
        if (colony == null && worldObj != null)
        {
            if (colonyId == 0)
            {
                colony = ColonyManager.getColony(worldObj, this.getPos());
            }
            else
            {
                colony = ColonyManager.getColony(colonyId);
            }

            if (colony == null)
            {
                //we tried to update the colony it is still missing... so we...
                if (worldObj.isRemote)
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
                        worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuilding(getPosition());
            if (building != null && (worldObj == null || !worldObj.isRemote))
            {
                building.setTileEntity(this);
            }
        }
    }

    @Override
    public void update()
    {
        if (!worldObj.isRemote && colonyId == 0)
        {
            final Colony tempColony = ColonyManager.getColony(worldObj, this.getPosition());
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

        if (compound.hasKey(TAG_INVENTORY, 10))
        {
            itemHandler.deserializeNBT(compound.getCompoundTag(TAG_INVENTORY));
        }
        else
        {
            // Compatibility code
            itemHandler.deserializeNBT(compound);
        }

        if (compound.hasKey(TAG_CUSTOM_NAME, 8))
        {
            customName = compound.getString(TAG_CUSTOM_NAME);
        }
        else
        {
            customName = null;
        }
        updateColonyReferences();
        mirror = compound.getBoolean(TAG_MIRROR);
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
        compound.setTag(TAG_INVENTORY, itemHandler.serializeNBT());
        if (customName != null)
        {
            compound.setString(TAG_CUSTOM_NAME, customName);
        }
        compound.setInteger(TAG_COLONY, colonyId);
        compound.setBoolean(TAG_MIRROR, mirror);
        return compound;
    }

    public boolean isUseableByPlayer(@NotNull final EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.pos) != this
                ? false
                : (player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D
                    && this.hasAccessPermission(player));
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.chest";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomName(String name)
    {
        this.customName = name;
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, net.minecraft.util.EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        return super.getCapability(capability, facing);
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
        return building == null || building.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
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
}
