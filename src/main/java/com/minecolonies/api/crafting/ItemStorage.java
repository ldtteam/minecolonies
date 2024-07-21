package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
    protected final boolean shouldIgnoreDamageValue;

    /**
     * Set this to ignore the damage value in comparisons.
     */
    protected final boolean shouldIgnoreNBTValue;

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
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param amount            the amount.
     * @param ignoreDamageValue should the damage value be ignored?
     * @param ignoreNBTValue    should the nbt value be ignored?
     */
    public ItemStorage(@NotNull final ItemStack stack, final int amount, final boolean ignoreDamageValue, final boolean ignoreNBTValue)
    {
        this.stack = stack;
        this.shouldIgnoreDamageValue = ignoreDamageValue;
        this.shouldIgnoreNBTValue = ignoreNBTValue;
        this.amount = amount;
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
        this.shouldIgnoreDamageValue = ignoreDamageValue;
        this.shouldIgnoreNBTValue = false;
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
        this.shouldIgnoreNBTValue = false;
        this.amount = ItemStackUtils.getSize(stack);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param item the item.
     */
    public ItemStorage(@NotNull final Item item)
    {
        this(item.getDefaultInstance());
    }

    /**
     * Creates an instance of the storage from JSON
     * 
     * @param jObject the JSON Object to parse
     */
    public ItemStorage(@NotNull final JsonObject jObject)
    {
        if (jObject.has(ITEM_PROP))
        {
            final ItemStack parsedStack = ItemStackUtils.idToItemStack(jObject.get(ITEM_PROP).getAsString());
            if(jObject.has(COUNT_PROP))
            {
                parsedStack.setCount(jObject.get(COUNT_PROP).getAsInt());
                this.amount = jObject.get(COUNT_PROP).getAsInt();
            }
            else
            {
                this.amount = parsedStack.getCount();
            }
            this.stack = parsedStack;
            if(jObject.has(MATCHTYPE_PROP))
            {
                String matchType = jObject.get(MATCHTYPE_PROP).getAsString();
                if(matchType.equals(MATCH_NBTIGNORE))
                {
                    this.shouldIgnoreNBTValue = true;
                }
                else // includes "exact"
                {
                    this.shouldIgnoreNBTValue = false;
                }
            }
            else
            {
                this.shouldIgnoreNBTValue = false;
            }
            this.shouldIgnoreDamageValue= true;
        }
        else
        {
            this.stack = ItemStack.EMPTY;
            this.amount = 0;
            this.shouldIgnoreDamageValue = true;
            this.shouldIgnoreNBTValue = true;
        }
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
     * Getter for the ignoreNBT.
     *
     * @return true if should ignore.
     */
    public boolean ignoreNBT()
    {
        return shouldIgnoreNBTValue;
    }

    @Override
    public String toString()
    {
        final ItemStack stack = this.stack.copy();
        stack.setCount(this.amount);
        return stack.toString();
    }

    @Override
    public int hashCode()
    {
        //Only use the stack itself for the has, equals will handle the broader attributes
        return Objects.hash(stack.getItem());
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
        return ItemStackUtils.compareItemStacksIgnoreStackSize(that.getItemStack(), this.getItemStack(), !(this.shouldIgnoreDamageValue || that.shouldIgnoreDamageValue), !(this.shouldIgnoreNBTValue || that.shouldIgnoreNBTValue));
    }

    /**
     * Ensure that two ItemStorage have the same comparison defintion
     * @param that the item to compare to
     * @return true if the comparisons match
     */
    public boolean matchDefinitionEquals(ItemStorage that)
    {
        return this.shouldIgnoreDamageValue == that.shouldIgnoreDamageValue 
        && this.shouldIgnoreNBTValue == that.shouldIgnoreNBTValue;
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
        return stack.getDamageValue();
    }

    /**
     * Getter for the remaining durability value.
     *
     * @return the durability value.
     */
    public int getRemainingDurablityValue()
    {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    /**
     * Is this an empty ItemStorage
     * 
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return ItemStackUtils.isEmpty(stack) || amount <= 0;
    }

    /**
     * Make a copy of the ItemStorage
     * @return a copy
     */
    public ItemStorage copy()
    {
        ItemStorage newInstance = new ItemStorage(stack.copy(), shouldIgnoreDamageValue, shouldIgnoreNBTValue);
        newInstance.setAmount(amount);
        return newInstance;
    }    

    /**
     * Get an immutable version of this item storage
     * @return immutable wrapper
     */
    public ImmutableItemStorage toImmutable()
    {
        return new ImmutableItemStorage(this);
    }
}