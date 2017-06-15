package com.minecolonies.coremod.entity.ai.item.handling;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Used to store an item with various informations to compare items later on.
 */
public class ItemStorage
{
    /**
     * The item to store.
     */
    private final ItemStack item;

    /**
     * Set this to ignore the damage value in comparisons.
     */
    private final boolean ignoreDamageValue;

    /**
     * Creates an instance of the storage.
     *
     * @param stack              the item.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStorage(@NotNull final ItemStack item, final boolean ignoreDamageValue)
    {
        this.item = item;
        this.ignoreDamageValue = ignoreDamageValue;
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack              the item.
     */
    public ItemStorage(@NotNull final ItemStack stack)
    {
        this.item = stack;
        this.ignoreDamageValue = false;
    }

    /**
     * Getter for the quantity.
     *
     * @return the amount.
     */
    public int getAmount()
    {
        return ItemStackUtils.getSize(item);
    }

    /**
     * Setter for the quantity.
     *
     * @param amount the amount.
     */
    public void setAmount(final int amount)
    {
        ItemStackUtils.setSize(item, ItemStackUtils.getSize(item) +  amount);
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


        return getItem().equals(that.getItem()) && (this.ignoreDamageValue || that.getDamageValue() == this.getDamageValue());
    }

    /**
     * Getter for the damage value.
     *
     * @return the damage value.
     */
    public int getDamageValue()
    {
        return item.getItemDamage();
    }

    /**
     * Getter for the item.
     *
     * @return the item.
     */
    @NotNull
    public Item getItem()
    {
        return item.getItem();
    }

    /**
     * Get the itemStack from this itemStorage.
     * @return the stack.
     */
    public ItemStack getItemStack()
    {
        return item;
    }
}
