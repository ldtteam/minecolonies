package com.minecolonies.api.crafting;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
        this.amount = amount;
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
        this.amount = ItemStackUtils.getSize(stack);
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
        this.amount = ItemStackUtils.getSize(stack);
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

    @Override
    public int hashCode()
    {
        return 31 * getItem().hashCode() + getDamageValue();
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


        return getItem().equals(that.getItem()) && (this.shouldIgnoreDamageValue || that.getDamageValue() == this.getDamageValue());
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