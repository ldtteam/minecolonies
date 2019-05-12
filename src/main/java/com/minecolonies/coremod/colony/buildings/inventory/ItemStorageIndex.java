package com.minecolonies.coremod.colony.buildings.inventory;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Index which uses Items(ItemStorage) as key.
 */
public class ItemStorageIndex<Value> extends HashSetIndex<ItemStorage, Value>
{
    /**
     * Adds an Itemstack to the index.
     *
     * @param stack Stack to add
     * @param val   Value to associate to the stack
     */
    public void addToIndex(ItemStack stack, Value val)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        this.addToIndex(new ItemStorage(stack), val);
    }

    /**
     * Adds an Itemstack to the index.
     *
     * @param stack Stack to add
     * @param val   Value to associate to the stack
     */
    public void removeFromIndex(ItemStack stack, Value val)
    {
        this.removeFromIndex(new ItemStorage(stack), val);
    }

    @Override
    public void addToIndex(ItemStorage key, Value val)
    {
        if (key == null || val == null || ItemStackUtils.isEmpty(key.getItemStack()))
        {
            return;
        }
        super.addToIndex(key, val);
    }

    @Override
    public void removeFromIndex(ItemStorage key, Value val)
    {
        if (key == null || val == null)
        {
            return;
        }
        super.removeFromIndex(key, val);
    }

    /**
     * Returns the first matching index entry for the given itemstack predicate.
     */
    public Tuple<ItemStorage, Set<Value>> getFirstEntryForItemStackPredicate(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        for (final Map.Entry<ItemStorage, Set<Value>> entry : getIndexMap().entrySet())
        {
            if (itemStackSelectionPredicate.test(entry.getKey().getItemStack()))
            {
                return new Tuple(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    /**
     * Returns a list of all ItemStorage keys matching the give predicate.
     *
     * @param predicate The itemstack predicate to search for.
     * @return List of matching items.
     */
    public List<ItemStorage> getAllMatchingItems(@NotNull final Predicate<ItemStack> predicate)
    {
        List<ItemStorage> found = new ArrayList<>();
        for (final Map.Entry<ItemStorage, Set<Value>> entry : getIndexMap().entrySet())
        {
            if (predicate.test(entry.getKey().getItemStack()))
            {
                found.add(entry.getKey());
            }
        }
        return found;
    }

    /**
     * Returns all matching values for a given predicate.
     *
     * @param itemStackSelectionPredicate predicate to select items.
     * @return Set of values matching the predicate.
     */
    public Set<Value> getValueForItemStackPredicate(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        Set<Value> values = new HashSet<>();
        for (Map.Entry<ItemStorage, Set<Value>> entry : getIndexMap().entrySet())
        {
            if (itemStackSelectionPredicate.test(entry.getKey().getItemStack()))
            {
                values.addAll(entry.getValue());
            }
        }
        return values;
    }
}

