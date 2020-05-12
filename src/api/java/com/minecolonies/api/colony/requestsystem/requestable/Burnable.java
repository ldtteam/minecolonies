package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Burnable requestable.
 * Delivers a stack of burnable fuel.
 */
public class Burnable implements IDeliverable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Burnable(final int count) {this.count = count;}

    public Burnable(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public static CompoundNBT serialize(final IFactoryController controller, final Burnable food)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.put(NBT_RESULT, food.result.serializeNBT());
        }

        return compound;
    }

    public static Burnable deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.keySet().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Burnable(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return FurnaceTileEntity.isFuel(stack);
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(@NotNull final int newCount)
    {
        return new Burnable(newCount);
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public int getMinimumCount()
    {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getResult()
    {
        return result;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Burnable))
        {
            return false;
        }

        final Burnable burnable = (Burnable) o;

        if (getCount() != burnable.getCount())
        {
            return false;
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), burnable.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getCount();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }
}
