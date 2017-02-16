package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen extends ItemStackHandler implements IInteractiveItemHandler
{
    /**
     * Number of slots in the inventory.
     */
    private static final int    INVENTORY_SIZE  = 27;
    /**
     * Max size of the stacks.
     */
    private static final int    MAX_STACK_SIZE  = 64;
    /*
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_CUSTOM_NAME = "CustomName";
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
     * The held item.
     */
    private int           heldItem;
    /**
     * Updated after the inventory has been changed.
     */
    private boolean inventoryChanged = false;
    /**
     * The citizen which owns the inventory.
     */
    private EntityCitizen citizen;

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final EntityCitizen citizen)
    {
        super(INVENTORY_SIZE);

        this.citizen = citizen;
        if (localeEnabled)
        {
            customName = title;
        }
    }

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled)
    {
        this(title, localeEnabled, null);
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
        return getStackInSlot(heldItem);
    }

    /**
     * Set item to be held by citizen.
     *
     * @param slot Slot index with item to be held by citizen.
     */
    public void setHeldItem(final int slot)
    {
        this.heldItem = slot;
    }

    /**
     * Removes one item of specified Item from inventory (if it is in a stack, the stack size will reduce with 1).
     *
     * @param itemIn the item to consume.
     * @return true if succeed.
     */
    public boolean consumeInventoryItem(final Item itemIn)
    {
        final int i = this.getInventorySlotContainItem(itemIn);

        if (i < 0)
        {
            return false;
        }
        else
        {
            --this.stacks[i].stackSize;
            if (this.stacks[i].stackSize <= 0)
            {
                this.stacks[i] = null;
            }
            onContentsChanged(i);

            return true;
        }
    }

    private int getInventorySlotContainItem(final Item itemIn)
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null && this.stacks[i].getItem() == itemIn)
            {
                return i;
            }
        }

        return NO_SLOT;
    }

    /**
     * Checks if a specified Item is inside the inventory.
     *
     * @param itemIn the item to check for.
     * @return if itemIn in inventory.
     */
    public boolean hasItem(final Item itemIn)
    {
        return getInventorySlotContainItem(itemIn) != NO_SLOT;
    }

    /**
     * Gets slot that hold item that is being held by citizen.
     * {@link #getHeldItemMainhand()}.
     *
     * @return Slot index of held item
     */
    public int getHeldItemSlot()
    {
        return heldItem;
    }

    /**
     * Checks if a certain slot is empty.
     *
     * @param index the slot.
     * @return true if empty.
     */
    public boolean isSlotEmpty(final int index)
    {
        return getStackInSlot(index) == null;
    }

    /**
     * Get the name of this object. For citizens this returns their name.
     *
     * @return the name of the inventory.
     */
    @NotNull
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "citizen.inventory";
    }

    /**
     * Used to retrieve variables.
     *
     * @param compound with the give tag.
     */
    @Override
    public void deserializeNBT(@NotNull final NBTTagCompound compound)
    {
        // ItemStackHandler helpfully uses the same format of storage we used to,
        // so additional compatibility code is unnecessary.
        super.deserializeNBT(compound);

        if (compound.hasKey(TAG_CUSTOM_NAME, Constants.NBT.TAG_STRING))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }
    }

    /**
     * Checks if the inventory is named.
     *
     * @return true if the inventory has a custom name.
     */
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
    protected int getStackLimit(int slot, ItemStack stack)
    {
        return Math.min(stack.getMaxStackSize(), MAX_STACK_SIZE);
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    protected void onContentsChanged(int index)
    {
        super.onContentsChanged(index);
        this.inventoryChanged = true;

        if (index == heldItem && getStackInSlot(index) == null)
        {
            if (citizen != null)
            {
                citizen.removeHeldItem();
            }
            heldItem = 0;
        }

        if (this.citizen != null)
        {
            this.citizen.onInventoryChanged();
        }
    }

    /**
     * Do not give this method the name canInteractWith because it clashes with Container.
     *
     * @param player the player acessing the inventory.
     * @return if the player is allowed to access.
     */
    public boolean isUseableByPlayer(@NotNull final EntityPlayer player)
    {
        return this.citizen.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    /**
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    /**
     * Used to store variables.
     */
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound compound = super.serializeNBT();
        if (this.hasCustomName())
        {
            compound.setString(TAG_CUSTOM_NAME, this.customName);
        }
        return compound;
    }
}

