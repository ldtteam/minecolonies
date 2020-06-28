package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Abstract crafting request.
 */
public abstract class AbstractCrafting implements IRequestable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_STACK       = "Stack";
    protected static final String NBT_COUNT       = "Count";
    protected static final String NBT_MIN_COUNT       = "Count";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * The stack.
     */
    @NotNull
    private final ItemStack theStack;

    /**
     * The count.
     */
    private final int count;

    /**
     * The minimum count.
     */
    private final int minCount;

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     * @param count the crafting count.
     * @param minCount the min crafting count.
     */
    public AbstractCrafting(@NotNull final ItemStack stack, final int count, final int minCount)
    {
        this.theStack = stack.copy();
        this.count = count;
        this.minCount = minCount;

        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        this.theStack.setCount(Math.min(this.theStack.getCount(), this.theStack.getMaxStackSize()));
    }

    @NotNull
    public ItemStack getStack()
    {
        return theStack;
    }

    /**
     * Get the count to fulfill.
     * @return the count.
     */
    public int getCount()
    {
        return count;
    }

    /**
     * Get the min count to fulfill.
     * @return the min count.
     */
    public int getMinCount()
    {
        return minCount;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractCrafting))
        {
            return false;
        }
        final AbstractCrafting that = (AbstractCrafting) o;
        return getCount() == that.getCount() && getMinCount() == that.getMinCount() && theStack.equals(that.theStack);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(theStack, getCount(), getMinCount());
    }
}
