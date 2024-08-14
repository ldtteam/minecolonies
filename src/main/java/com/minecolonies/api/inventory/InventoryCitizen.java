package com.minecolonies.api.inventory;

import com.google.common.collect.Iterables;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_INV_SLOTS;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IItemHandlerModifiable, Nameable
{
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int NO_SLOT = -1;

    /**
     * The default inv size.
     */
    private static final int DEFAULT_INV_SIZE = 27;
    private static final int ROW_SIZE         = 9;

    /**
     * Amount of free slots
     */
    private int freeSlots = DEFAULT_INV_SIZE;

    /**
     * The inventory. (27 main inventory, 4 armor slots)
     */
    private NonNullList<ItemStack> mainInventory = NonNullList.withSize(DEFAULT_INV_SIZE, ItemStackUtils.EMPTY);
    private NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStackUtils.EMPTY);

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
    public ItemStack getHeldItem(final InteractionHand hand)
    {
        if (hand.equals(InteractionHand.MAIN_HAND))
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
    public void setHeldItem(final InteractionHand hand, final int slot)
    {
        if (hand.equals(InteractionHand.MAIN_HAND))
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
    public int getHeldItemSlot(final InteractionHand hand)
    {
        if (hand.equals(InteractionHand.MAIN_HAND))
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
     * Checks if the inventory has space
     *
     * @return true if the main inventory (without armor slots) has an empty slot.
     */
    public boolean hasSpace()
    {
        return freeSlots > 0;
    }

    /**
     * Checks if the inventory is completely empty.
     *
     * @return true if the main inventory (without armor slots) is completely empty.
     */
    public boolean isEmpty()
    {
        return freeSlots == mainInventory.size();
    }

    /**
     * Checks if the inventory is completely full.
     *
     * @return true if the main inventory (without armor slots) is completely full.
     */
    public boolean isFull()
    {
        return freeSlots == 0;
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

            for (int i = 0; i < mainInventory.size(); i++)
            {
                inv.set(i, mainInventory.get(i));
            }

            mainInventory = inv;
            freeSlots += futureSize - size;
        }
    }

    /**
     * Get the name of this object. For citizens this returns their name.
     *
     * @return the name of the inventory.
     */
    @NotNull
    @Override
    public Component getName()
    {
        return Component.translatableEscape(this.hasCustomName() ? this.customName : "citizen.inventory");
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
        if (index == NO_SLOT)
        {
            return ItemStack.EMPTY;
        }
        if (index >= mainInventory.size())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            return mainInventory.get(index);
        }
    }

    /**
     * Get the armor from a specific equipment slot.
     * @param equipmentSlot the slot to get it from.
     * @return the stack.
     */
    public ItemStack getArmorInSlot(final EquipmentSlot equipmentSlot)
    {
        if (equipmentSlot.isArmor())
        {
            return armorInventory.get(equipmentSlot.getIndex());
        }
        return ItemStack.EMPTY;
    }

    /**
     * Force an armor stack in a slot. This is for container interaction only.
     * @param equipmentSlot the slot to pick.
     * @param stack the stack to set.
     */
    public void forceArmorStackToSlot(final EquipmentSlot equipmentSlot, final ItemStack stack)
    {
        armorInventory.set(equipmentSlot.getIndex(), stack);
        if (citizen != null)
        {
            citizen.getEntity().ifPresent(citizen -> citizen.onArmorAdd(stack, equipmentSlot));
            markDirty();
        }
    }

    /**
     * Force remove armor stack from a slot. This is for container interaction only.
     * @param equipmentSlot the slot to clear.
     * @param stack the stack being removed.
     */
    public void forceClearArmorInSlot(final EquipmentSlot equipmentSlot, final ItemStack stack)
    {
        if (equipmentSlot.isArmor())
        {
            armorInventory.set(equipmentSlot.getIndex(), ItemStack.EMPTY);
            if (citizen != null)
            {
                citizen.getEntity().ifPresent(citizen -> citizen.onArmorRemove(stack, equipmentSlot));
                markDirty();
            }
        }
    }

    /**
     * Transfer from inventory slot to armor.
     * @param equipmentSlot the slot to transfer it to.
     * @param slot the slot to transfer it from.
     */
    public void transferArmorToSlot(final EquipmentSlot equipmentSlot, final int slot)
    {
        if (equipmentSlot.isArmor())
        {
            markDirty();
            final ItemStack oldArmorStack = armorInventory.get(equipmentSlot.getIndex());
            final ItemStack newArmorStack = getStackInSlot(slot);

            if (!oldArmorStack.isEmpty())
            {
                citizen.getEntity().ifPresent(citizen -> citizen.onArmorRemove(oldArmorStack, equipmentSlot));
            }

            armorInventory.set(equipmentSlot.getIndex(), newArmorStack);
            citizen.getEntity().ifPresent(citizen -> citizen.onArmorAdd(newArmorStack, equipmentSlot));
            setStackInSlot(slot, oldArmorStack);
        }
    }

    /**
     * Move armor from armor slots to inventory.
     * @param equipmentSlot the origin slot.
     */
    public void moveArmorToInventory(final EquipmentSlot equipmentSlot)
    {
        if (equipmentSlot.isArmor())
        {
            final ItemStack armorStack = armorInventory.get(equipmentSlot.getIndex());
            if (InventoryUtils.addItemStackToItemHandler(this, armorStack))
            {
                markDirty();
                armorInventory.set(equipmentSlot.getIndex(), ItemStack.EMPTY);
                citizen.getEntity().ifPresent(citizen -> citizen.onArmorRemove(armorStack, equipmentSlot));
            }
        }
    }

    /**
     * Damage an item within the inventory
     *
     * @param slot     slot to damage
     * @param amount   damage amount
     * @param entityIn entity which uses the item
     * @param onBroken action upon item break
     * @return true if the item broke
     */
    public <T extends LivingEntity> boolean damageInventoryItem(final int slot, int amount, @Nullable T entityIn, @Nullable Consumer<Item> onBroken)
    {
        final ItemStack stack = mainInventory.get(slot);
        if (!ItemStackUtils.isEmpty(stack))
        {
            // The 4 parameter inner call from forge is for adding a callback to alter the damage caused,
            // but unlike its description does not actually damage the item(despite the same function name). So used to just calculate the damage.
            stack.hurtAndBreak(stack.getItem().damageItem(stack, amount, entityIn, onBroken), (ServerLevel) entityIn.level(), entityIn, onBroken);

            if (ItemStackUtils.isEmpty(stack))
            {
                freeSlots++;
            }
        }

        return ItemStackUtils.isEmpty(stack);
    }

    /**
     * Shrinks an item in the given slot
     *
     * @param slot slot to shrink
     * @return true if item is empty afterwards
     */
    public boolean shrinkInventoryItem(final int slot)
    {
        final ItemStack stack = mainInventory.get(slot);
        if (!ItemStackUtils.isEmpty(stack))
        {
            stack.setCount(stack.getCount() - 1);

            if (ItemStackUtils.isEmpty(stack))
            {
                freeSlots++;
            }
        }

        return ItemStackUtils.isEmpty(stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
    {
        if (stack.isEmpty())
        {
            return stack;
        }

        final ItemStack copy = stack.copy();
        final ItemStack inSlot = mainInventory.get(slot);
        if (inSlot.getCount() >= inSlot.getMaxStackSize() || (!inSlot.isEmpty() && !ItemStackUtils.compareItemStacksIgnoreStackSize(inSlot, copy)))
        {
            return copy;
        }

        if (inSlot.isEmpty())
        {
            if (!simulate)
            {
                markDirty();
                freeSlots--;
                mainInventory.set(slot, copy);
                return ItemStack.EMPTY;
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }

        final int avail = inSlot.getMaxStackSize() - inSlot.getCount();
        if (avail >= copy.getCount())
        {
            if (!simulate)
            {
                markDirty();
                inSlot.setCount(inSlot.getCount() + copy.getCount());
            }
            return ItemStack.EMPTY;
        }
        else
        {
            if (!simulate)
            {
                markDirty();
                inSlot.setCount(inSlot.getCount() + avail);
            }
            copy.setCount(copy.getCount() - avail);
            return copy;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        final ItemStack inSlot = mainInventory.get(slot);
        if (inSlot.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        if (amount >= inSlot.getCount())
        {
            if (!simulate)
            {
                markDirty();
                freeSlots++;
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
                markDirty();
                inSlot.setCount(inSlot.getCount() - amount);
                if (ItemStackUtils.isEmpty(inSlot))
                {
                    freeSlots++;
                }
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
        return true;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it hasn't changed and skip it.
     */
    public void markDirty()
    {
        if (this.citizen != null)
        {
            this.citizen.markDirty(20);
        }
    }

    /**
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    @Override
    public Component getDisplayName()
    {
        return this.hasCustomName() ? Component.literal(customName) : Component.literal(citizen.getName());
    }

    /**
     * Writes the inventory to nbt.
     *
     * @param nbtTagCompound the compound to store it in.
     */
    public void write(@NotNull final HolderLookup.Provider provider, final CompoundTag nbtTagCompound)
    {
        if (citizen != null && citizen.getColony() != null)
        {
            final double researchEffect = citizen.getColony().getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_INV_SLOTS);
            if (researchEffect > 0 && this.mainInventory.size() < DEFAULT_INV_SIZE + researchEffect)
            {
                resizeInventory(this.mainInventory.size(), (int) (DEFAULT_INV_SIZE + researchEffect));
            }
        }

        nbtTagCompound.putInt(TAG_INV_SIZE, this.mainInventory.size());

        final ListTag invTagList = new ListTag();
        freeSlots = mainInventory.size();
        for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!(this.mainInventory.get(i)).isEmpty())
            {
                final CompoundTag compoundNBT = new CompoundTag();
                compoundNBT.putByte("Slot", (byte) i);
                invTagList.add(this.mainInventory.get(i).save(provider, compoundNBT));
                freeSlots--;
            }
        }
        nbtTagCompound.put(TAG_INVENTORY, invTagList);

        final ListTag armorTagList = new ListTag();
        for (int i = 0; i < this.armorInventory.size(); ++i)
        {
            if (!(this.armorInventory.get(i)).isEmpty())
            {
                final CompoundTag compoundNBT = new CompoundTag();
                compoundNBT.putByte("Slot", (byte) i);
                (this.armorInventory.get(i)).save(provider, compoundNBT);
                armorTagList.add(compoundNBT);
            }
        }
        nbtTagCompound.put(TAG_ARMOR_INVENTORY, armorTagList);
    }

    /**
     * Reads from the given compound and fills the slots in the inventory with the correct items.
     *
     * @param nbtTagCompound the compound.
     */
    public void read(@NotNull final HolderLookup.Provider provider, final CompoundTag nbtTagCompound)
    {
        if (nbtTagCompound.contains(TAG_ARMOR_INVENTORY))
        {
            int size = nbtTagCompound.getInt(TAG_INV_SIZE);
            if (this.mainInventory.size() < size)
            {
                size -= size % ROW_SIZE;
                this.mainInventory = NonNullList.withSize(size, ItemStackUtils.EMPTY);
            }

            freeSlots = mainInventory.size();

            final ListTag nbtTagList = nbtTagCompound.getList(TAG_INVENTORY, 10);
            for (int i = 0; i < nbtTagList.size(); i++)
            {
                final CompoundTag compoundNBT = nbtTagList.getCompound(i);
                final int j = compoundNBT.getByte("Slot") & 255;
                final ItemStack itemstack = ItemStack.parseOptional(provider, compoundNBT);

                if (!itemstack.isEmpty())
                {
                    if (j < this.mainInventory.size())
                    {
                        this.mainInventory.set(j, itemstack);
                        freeSlots--;
                    }
                }
            }

            final ListTag armorTagList = nbtTagCompound.getList(TAG_ARMOR_INVENTORY, 10);
            for (int i = 0; i < armorTagList.size(); ++i)
            {
                final CompoundTag compoundNBT = armorTagList.getCompound(i);
                final int j = compoundNBT.getByte("Slot") & 255;
                final ItemStack itemstack = ItemStack.parseOptional(provider, compoundNBT);

                if (!itemstack.isEmpty())
                {
                    if (j < this.armorInventory.size())
                    {
                        this.armorInventory.set(j, itemstack);
                    }
                }
            }
        }
        else
        {
            final ListTag nbtTagList = nbtTagCompound.getList(TAG_INVENTORY, 10);
            if (this.mainInventory.size() < nbtTagList.getCompound(0).getInt(TAG_SIZE))
            {
                int size = nbtTagList.getCompound(0).getInt(TAG_SIZE);
                size -= size % ROW_SIZE;
                this.mainInventory = NonNullList.withSize(size, ItemStackUtils.EMPTY);
            }

            freeSlots = mainInventory.size();

            for (int i = 1; i < nbtTagList.size(); i++)
            {
                final CompoundTag compoundNBT = nbtTagList.getCompound(i);

                final int j = compoundNBT.getByte("Slot") & 255;
                final ItemStack itemstack = ItemStack.parseOptional(provider, compoundNBT);

                if (!itemstack.isEmpty())
                {
                    if (j < this.mainInventory.size())
                    {
                        this.mainInventory.set(j, itemstack);
                        freeSlots--;
                    }
                }
            }
        }
    }

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack)
    {
        if (!ItemStackUtils.isEmpty(stack))
        {
            if (ItemStackUtils.isEmpty(mainInventory.get(slot)))
            {
                freeSlots--;
            }
        }
        else if (!ItemStackUtils.isEmpty(mainInventory.get(slot)))
        {
            freeSlots++;
        }

        mainInventory.set(slot, stack);
    }

    /**
     * Get an iterable of armor and hand inventory.
     * @return the itemstack iterable.
     */
    public Iterable<ItemStack> getIterableArmorAndHandInv()
    {
        return Iterables.concat(armorInventory, List.of(getStackInSlot(mainItem), getStackInSlot(offhandItem)));
    }
}
