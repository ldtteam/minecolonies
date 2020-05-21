package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent deliveries inside the request system.
 * This class can be used to request a getDelivery of a given ItemStack from a source to a target.
 */
public class Delivery extends AbstractDeliverymanRequestable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_START  = "Start";
    private static final String NBT_TARGET = "Target";
    private static final String NBT_STACK  = "Stack";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ILocation start;
    @NotNull
    private final ILocation target;
    @NotNull
    private final ItemStack stack;

    public Delivery(@NotNull final ILocation start, @NotNull final ILocation target, @NotNull final ItemStack stack, final int priority)
    {
        super(priority);
        this.start = start;
        this.target = target;
        this.stack = stack;
    }

    @NotNull
    public static CompoundNBT serialize(@NotNull final IFactoryController controller, final Delivery delivery)
    {
        final CompoundNBT compound = new CompoundNBT();

        compound.put(NBT_START, controller.serialize(delivery.getStart()));
        compound.put(NBT_TARGET, controller.serialize(delivery.getTarget()));
        compound.put(NBT_STACK, delivery.getStack().serializeNBT());
        compound.put(NBT_PRIORITY, controller.serialize(delivery.getPriority()));

        return compound;
    }

    @NotNull
    public ILocation getStart()
    {
        return start;
    }

    @NotNull
    public ILocation getTarget()
    {
        return target;
    }

    @NotNull
    public ItemStack getStack()
    {
        return stack;
    }

    @NotNull
    public static Delivery deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT compound)
    {
        final ILocation start = controller.deserialize(compound.getCompound(NBT_START));
        final ILocation target = controller.deserialize(compound.getCompound(NBT_TARGET));
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final int priority = controller.deserialize(compound.getCompound(NBT_PRIORITY));

        return new Delivery(start, target, stack, priority);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!super.equals(o))
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Delivery))
        {
            return false;
        }

        final Delivery delivery = (Delivery) o;

        if (!getStart().equals(delivery.getStart()))
        {
            return false;
        }
        if (!getTarget().equals(delivery.getTarget()))
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), delivery.getStack());
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + getStart().hashCode();
        result = 31 * result + getTarget().hashCode();
        result = 31 * result + getStack().hashCode();
        return result;
    }
}
