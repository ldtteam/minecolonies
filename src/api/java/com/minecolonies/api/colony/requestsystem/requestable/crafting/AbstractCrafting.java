package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractCrafting implements IRequestable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_STACK       = "Stack";
    protected static final String NBT_COUNT       = "Count";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack theStack;

    private final int count;

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     * @param count the crafting count.
     */
    public AbstractCrafting(@NotNull final ItemStack stack, final int count)
    {
        this.theStack = stack.copy();
        this.count = count;

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

    public int getCount()
    {
        return count;
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
        return getCount() == that.getCount() &&
                 theStack.equals(that.theStack);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(theStack, getCount());
    }
}
