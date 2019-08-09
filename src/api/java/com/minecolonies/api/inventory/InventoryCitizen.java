package com.minecolonies.api.inventory;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IItemHandler, INameable
{
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int NO_SLOT = -1;

    /**
     * The inventory. (36 main inventory, 4 armor slots, 1 offhand slot)
     */
    private final NonNullList<ItemStack> mainInventory = NonNullList.withSize(41, ItemStackUtils.EMPTY);

    /**
     * The index of the currently held items (0-8).
     */
    private int mainItem    = NO_SLOT;
    private int offhandItem = NO_SLOT;

    /**
     * The inventories custom name. In our case the citizens name.
     */
    private String customName;

    /**
     * The citizen which owns the inventory.
     */
    private ICitizenData citizen;

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final ICitizenData citizen)
    {
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
        if (localeEnabled)
        {
            customName = title;
        }
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
     * Returns the item that is currently being held by citizen.
     *
     * @return {@link ItemStack} currently being held by citizen.
     */
    public ItemStack getHeldItem(final Hand hand)
    {
        if (hand.equals(Hand.MAIN_HAND))
        {
            return getStackInSlot(mainItem);
        }

        return getStackInSlot(offhandItem);
    }

    /**
     * Set item to be held by citizen.
     *
     * @param slot Slot index with item to be held by citizen.
     */
    public void setHeldItem(final Hand hand, final int slot)
    {
        if (hand.equals(Hand.MAIN_HAND))
        {
            this.mainItem = slot;
        }

        this.offhandItem = slot;
    }

    /**
     * Gets slot that hold item that is being held by citizen.
     *
     * @return Slot index of held item
     */
    public int getHeldItemSlot(final Hand hand)
    {
        if (hand.equals(Hand.MAIN_HAND))
        {
            return mainItem;
        }

        return offhandItem;
    }

    @Override
    public int getSlots()
    {
        return this.mainInventory.size();
    }

    /**
     * Get the name of this object. For citizens this returns their name.
     *
     * @return the name of the inventory.
     */
    @NotNull
    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent(this.hasCustomName() ? this.customName : "citizen.inventory");
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
     * Returns the stack in the given slot.
     *
     * @param index the index.
     * @return the stack.
     */
    @NotNull
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        if (index > mainInventory.size())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            return mainInventory.get(index);
        }
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
    {
        final ItemStack inSlot = mainInventory.get(slot);
        if (inSlot.getCount() >= inSlot.getMaxStackSize() || !inSlot.isItemEqual(stack))
        {
            return stack;
        }

        final int avail = inSlot.getMaxStackSize() - inSlot.getCount();
        if (avail >= stack.getCount())
        {
            if (!simulate)
            {
                inSlot.setCount(inSlot.getMaxStackSize());
            }
            return ItemStack.EMPTY;
        }
        else
        {
            if (!simulate)
            {
                inSlot.setCount(inSlot.getCount() + stack.getCount());
            }
            stack.setCount(stack.getCount() - avail);
            return stack;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        final ItemStack inSlot = mainInventory.get(slot);

        if (amount >= inSlot.getCount())
        {
            if (!simulate)
            {
                mainInventory.set(slot, ItemStack.EMPTY);
            }
            return inSlot;
        }
        else
        {

            final ItemStack copy = inSlot.copy();
            copy.setCount(amount);
            if (!simulate)
            {
                inSlot.setCount(inSlot.getCount() - amount);
            }
            return copy;
        }
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return mainInventory.size() - 1;
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack)
    {
        final ItemStack inSlot = mainInventory.get(slot);
        if (inSlot.getCount() >= inSlot.getMaxStackSize() || !inSlot.isItemEqual(stack))
        {
            return false;
        }

        if (slot == 36)
        {
            return stack.getEquipmentSlot() == EquipmentSlotType.HEAD;
        }
        else if (slot == 37)
        {
            return stack.getEquipmentSlot() == EquipmentSlotType.CHEST;
        }
        else if (slot == 38)
        {
            return stack.getEquipmentSlot() == EquipmentSlotType.LEGS;
        }
        else if (slot == 39)
        {
            return stack.getEquipmentSlot() == EquipmentSlotType.FEET;
        }
        return true;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty()
    {
        if (this.citizen != null)
        {
            this.citizen.markDirty();
        }
    }

    /**
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new StringTextComponent(customName) : new StringTextComponent(citizen.getName());
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     *
     * @param nbtTagList the taglist in.
     * @return the filled list.
     */
    public ListNBT write(final ListNBT nbtTagList)
    {
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!(this.mainInventory.get(i)).isEmpty())
            {
                final CompoundNBT compoundNBT = new CompoundNBT();
                compoundNBT.putByte("Slot", (byte) i);
                (this.mainInventory.get(i)).write(compoundNBT);
                nbtTagList.add(compoundNBT);
            }
        }

        return nbtTagList;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     *
     * @param nbtTagList the tag list.
     */
    public void read(final ListNBT nbtTagList)
    {
        this.mainInventory.clear();

        for (int i = 0; i < nbtTagList.size(); ++i)
        {
            final CompoundNBT compoundNBT = nbtTagList.getCompound(i);
            final int j = compoundNBT.getByte("Slot") & 255;
            final ItemStack itemstack = ItemStack.read(compoundNBT);

            if (!itemstack.isEmpty())
            {
                if (j < this.mainInventory.size())
                {
                    this.mainInventory.set(j, itemstack);
                }
            }
        }
    }
}
