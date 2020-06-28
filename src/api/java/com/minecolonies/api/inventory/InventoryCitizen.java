package com.minecolonies.api.inventory;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.research.effects.AbstractResearchEffect;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.research.util.ResearchConstants.INV_SLOTS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SIZE;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IItemHandlerModifiable, INameable
{
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int NO_SLOT = -1;

    /**
     * The default inv size.
     */
    private static final int DEFAULT_INV_SIZE = 27;

    /**
     * Armor inv size.
     */
    private static final int ARMOR_SIZE = 5;

    /**
     * The inventory. (27 main inventory, 4 armor slots, 1 offhand slot)
     */
    private NonNullList<ItemStack> mainInventory = NonNullList.withSize(DEFAULT_INV_SIZE + ARMOR_SIZE, ItemStackUtils.EMPTY);

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
     * @param hand the hand it is held in.
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
     * @param hand the hand it is held in.
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
     * @param hand the hand it is held in.
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
        return this.mainInventory.size() - ARMOR_SIZE;
    }

    /**
     * Checks if the inventory has space
     *
     * @return true if the main inventory (without armor slots) has an empty slot.
     */
    public boolean hasSpace()
    {
        return this.mainInventory.stream().limit(getSlots()).anyMatch(itemStack -> itemStack.isEmpty());
    }

    /**
     * Checks if the inventory is completely empty.
     *
     * @return true if the main inventory (without armor slots) is completely empty.
     */
    public boolean isEmpty()
    {
        return !this.mainInventory.stream().limit(getSlots()).anyMatch(itemStack -> !itemStack.isEmpty());
    }

    /**
     * Resize this inventory.
     *
     * @param size       the current size.
     * @param futureSize the future size.
     */
    private void resizeInventory(final int size, final int futureSize)
    {
        if (size < futureSize)
        {
            final NonNullList<ItemStack> inv = NonNullList.withSize(futureSize, ItemStackUtils.EMPTY);

            for (int i = 0; i < mainInventory.size() - ARMOR_SIZE; i++)
            {
                inv.set(i, mainInventory.get(i));
            }

            int index = inv.size() - ARMOR_SIZE;
            for (int i = mainInventory.size() - ARMOR_SIZE; i < mainInventory.size(); i++)
            {
                inv.set(index++, mainInventory.get(i));
            }
            mainInventory = inv;
        }
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
        if (index >= mainInventory.size())
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
        if (inSlot.getCount() >= inSlot.getMaxStackSize() || (!inSlot.isEmpty() && !inSlot.isItemEqual(stack)))
        {
            return stack;
        }

        if (inSlot.isEmpty())
        {
            if (!simulate)
            {
                mainInventory.set(slot, stack);
                return ItemStack.EMPTY;
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }

        final int avail = inSlot.getMaxStackSize() - inSlot.getCount();
        if (avail >= stack.getCount())
        {
            if (!simulate)
            {
                inSlot.setCount(inSlot.getCount() + stack.getCount());
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
        return 64;
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack)
    {
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
        if (citizen != null && citizen.getColony() != null)
        {
            final AbstractResearchEffect<Double> researchEffect =
              citizen.getColony().getResearchManager().getResearchEffects().getEffect(INV_SLOTS, AbstractResearchEffect.class);
            if (researchEffect != null && this.mainInventory.size() - ARMOR_SIZE < DEFAULT_INV_SIZE + researchEffect.getEffect())
            {
                resizeInventory(this.mainInventory.size(), (int) (DEFAULT_INV_SIZE + researchEffect.getEffect() + ARMOR_SIZE));
            }
        }

        final CompoundNBT sizeNbt = new CompoundNBT();
        sizeNbt.putInt(TAG_SIZE, this.mainInventory.size());
        nbtTagList.add(sizeNbt);

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
        if (this.mainInventory.size() < nbtTagList.getCompound(0).getInt(TAG_SIZE))
        {
            this.mainInventory = NonNullList.withSize(nbtTagList.getCompound(0).getInt(TAG_SIZE), ItemStackUtils.EMPTY);
        }

        for (int i = 1; i < nbtTagList.size(); ++i)
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

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack)
    {
        mainInventory.set(slot, stack);
    }
}
