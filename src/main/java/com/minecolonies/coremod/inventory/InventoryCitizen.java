package com.minecolonies.coremod.inventory;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IInventory
{
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int                    NO_SLOT          = -1;
    /**
     * Size of the hotbar.
     */
    private static final int                    HOTBAR_SIZE      = 0;
    /**
     * The main inventory.
     */
    private final        NonNullList<ItemStack> mainInventory    = NonNullList.<ItemStack>withSize(36, ItemStackUtils.EMPTY);
    /**
     * The armour inventory.
     */
    private final        NonNullList<ItemStack> armorInventory   = NonNullList.<ItemStack>withSize(4, ItemStackUtils.EMPTY);
    /**
     * The off-hand inventory.
     */
    private final        NonNullList<ItemStack> offHandInventory = NonNullList.<ItemStack>withSize(1, ItemStackUtils.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories;
    /**
     * The index of the currently held item (0-8).
     */
    public        int                          currentItem;

    private ItemStack itemStack = ItemStackUtils.EMPTY;

    /**
     * The inventories custom name. In our case the citizens name.
     */
    private String customName;
    /**
     * Updated after the inventory has been changed.
     */
    private boolean inventoryChanged = false;
    /**
     * The citizen which owns the inventory.
     */
    private CitizenData citizen;

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final CitizenData citizen)
    {
        this.citizen = citizen;
        if (localeEnabled)
        {
            customName = title;
        }
        this.allInventories = new ArrayList<>();
        this.allInventories.add(this.mainInventory);
        this.allInventories.add(this.armorInventory);
        this.allInventories.add(this.offHandInventory);

        this.itemStack = ItemStackUtils.EMPTY;
    }

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled)
    {
        if (localeEnabled)
        {
            customName = title;
        }
        this.allInventories = new ArrayList<>();
        this.allInventories.add(this.mainInventory);
        this.allInventories.add(this.armorInventory);
        this.allInventories.add(this.offHandInventory);
        this.itemStack = ItemStackUtils.EMPTY;
    }

    /**
     * Get the size of the citizens hotbar inventory.
     *
     * @return the size.
     */
    public int getHotbarSize()
    {
        return HOTBAR_SIZE;
    }

    /**
     * Sets the name of the inventory.
     *
     * @param customName the string to use to set the name.
     */
    public void setCustomName(final String customName)
    {
        this.customName = customName;
    }

    /**
     * Checks if the inventory has been changed and then resets the boolean.
     *
     * @return true if it changed.
     */
    public boolean hasInventoryChanged()
    {
        if (inventoryChanged)
        {
            inventoryChanged = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the item that is currently being held by citizen.
     *
     * @return {@link ItemStack} currently being held by citizen.
     */
    public ItemStack getHeldItemMainhand()
    {
        return getStackInSlot(currentItem);
    }

    /**
     * Set item to be held by citizen.
     *
     * @param slot Slot index with item to be held by citizen.
     */
    public void setHeldItem(final int slot)
    {
        this.currentItem = slot;
    }

    /**
     * Gets slot that hold item that is being held by citizen.
     * {@link #getHeldItemMainhand()}.
     *
     * @return Slot index of held item
     */
    public int getHeldItemSlot()
    {
        return currentItem;
    }

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the size of the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return this.mainInventory.size();
    }    /**
     * Get the name of this object. For citizens this returns their name.
     *
     * @return the name of the inventory.
     */
    @NotNull
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "citizen.inventory";
    }

    /**
     * Checks if the inventory is empty.
     *
     * @return true if so.
     */
    @Override
    public boolean isEmpty()
    {
        for (final ItemStack itemstack : this.mainInventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        for (final ItemStack itemstack1 : this.armorInventory)
        {
            if (!itemstack1.isEmpty())
            {
                return false;
            }
        }

        for (final ItemStack itemstack2 : this.offHandInventory)
        {
            if (!itemstack2.isEmpty())
            {
                return false;
            }
        }

        return true;
    }    /**
     * Checks if the inventory is named.
     *
     * @return true if the inventory has a custom name.
     */
    @Override
    public boolean hasCustomName()
    {
        return this.customName != null;
    }

    /**
     * Returns the stack in the given slot.
     *
     * @param index the index.
     * @return the stack.
     */
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        List<ItemStack> list = null;
        int tempIndex = index;
        for (final NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            if (tempIndex < nonnulllist.size())
            {
                list = nonnulllist;
                break;
            }

            tempIndex -= nonnulllist.size();
        }

        return list == null ? ItemStackUtils.EMPTY : list.get(tempIndex);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     *
     * @param index the index it is in.
     * @param count amount to reduce.
     * @return the new stack.
     */
    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        List<ItemStack> list = null;
        int tempIndex = index;
        for (final NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            if (tempIndex < nonnulllist.size())
            {
                list = nonnulllist;
                break;
            }

            tempIndex -= nonnulllist.size();
        }

        return list != null && !ItemStackUtils.isEmpty(list.get(tempIndex)) ? ItemStackHelper.getAndSplit(list, tempIndex, count) : ItemStackUtils.EMPTY;
    }

    /**
     * Removes a stack from the given slot and returns it.
     *
     * @param index the index to remove it from.
     * @return the stack.
     */
    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        NonNullList<ItemStack> nonnulllist = null;
        int tempIndex = index;
        for (final NonNullList<ItemStack> nonnulllist1 : this.allInventories)
        {
            if (tempIndex < nonnulllist1.size())
            {
                nonnulllist = nonnulllist1;
                break;
            }

            tempIndex -= nonnulllist1.size();
        }

        if (nonnulllist != null && !ItemStackUtils.isEmpty(nonnulllist.get(tempIndex)))
        {
            final ItemStack itemstack = nonnulllist.get(tempIndex);
            nonnulllist.set(tempIndex, ItemStackUtils.EMPTY);
            return itemstack;
        }
        else
        {
            return ItemStackUtils.EMPTY;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param index the index.
     * @param stack the stack.
     */
    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        NonNullList<ItemStack> nonnulllist = null;
        int tempIndex = index;
        for (final NonNullList<ItemStack> nonnulllist1 : this.allInventories)
        {
            if (tempIndex < nonnulllist1.size())
            {
                nonnulllist = nonnulllist1;
                break;
            }

            tempIndex -= nonnulllist1.size();
        }

        if (nonnulllist != null)
        {
            nonnulllist.set(tempIndex, stack);
        }
    }

    /**
     * Contains the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     *
     * @return the stack size.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return STACKSIZE;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
        if (this.citizen != null)
        {
            this.citizen.markDirty();
        }
    }

    /**
     * Do not give this method the name canInteractWith because it clashes with Container.
     *
     * @param player the player acessing the inventory.
     * @return if the player is allowed to access.
     */
    @Override
    public boolean isUsableByPlayer(@NotNull final EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            return true;
        }

        if (this.citizen == null)
        {
            return false;
        }

        return this.citizen.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    /**
     * Called when inventory is opened by a player.
     *
     * @param player the player who opened the inventory.
     */
    @Override
    public void openInventory(final EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Called after the inventory has been closed by a player.
     *
     * @param player the player who opened the inventory.
     */
    @Override
    public void closeInventory(final EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }    /**
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     *
     * @param index the accessing slot.
     * @param stack the stack trying to enter.
     * @return if the stack may be inserted.
     */
    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return true;
    }

    /**
     * This may be used in order to return values of different GUI areas like the ones in the beacon.
     *
     * @param id the id of the field.
     * @return the value of the field.
     */
    @Override
    public int getField(final int id)
    {
        return 0;
    }

    /**
     * This may be used to set GUI areas with a certain id and value.
     *
     * @param id    some id.
     * @param value some value.
     */
    @Override
    public void setField(final int id, final int value)
    {
        /*
         * We currently need no fields.
         */
    }

    /**
     * Returns the number of fields.
     *
     * @return the amount.
     */
    @Override
    public int getFieldCount()
    {
        return 0;
    }

    /**
     * Clears the whole inventory.
     */
    @Override
    public void clear()
    {
        for (final List<ItemStack> list : this.allInventories)
        {
            list.clear();
        }
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     *
     * @param nbtTagListIn the taglist in.
     * @return the filled list.
     */
    public NBTTagList writeToNBT(final NBTTagList nbtTagListIn)
    {
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!(this.mainInventory.get(i)).isEmpty())
            {
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                (this.mainInventory.get(i)).writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j)
        {
            if (!(this.armorInventory.get(j)).isEmpty())
            {
                final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) (j + 100));
                (this.armorInventory.get(j)).writeToNBT(nbttagcompound1);
                nbtTagListIn.appendTag(nbttagcompound1);
            }
        }

        for (int k = 0; k < this.offHandInventory.size(); ++k)
        {
            if (!(this.offHandInventory.get(k)).isEmpty())
            {
                final NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                nbttagcompound2.setByte("Slot", (byte) (k + 150));
                (this.offHandInventory.get(k)).writeToNBT(nbttagcompound2);
                nbtTagListIn.appendTag(nbttagcompound2);
            }
        }

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     *
     * @param nbtTagListIn the tag list.
     */
    public void readFromNBT(final NBTTagList nbtTagListIn)
    {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i)
        {
            final NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            final int j = nbttagcompound.getByte("Slot") & 255;
            final ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty())
            {
                if (j >= 0 && j < this.mainInventory.size())
                {
                    this.mainInventory.set(j, itemstack);
                }
                else if (j >= 100 && j < this.armorInventory.size() + 100)
                {
                    this.armorInventory.set(j - 100, itemstack);
                }
                else if (j >= 150 && j < this.offHandInventory.size() + 150)
                {
                    this.offHandInventory.set(j - 150, itemstack);
                }
            }
        }
    }

    /**
     * Stack helds by mouse, used in GUI and Containers
     *
     * @return the hold stack.
     */
    public ItemStack getItemStack()
    {
        return this.itemStack;
    }

    /**
     * Set the stack helds by mouse, used in GUI/Container
     *
     * @param itemStackIn the stack to set.
     */
    public void setItemStack(final ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
    }






}
