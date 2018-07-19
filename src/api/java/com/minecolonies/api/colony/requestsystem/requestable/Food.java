package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Eatable requestable.
 * Delivers a stack of food.
 */
public class Food implements IDeliverable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Food(final int count) {this.count = count;}

    public Food(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public static NBTTagCompound serialize(final IFactoryController controller, final Food food)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.setTag(NBT_RESULT, food.result.serializeNBT());
        }

        return compound;
    }

    public static Food deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final int count = compound.getInteger(NBT_COUNT);
        final ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Food(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof ItemFood;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public int getCount()
    {
        return count;
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
        if (!(o instanceof Food))
        {
            return false;
        }

        final Food food = (Food) o;

        if (getCount() != food.getCount())
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), food.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getCount();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }
}
