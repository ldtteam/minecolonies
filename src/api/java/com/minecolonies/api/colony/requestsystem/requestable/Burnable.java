package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
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
    private ItemStack result;

    public Burnable(final int count) {this.count = count;}

    public Burnable(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public static NBTTagCompound serialize(IFactoryController controller, Burnable food)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.setTag(NBT_RESULT, food.result.serializeNBT());
        }

        return compound;
    }

    public static Burnable deserialize(IFactoryController controller, NBTTagCompound compound)
    {
        int count = compound.getInteger(NBT_COUNT);
        ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Burnable(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return TileEntityFurnace.isItemFuel(stack) && stack.getCount() >= getCount();
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




}
