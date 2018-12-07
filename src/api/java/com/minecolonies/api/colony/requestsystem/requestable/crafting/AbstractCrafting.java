package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCrafting implements IRequestable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_STACK       = "Stack";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack theStack;

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     */
    public AbstractCrafting(@NotNull final ItemStack stack)
    {
        this.theStack = stack.copy();

        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        this.theStack.setCount(Math.min(this.theStack.getCount(), this.theStack.getMaxStackSize()));
    }
/*
    *//**
     * Serialize the deliverable.
     * @param controller the controller.
     * @param input the input.
     * @return the compound.
     *//*
    public static NBTTagCompound serialize(final IFactoryController controller, final AbstractCrafting input)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_STACK, input.theStack.serializeNBT());

        return compound;
    }

    *//**
     * Deserialize the deliverable.
     * @param controller the controller.
     * @param compound the compound.
     * @return the deliverable.
     *//*
    public static AbstractCrafting deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_STACK));

        return new AbstractCrafting(stack);
    }*/

    @NotNull
    public ItemStack getStack()
    {
        return theStack;
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

        final AbstractCrafting stack1 = (AbstractCrafting) o;

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack1.getStack());
    }

    @Override
    public int hashCode()
    {
        return getStack().hashCode();
    }
}
