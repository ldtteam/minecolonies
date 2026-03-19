package com.minecolonies.api.crafting;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Static instances of itemstorage only allowed to be used for comparisons e.g. checking contains in a set
 */
public class CompareStorage extends ItemStorage
{
    private static int              nextIndex = 0;
    private static CompareStorage[] pool      = new CompareStorage[100];
    static
    {
        for (int i = 0; i < pool.length; i++)
        {
            pool[i] = new CompareStorage(i);
        }
    }

    private final int         creationIndex;
    private       ItemStack   comparedStack                  = ItemStack.EMPTY;
    private       boolean     compareShouldIgnoreDamageValue = false;
    private       boolean     compareShouldIgnoreNBTValue    = false;
    private       ItemStorage foundEqualStorage              = null;

    public static CompareStorage of(final ItemStack stack, final boolean shouldIgnoreDamageValue, final boolean shouldIgnoreNBTValue)
    {
        final CompareStorage storage = pool[nextIndex];
        nextIndex = (nextIndex + 1) % pool.length;

        storage.comparedStack = stack;
        storage.compareShouldIgnoreDamageValue = shouldIgnoreDamageValue;
        storage.compareShouldIgnoreNBTValue = shouldIgnoreNBTValue;
        storage.foundEqualStorage = null;
        return storage;
    }

    private CompareStorage(int creationIndex)
    {
        super(ItemStack.EMPTY, 1, false);
        this.creationIndex = creationIndex;
    }

    /**
     * Get the ItemStorage found to be equal to this compare storage lookup. Gets filled on equals match when using .get or .contains on a set or map with itemstorages
     *
     * @return
     */
    public ItemStorage getFoundEqualStorage()
    {
        return foundEqualStorage;
    }

    /**
     * Returns true when an equal ItemStorage was found during a lookup
     *
     * @return
     */
    public boolean foundEqualStorage()
    {
        return foundEqualStorage != null;
    }

    @Override
    public int hashCode()
    {
        if (nextIndex == creationIndex)
        {
            throw new RuntimeException("Access after pool limit is reached, this object should not be stored and used much later!");
        }

        return Objects.hash(comparedStack.getItem());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (nextIndex == creationIndex)
        {
            throw new RuntimeException("Access after pool limit is reached, this object should not be stored and used much later!");
        }

        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final ItemStorage that))
        {
            return false;
        }

        boolean equals = ItemStackUtils.compareItemStacksIgnoreStackSize(that.getItemStack(),
            comparedStack,
            !(this.compareShouldIgnoreDamageValue || that.shouldIgnoreDamageValue),
            !(this.compareShouldIgnoreNBTValue || that.shouldIgnoreNBTValue));

        if (equals)
        {
            foundEqualStorage = (ItemStorage) o;
        }
        return equals;
    }
}
