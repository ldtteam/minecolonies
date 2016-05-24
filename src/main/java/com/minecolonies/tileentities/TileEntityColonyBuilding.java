package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

public class TileEntityColonyBuilding extends TileEntityChest
{
    private              int        colonyId    = 0;
    private              Colony     colony;
    private              Building   building;

    private final static String     TAG_COLONY  = "colony";

    public TileEntityColonyBuilding(){}

    @Override
    public void update()
    {
        super.update();

        if (!worldObj.isRemote)
        {
            if (colonyId == 0)
            {
                throw new IllegalStateException(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] has no colonyId", worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
            }
        }
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
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.hasAccessPermission(player);
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
    public Building getBuilding()
    {
        if (building == null) updateColonyReferences();
        return building;
    }

    /**
     *  Sets the building associated with the tile entity
     *
     * @param b     {@link Building} to associate with the tile entity
     */
    public void setBuilding(Building b)
    {
        building = b;
    }

    /**
     * Returns the view of the building associated with the tile entity
     *
     * @return      {@link com.minecolonies.colony.buildings.Building.View} the tile entity is associated with
     */
    public Building.View getBuildingView()
    {
        ColonyView c = ColonyManager.getColonyView(colonyId);
        return c!= null ? c.getBuilding(getPosition()) : null;
    }

    /**
     * Checks if the player has permission to access the hut
     *
     * @param player    Player to check permission of
     * @return          True when player has access, or building doesn't exist, otherwise false.
     */
    public boolean hasAccessPermission(EntityPlayer player)//TODO This is called every tick the GUI is open. Is that bad?
    {
        return building == null || building.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);

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

    //-----------------------------Material Handling--------------------------------

    /**
     * Makes sure ItemStacks inside of the inventory aren't affected by changes to the returned stack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        ItemStack stack = super.getStackInSlot(index);
        if(stack == null)
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
        if(stack == null){
            return;
        }

        if(MaterialSystem.isEnabled)
        {
            building.getMaterialStore().addMaterial(stack.getItem(), stack.stackSize);
        }
    }

    private void removeStackFromMaterialStore(ItemStack stack)
    {
        if(stack == null){
            return;
        }

        if(MaterialSystem.isEnabled)
        {
            building.getMaterialStore().removeMaterial(stack.getItem(), stack.stackSize);
        }
    }

    public void initMaterialStoreFromInventory()
    {
        for(int slot = 0; slot < this.getSizeInventory(); slot++)
        {
            this.addStackToMaterialStore(this.getStackInSlot(slot));
        }
    }
}
