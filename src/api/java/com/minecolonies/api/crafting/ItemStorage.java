package com.minecolonies.api.crafting;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Used to store an stack with various informations to compare items later on.
 */
public class ItemStorage
{
    /**
     * The stack to store.
     */
    private final ItemStack stack;

    /**
     * Set this to ignore the damage value in comparisons.
     */
    private final boolean shouldIgnoreDamageValue;

    /**
     * Set this to ignore the damage value in comparisons.
     */
    private final boolean shouldIgnoreNBTValue;

    /**
     * The creative tab index of the storage.
     */
    private final List<Integer> creativeTabIndex = new ArrayList<>();

    /**
     * Amount of the storage.
     */
    private int amount;

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param amount            the amount.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStorage(@NotNull final ItemStack stack, final int amount, final boolean ignoreDamageValue)
    {
        this.stack = stack;
        this.shouldIgnoreDamageValue = ignoreDamageValue;
        this.shouldIgnoreNBTValue = ignoreDamageValue;
        this.amount = amount;
        this.creativeTabIndex = stack.getItem().creat() != null ? stack.getItem().getCreativeTab().index : 0;
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack                the stack.
     * @param ignoreDamageValue    should the damage value be ignored?
     * @param shouldIgnoreNBTValue should the nbt value be ignored?
     */
    public ItemStorage(@NotNull final ItemStack stack, final boolean ignoreDamageValue, final boolean shouldIgnoreNBTValue)
    {
        this.stack = stack;
        this.shouldIgnoreDamageValue = ignoreDamageValue;
        this.shouldIgnoreNBTValue = shouldIgnoreNBTValue;
        this.creativeTabIndex = stack.getItem().getCreativeTab() != null ? stack.getItem().getCreativeTab().index : 0;
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStorage(@NotNull final ItemStack stack, final boolean ignoreDamageValue)
    {
        this.stack = stack;
        this.shouldIgnoreDamageValue = ignoreDamageValue;
        this.shouldIgnoreNBTValue = ignoreDamageValue;
        this.amount = ItemStackUtils.getSize(stack);
        this.creativeTabIndex = stack.getItem().getCreativeTab() != null ? stack.getItem().getCreativeTab().index : 0;
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack the stack.
     */
    public ItemStorage(@NotNull final ItemStack stack)
    {
        this.stack = stack;
        this.shouldIgnoreDamageValue = false;
        this.shouldIgnoreNBTValue = false;
        this.amount = ItemStackUtils.getSize(stack);
        this.creativeTabIndex = stack.getItem().getCreativeTab() != null ? stack.getItem().getCreativeTab().index : 0;
    }

    /**
     * Check a list for an ItemStack matching a predicate.
     *
     * @param list      the list to check.
     * @param predicate the predicate to test.
     * @return the matching stack or null if not found.
     */
    public static ItemStorage getItemStackOfListMatchingPredicate(final List<ItemStorage> list, final Predicate<ItemStack> predicate)
    {
        for (final ItemStorage stack : list)
        {
            if (predicate.test(stack.getItemStack()))
            {
                return stack;
            }
        }
        return null;
    }

    /**
     * Get the itemStack from this itemStorage.
     *
     * @return the stack.
     */
    public ItemStack getItemStack()
    {
        return stack;
    }

    /**
     * Getter for the quantity.
     *
     * @return the amount.
     */
    public int getAmount()
    {
        return this.amount;
    }

    /**
     * Setter for the quantity.
     *
     * @param amount the amount.
     */
    public void setAmount(final int amount)
    {
        this.amount = amount;
    }

    /**
     * Getter for the ignoreDamageValue.
     *
     * @return true if should ignore.
     */
    public boolean ignoreDamageValue()
    {
        return shouldIgnoreDamageValue;
    }

    /**
     * Getter for the creativeTab index of the storage.
     * @return the index.
     */
    public int getCreativeTabIndex()
    {
        return creativeTabIndex;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(stack.getItem())
                + (this.shouldIgnoreDamageValue ? 0 : (this.stack.getItemDamage() * 31))
                + (this.shouldIgnoreNBTValue ? 0 : ((this.stack.getTagCompound() == null) ? 0 : this.stack.getTagCompound().hashCode()));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ItemStorage that = (ItemStorage) o;


        return stack.isItemEqual(that.getItemStack())
                && (this.shouldIgnoreDamageValue || that.getDamageValue() == this.getDamageValue())
                && (this.shouldIgnoreNBTValue
                      || (that.getItemStack().getTagCompound() == null && this.getItemStack().getTagCompound() == null)
                      || that.getItemStack().getTagCompound().equals(this.getItemStack().getTagCompound()));
    }

    /**
     * Getter for the stack.
     *
     * @return the stack.
     */
    @NotNull
    public Item getItem()
    {
        return stack.getItem();
    }

    /**
     * Getter for the damage value.
     *
     * @return the damage value.
     */
    public int getDamageValue()
    {
        return stack.getItemDamage();
    }
}