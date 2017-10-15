package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items.
 */
public class Stack implements IDeliverable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK  = "Stack";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack stack;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Stack(@NotNull final ItemStack stack) {
        if (ItemStackUtils.isEmpty(stack)) {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        this.stack = stack;
    }

    public Stack(@NotNull final ItemStack stack, @NotNull final ItemStack result)
    {
        this.stack = stack;
        this.result = result;
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return ItemStackUtils.compareItemStacksIgnoreStackSize(stack, this.stack) && stack.getCount() >= getCount();
    }

    @Override
    public int getCount()
    {
        return stack.getCount();
    }

    @NotNull
    public ItemStack getStack()
    {
        return stack;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @NotNull
    @Override
    public ItemStack getResult()
    {
        return result;
    }

    public static NBTTagCompound serialize(IFactoryController controller, Stack input)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_STACK, input.stack.serializeNBT());

        if (!ItemStackUtils.isEmpty(input.result))
            compound.setTag(NBT_RESULT, input.result.serializeNBT());

        return compound;
    }

    public static Stack deserialize(IFactoryController controller, NBTTagCompound compound) {
        ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_STACK));
        ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Stack(stack, result);
    }
}

