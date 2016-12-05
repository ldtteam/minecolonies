package com.minecolonies.coremod.entity.ai.item.handling;

import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Used to store an item with various informations to compare items later on.
 */
public class ItemStorage
{
    /**
     * The item to store.
     */
    private final Item item;

    /**
     * The damage value.
     */
    private final int damageValue;

    /**
     * The amount.
     */
    private final int amount;

    /**
     * Set this to ignore the damage value in comparisons.
     */
    private final boolean ignoreDamageValue;

    /**
     * Creates an instance of the storage.
     *
     * @param item              the item.
     * @param damageValue       it's damage value.
     * @param amount            optional amount.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStorage(@NotNull final Item item, final int damageValue, final int amount, final boolean ignoreDamageValue)
    {
        this.item = item;
        this.damageValue = damageValue;
        this.amount = amount;
        this.ignoreDamageValue = ignoreDamageValue;
    }

    /**
     * Getter for the quantity.
     *
     * @return the amount.
     */
    public int getAmount()
    {
        return amount;
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
        return damageValue;
    }

    /**
     * Getter for the item.
     *
     * @return the item.
     */
    @NotNull
    public Item getItem()
    {
        return item;
    }
}
