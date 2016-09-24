package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.Log;
import com.sun.javafx.binding.Logging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

    /**
     * The colony id.
     */
    private              int    colonyId   = 0;

    /**
     * The colony.
     */
    private Colony           colony;

    /**
     * The building the tileEntity belongs to.
     */
    private AbstractBuilding building;

    /**
     * Empty standard constructor.
     */
    public TileEntityColonyBuilding()
    {
        /**
         * Intentionally left empty.
         */
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound compound = new NBTTagCompound();
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound compound = packet.getNbtCompound();
        colonyId = compound.getInteger(TAG_COLONY);
    }

    @Override
    public void update()
    {
        super.update();

        if (!worldObj.isRemote && colonyId == 0)
        {
            Logging.getLogger().severe(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId",
              worldObj.getWorldInfo().getWorldName(),
              pos.getX(),
              pos.getY(),
              pos.getZ()));
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

    /**
     * Returns the position of the tile entity
     *
     * @return Block Coordinates of the tile entity
     */
    public BlockPos getPosition()
    {
        return pos;
    }

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

        if (building == null && colony != null)
        {
            building = colony.getBuilding(getPosition());
            if (building != null  && (worldObj == null || !worldObj.isRemote))
            {
                building.setTileEntity(this);
            }
        }
    }

    /**
     * Returns the colony ID
     *
     * @return ID of the colony
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity
     *
     * @return Colony of the tile entity
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
     * Sets the colony of the tile entity
     *
     * @param c Colony to set in references
     */
    public void setColony(Colony c)
    {
        colony = c;
        colonyId = c.getID();
        markDirty();
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

    /**
     * Returns the building associated with the tile entity
     *
     * @return {@link AbstractBuilding} associated with the tile entity
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
     * Sets the building associated with the tile entity
     *
     * @param b {@link AbstractBuilding} to associate with the tile entity
     */
    public void setBuilding(AbstractBuilding b)
    {
        building = b;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (colonyId == 0)
        {
            throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId; %s colony reference.",
              worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ(),
              colony == null ? "NO" : "valid"));
        }
        compound.setInteger(TAG_COLONY, colonyId);
        return compound;
    }

    /**
     * Returns the view of the building associated with the tile entity
     *
     * @return {@link AbstractBuilding.View} the tile entity is associated with
     */
    public AbstractBuilding.View getBuildingView()
    {
        ColonyView c = ColonyManager.getColonyView(colonyId);
        return c != null ? c.getBuilding(getPosition()) : null;
    }

    @Override
    public boolean isUseableByPlayer(@NotNull EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.hasAccessPermission(player);
    }

    /**
     * Checks if the player has permission to access the hut
     *
     * @param player Player to check permission of
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    public boolean hasAccessPermission(EntityPlayer player)//TODO This is called every tick the GUI is open. Is that bad?
    {
        return building == null || building.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    //-----------------------------Material Handling--------------------------------

    /**
     * Makes sure ItemStacks inside of the inventory aren't affected by changes to the returned stack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        ItemStack stack = super.getStackInSlot(index);
        if (stack == null)
        {
            return null;
        }
        return stack.copy();
    }

    @Override
    public ItemStack decrStackSize(int index, int quantity)
    {
        ItemStack removed = super.decrStackSize(index, quantity);

        removeStackFromMaterialStore(removed);

        return removed;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack removed = super.removeStackFromSlot(index);

        removeStackFromMaterialStore(removed);

        return removed;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        ItemStack previous = getStackInSlot(index);
        removeStackFromMaterialStore(previous);

        super.setInventorySlotContents(index, stack);

        addStackToMaterialStore(stack);
    }

    private void addStackToMaterialStore(ItemStack stack)
    {
        if (stack == null)
        {
            return;
        }

        if (MaterialSystem.isEnabled)
        {
            building.getMaterialStore().addMaterial(stack.getItem(), stack.stackSize);
        }
    }

    private void removeStackFromMaterialStore(ItemStack stack)
    {
        if (stack == null)
        {
            return;
        }

        if (MaterialSystem.isEnabled)
        {
            building.getMaterialStore().removeMaterial(stack.getItem(), stack.stackSize);
        }
    }
}
