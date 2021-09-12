package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

public class ItemStackHandling extends ItemStorage
{
    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param amount            the amount.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStackHandling(@NotNull final ItemStack stack, final int amount, final boolean ignoreDamageValue)
    {
        super(stack, amount, ignoreDamageValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack                the stack.
     * @param ignoreDamageValue    should the damage value be ignored?
     * @param shouldIgnoreNBTValue should the nbt value be ignored?
     */
    public ItemStackHandling(@NotNull final ItemStack stack, final boolean ignoreDamageValue, final boolean shouldIgnoreNBTValue)
    {
        super(stack,ignoreDamageValue, shouldIgnoreNBTValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ItemStackHandling(@NotNull final ItemStack stack, final boolean ignoreDamageValue)
    {
        super(stack,ignoreDamageValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack the stack.
     */
    public ItemStackHandling(@NotNull final ItemStack stack)
    {
        super(stack);
    }

    /**
     * Creates an instance of the storage from JSON
     * 
     * @param jObject the JSON Object to parse
     */
    public ItemStackHandling(@NotNull final JsonObject jObject)
    {
        super(jObject);
    }

    @Override
    public ItemStorage copy()
    {
        ItemStorage newInstance = new ItemStackHandling(stack.copy(), shouldIgnoreDamageValue, shouldIgnoreNBTValue);
        newInstance.setAmount(amount);
        return newInstance;
    }    

}
