package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent deliveries inside the request system.
 * This class can be used to request a getDelivery of a given ItemStack from a source to a target.
 */
public class Delivery
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

    public Delivery(@NotNull ILocation start, @NotNull ILocation target, @NotNull ItemStack stack)
    {
        this.start = start;
        this.target = target;
        this.stack = stack;
    }

    @NotNull
    public static Delivery deserialize(@NotNull IFactoryController controller, @NotNull NBTTagCompound compound)
    {
        ILocation start = controller.deserialize(compound.getCompoundTag(NBT_START));
        ILocation target = controller.deserialize(compound.getCompoundTag(NBT_TARGET));
        ItemStack stack = ItemStackUtils.loadItemStackFromNBT(compound.getCompoundTag(NBT_STACK));

        return new Delivery(start, target, stack);
    }

    @NotNull
    public ILocation getStart()
    {
        return start;
    }

    @NotNull
    public NBTTagCompound serialize(@NotNull IFactoryController controller)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_START, controller.serialize(getStack()));
        compound.setTag(NBT_TARGET, controller.serialize(getTarget()));
        compound.setTag(NBT_STACK, getStack().serializeNBT());

        return compound;
    }

    @NotNull
    public ItemStack getStack()
    {
        return stack;
    }

    @NotNull
    public ILocation getTarget()
    {
        return target;
    }
}
