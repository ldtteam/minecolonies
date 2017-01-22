package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IInventory
{
    /**
     * Max size of the stacks.
     */
    private static final int    MAX_STACK_SIZE  = 64;
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int    NO_SLOT         = -1;
    /**
     * Size of the hotbar.
     */
    private static final int    HOTBAR_SIZE     = 0;

    /**
     * The inventories custom name. In our case the citizens name.
     */
    private String        customName;
    /**
     * Updated after the inventory has been changed.
     */
    private boolean inventoryChanged = false;
    /**
     * The citizen which owns the inventory.
     */
    private EntityCitizen citizen;

    /**
     * The main inventory.
     */
    private final NonNullList<ItemStack> mainInventory    = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
    /**
     * The armour inventory.
     */
    private final NonNullList<ItemStack> armorInventory   = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
    /**
     * The off-hand inventory.
     */
    private final NonNullList<ItemStack> offHandInventory = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories;
    /** The index of the currently held item (0-8). */
    public        int                          currentItem;
    private       ItemStack                    itemStack;


    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final EntityCitizen citizen)
    {
        this.citizen = citizen;
        if (localeEnabled)
        {
            customName = title;
        }
        this.allInventories = new ArrayList<NonNullList<ItemStack>>();
        this.allInventories.add(this.mainInventory);
        this.allInventories.add(this.armorInventory);
        this.allInventories.add(this.offHandInventory);

        this.itemStack = ItemStack.EMPTY;
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
        this.allInventories = new ArrayList<NonNullList<ItemStack>>();
        this.allInventories.add(this.mainInventory);
        this.allInventories.add(this.armorInventory);
        this.allInventories.add(this.offHandInventory);
        this.itemStack = ItemStack.EMPTY;
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
     * Checks if a certain slot is empty.
     *
     * @param index the slot.
     * @return true if empty.
     */
    public boolean isSlotEmpty(final int index)
    {
        return getStackInSlot(index) == null || getStackInSlot(index) == ItemStack.EMPTY;
    }

    /**
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
     * Contains the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     *
     * @return the stack size.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return MAX_STACK_SIZE;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
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
        return this.citizen.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
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
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
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
     * Returns the item stack currently held by the player.
     */
    public ItemStack getCurrentItem()
    {
        return this.mainInventory.get(this.currentItem);
    }

    private boolean canMergeStacks(ItemStack stack1, ItemStack stack2)
    {
        return !stack1.isEmpty() && InventoryCitizen.stackEqualExact(stack1, stack2) && stack1.isStackable()
                && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < this.getInventoryStackLimit();
    }

    /**
     * Checks item, NBT, and meta if the item is not damageable
     */
    private static boolean stackEqualExact(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem()
                && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /**
     * Returns the first item stack that is empty.
     */
    public int getFirstEmptyStack()
    {
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if ((this.mainInventory.get(i)).isEmpty())
            {
                return i;
            }
        }

        return NO_SLOT;
    }

    /**
     * Pick a item in a certain inventory slot.
     * Probably won't need this.
     * @param index the slot.
     */
    public void pickItem(int index)
    {
        ItemStack itemstack = this.mainInventory.get(this.currentItem);
        this.mainInventory.set(this.currentItem, this.mainInventory.get(index));
        this.mainInventory.set(index, itemstack);
    }

    /**
     * Finds the stack or an equivalent one in the main inventory
     * @param stack the stack to get the slot for.
     * @return the slot it is in.
     */
    @SideOnly(Side.CLIENT)
    public int getSlotFor(ItemStack stack)
    {
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!(this.mainInventory.get(i)).isEmpty() && InventoryCitizen.stackEqualExact(stack, this.mainInventory.get(i)))
            {
                return i;
            }
        }

        return NO_SLOT;
    }


    /**
     * Removes matching items from the inventory.
     * @param itemIn The item to match, null ignores.
     * @param metadataIn The metadata to match, -1 ignores.
     * @param removeCount The number of items to remove. If less than 1, removes all matching items.
     * @param itemNBT The NBT data to match, null ignores.
     * @return The number of items removed from the inventory.
     */
    public int clearMatchingItems(@javax.annotation.Nullable Item itemIn, int metadataIn, int removeCount, @javax.annotation.Nullable NBTTagCompound itemNBT)
    {
        int i = 0;

        for (int j = 0; j < this.getSizeInventory(); ++j)
        {
            ItemStack itemstack = this.getStackInSlot(j);

            if (!itemstack.isEmpty() && (itemIn == null || itemstack.getItem() == itemIn)
                    && (metadataIn <= NO_SLOT || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil
                    .areNBTEquals(itemNBT, itemstack.getTagCompound(), true)))
            {
                int k = removeCount <= 0 ? itemstack.getCount() : Math.min(removeCount - i, itemstack.getCount());
                i += k;

                if (removeCount != 0)
                {
                    itemstack.shrink(k);

                    if (itemstack.isEmpty())
                    {
                        this.setInventorySlotContents(j, ItemStack.EMPTY);
                    }

                    if (removeCount > 0 && i >= removeCount)
                    {
                        return i;
                    }
                }
            }
        }

        if (!this.itemStack.isEmpty())
        {
            if (itemIn != null && this.itemStack.getItem() != itemIn)
            {
                return i;
            }

            if (metadataIn > NO_SLOT && this.itemStack.getMetadata() != metadataIn)
            {
                return i;
            }

            if (itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, this.itemStack.getTagCompound(), true))
            {
                return i;
            }

            int l = removeCount <= 0 ? this.itemStack.getCount() : Math.min(removeCount - i, this.itemStack.getCount());
            i += l;

            if (removeCount != 0)
            {
                this.itemStack.shrink(l);

                if (this.itemStack.isEmpty())
                {
                    this.itemStack = ItemStack.EMPTY;
                }

                if (removeCount > 0 && i >= removeCount)
                {
                    return i;
                }
            }
        }

        return i;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     * @param itemStackIn the itemStack to store.
     * @return the quantity of left over items.
     */
    private int storePartialItemStack(ItemStack itemStackIn)
    {
        int i = itemStackIn.getCount();
        int j = this.storeItemStack(itemStackIn);

        if (j == NO_SLOT)
        {
            j = this.getFirstEmptyStack();
        }

        if (j == NO_SLOT)
        {
            return i;
        }
        else
        {
            ItemStack itemstack = this.getStackInSlot(j);

            if (itemstack.isEmpty())
            {
                // Forge: Replace Item clone above to preserve item capabilities when picking the item up.
                itemstack = itemStackIn.copy();
                itemstack.setCount(0);

                if (itemStackIn.hasTagCompound())
                {
                    itemstack.setTagCompound(itemStackIn.getTagCompound().copy());
                }

                this.setInventorySlotContents(j, itemstack);
            }

            int k = i;

            if (i > itemstack.getMaxStackSize() - itemstack.getCount())
            {
                k = itemstack.getMaxStackSize() - itemstack.getCount();
            }

            if (k > this.getInventoryStackLimit() - itemstack.getCount())
            {
                k = this.getInventoryStackLimit() - itemstack.getCount();
            }

            if (k == 0)
            {
                return i;
            }
            else
            {
                i = i - k;
                itemstack.grow(k);
                itemstack.setAnimationsToGo(5);
                return i;
            }
        }
    }

    /**
     * stores an itemstack in the users inventory
     */
    private int storeItemStack(ItemStack itemStackIn)
    {
        if (this.canMergeStacks(this.getStackInSlot(this.currentItem), itemStackIn))
        {
            return this.currentItem;
        }
        else if (this.canMergeStacks(this.getStackInSlot(40), itemStackIn))
        {
            return 40;
        }
        else
        {
            for (int i = 0; i < this.mainInventory.size(); ++i)
            {
                if (this.canMergeStacks(this.mainInventory.get(i), itemStackIn))
                {
                    return i;
                }
            }

            return NO_SLOT;
        }
    }

    /**
     * Decrement the number of animations remaining. Only called on client side. This is used to handle the animation of
     * receiving a block.
     */
    public void decrementAnimations()
    {
        for (NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            for (int i = 0; i < nonnulllist.size(); ++i)
            {
                if (!(nonnulllist.get(i)).isEmpty())
                {
                    (nonnulllist.get(i)).updateAnimation(this.citizen.world, this.citizen, i, this.currentItem == i);
                }
            }
        }
    }

    /**
     * Adds the item stack to the inventory, returns false if it is impossible.
     * @param itemStackIn stack to add.
     * @return true if possible.
     */
    public boolean addItemStackToInventory(final ItemStack itemStackIn)
    {
        if (itemStackIn.isEmpty())
        {
            return false;
        }
        else
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int j = this.getFirstEmptyStack();

                    if (j >= 0)
                    {
                        this.mainInventory.set(j, itemStackIn.copy());
                        (this.mainInventory.get(j)).setAnimationsToGo(5);
                        itemStackIn.setCount(0);
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    int i;

                    while (true)
                    {
                        i = itemStackIn.getCount();
                        itemStackIn.setCount(this.storePartialItemStack(itemStackIn));

                        if (itemStackIn.isEmpty() || itemStackIn.getCount() >= i)
                        {
                            break;
                        }
                    }

                    return itemStackIn.getCount() < i;
                }
            }
            catch (Exception throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
                crashreportcategory.setDetail("Item name", new ICrashReportDetail<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        return itemStackIn.getDisplayName();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     * @param index the index it is in.
     * @param count amount to reduce.
     * @return the new stack.
     */
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        List<ItemStack> list = null;
        int tempIndex = index;
        for (NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            if (tempIndex < nonnulllist.size())
            {
                list = nonnulllist;
                break;
            }

            tempIndex -= nonnulllist.size();
        }

        return list != null && !(list.get(tempIndex)).isEmpty() ? ItemStackHelper.getAndSplit(list, tempIndex, count) : ItemStack.EMPTY;
    }

    /**
     * Delete a certain stack.
     * @param stack stack to delete.
     */
    public void deleteStack(ItemStack stack)
    {
        for (NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            for (int i = 0; i < nonnulllist.size(); ++i)
            {
                if (nonnulllist.get(i) == stack)
                {
                    nonnulllist.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     * @param index the index to remove it from.
     * @return the stack.
     */
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        NonNullList<ItemStack> nonnulllist = null;
        int tempIndex = index;
        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories)
        {
            if (tempIndex < nonnulllist1.size())
            {
                nonnulllist = nonnulllist1;
                break;
            }

            tempIndex -= nonnulllist1.size();
        }

        if (nonnulllist != null && !(nonnulllist.get(tempIndex)).isEmpty())
        {
            ItemStack itemstack = nonnulllist.get(tempIndex);
            nonnulllist.set(tempIndex, ItemStack.EMPTY);
            return itemstack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     * @param index the index.
     * @param stack the stack.
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        NonNullList<ItemStack> nonnulllist = null;
        int tempIndex = index;
        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories)
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
     * Get the strength against a block.
     * @param state the block.
     * @return the float value.
     */
    public float getStrVsBlock(IBlockState state)
    {
        float f = 1.0F;

        if (!(this.mainInventory.get(this.currentItem)).isEmpty())
        {
            f *= (this.mainInventory.get(this.currentItem)).getStrVsBlock(state);
        }

        return f;
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     * @param nbtTagListIn the taglist in.
     * @return the filled list.
     */
    public NBTTagList writeToNBT(NBTTagList nbtTagListIn)
    {
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!(this.mainInventory.get(i)).isEmpty())
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                (this.mainInventory.get(i)).writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j)
        {
            if (!(this.armorInventory.get(j)).isEmpty())
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)(j + 100));
                (this.armorInventory.get(j)).writeToNBT(nbttagcompound1);
                nbtTagListIn.appendTag(nbttagcompound1);
            }
        }

        for (int k = 0; k < this.offHandInventory.size(); ++k)
        {
            if (!(this.offHandInventory.get(k)).isEmpty())
            {
                NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                nbttagcompound2.setByte("Slot", (byte)(k + 150));
                (this.offHandInventory.get(k)).writeToNBT(nbttagcompound2);
                nbtTagListIn.appendTag(nbttagcompound2);
            }
        }

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     * @param nbtTagListIn the tag list.
     */
    public void readFromNBT(NBTTagList nbtTagListIn)
    {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

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
     * Returns the number of slots in the inventory.
     * @return the size of the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return this.mainInventory.size();
    }

    /**
     * Checks if the inventory is empty.
     * @return true if so.
     */
    @Override
    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.mainInventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.armorInventory)
        {
            if (!itemstack1.isEmpty())
            {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.offHandInventory)
        {
            if (!itemstack2.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     * @param index the index.
     * @return the stack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        List<ItemStack> list = null;
        int tempIndex = index;
        for (NonNullList<ItemStack> nonnulllist : this.allInventories)
        {
            if (tempIndex < nonnulllist.size())
            {
                list = nonnulllist;
                break;
            }

            tempIndex -= nonnulllist.size();
        }

        return list == null ? ItemStack.EMPTY : list.get(tempIndex);
    }

    /**
     * Checks if the entity can harvest a block.
     * @param state the block.
     * @return true if so.
     */
    public boolean canHarvestBlock(IBlockState state)
    {
        if (state.getMaterial().isToolNotRequired())
        {
            return true;
        }
        else
        {
            ItemStack itemstack = this.getStackInSlot(this.currentItem);
            return !itemstack.isEmpty() ? itemstack.canHarvestBlock(state) : false;
        }
    }


    /**
     * returns a player armor item (as itemstack) contained in specified armor slot.
     * @param slotIn the slot.
     * @return the itemStack.
     */
    public ItemStack armorItemInSlot(int slotIn)
    {
        return this.armorInventory.get(slotIn);
    }

    /**
     * Drop all armor and main inventory items.
     */
    public void dropAllItems()
    {
        for (List<ItemStack> list : this.allInventories)
        {
            for (int i = 0; i < list.size(); ++i)
            {
                ItemStack itemstack = list.get(i);

                if (!itemstack.isEmpty())
                {
                    this.citizen.dropItem(itemstack.getItem(), itemstack.getCount());
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Set the stack helds by mouse, used in GUI/Container
     */
    public void setItemStack(ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
    }

    /**
     * Stack helds by mouse, used in GUI and Containers
     */
    public ItemStack getItemStack()
    {
        return this.itemStack;
    }

    /**
     * Returns true if the specified ItemStack exists in the inventory.
     * @param itemStackIn the stack to be searched for.
     * @return true if it exists.
     */
    public boolean hasItemStack(ItemStack itemStackIn)
    {
        label19:

        for (List<ItemStack> list : this.allInventories)
        {
            Iterator<ItemStack> iterator = list.iterator();

            while (true)
            {
                if (!iterator.hasNext())
                {
                    continue label19;
                }

                ItemStack itemstack = iterator.next();

                if (!itemstack.isEmpty() && itemstack.isItemEqual(itemStackIn))
                {
                    break;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Copy the ItemStack contents from another InventoryCitizen instance
     *
     * @param inventoryCitizen the citizens inventory to copy.
     */
    public void copyInventory(InventoryPlayer inventoryCitizen)
    {
        for (int i = 0; i < this.getSizeInventory(); ++i)
        {
            this.setInventorySlotContents(i, inventoryCitizen.getStackInSlot(i));
        }

        this.currentItem = inventoryCitizen.currentItem;
    }

    /**
     * Clears the whole inventory.
     *
     */
    @Override
    public void clear()
    {
        for (List<ItemStack> list : this.allInventories)
        {
            list.clear();
        }
    }
}
