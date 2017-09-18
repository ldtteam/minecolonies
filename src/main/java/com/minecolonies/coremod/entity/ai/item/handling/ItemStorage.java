package com.minecolonies.coremod.entity.ai.item.handling;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    private final boolean ignoreDamageValue;

    /**
     * Set this to ignore the exact item, if you only want the class to be right.
     */
    private final boolean ignoreExactItem;

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
        this.ignoreDamageValue = ignoreDamageValue;
        this.ignoreExactItem = false;
        this.amount = amount;
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param ignoreDamageValue should the damage value be ignored?
     * @param ignoreDamageValue should the item be ignored?
     */
    public ItemStorage(@NotNull final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreExactItem)
    {
        this.stack = stack;
        this.ignoreDamageValue = ignoreDamageValue;
        this.ignoreExactItem = ignoreExactItem;
        this.amount = ItemStackUtils.getSize(stack);
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
        this.ignoreDamageValue = ignoreDamageValue;
        this.ignoreExactItem = false;
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
        this.ignoreDamageValue = false;
        this.ignoreExactItem = false;
        this.amount = ItemStackUtils.getSize(stack);
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
        return ignoreDamageValue;
    }

    @Override
    public int hashCode()
    {
        return 31 * getItem().hashCode() + (ignoreDamageValue? 0 : getDamageValue());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof ItemStorage))
        {
            return false;
        }

        final ItemStorage that = (ItemStorage) o;
        if(ignoreExactItem || that.ignoreExactItem)
        {
            return getItem().getClass().isAssignableFrom(that.getItem().getClass()) || that.getItem().getClass().isAssignableFrom(this.getItem().getClass());
        }

        return getItem().equals(that.getItem()) && (this.ignoreDamageValue || that.getDamageValue() == this.getDamageValue());
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
     * Get the itemStack from this itemStorage.
     *
     * @return the stack.
     */
    public ItemStack getItemStack()
    {
        return stack;
    }
}
