package com.minecolonies.coremod.colony.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Smeltable requestable.
 * Delivers a stack of a smeltable ore.
 */
public class SmeltableOre implements IDeliverable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    private ItemStack result;

    public SmeltableOre(final int count)
    {
        this.count = count;
    }

    public SmeltableOre(final int count, final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public static NBTTagCompound serialize(final IFactoryController controller, final SmeltableOre food)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.setTag(NBT_RESULT, food.result.serializeNBT());
        }

        return compound;
    }

    public static SmeltableOre deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final int count = compound.getInteger(NBT_COUNT);
        final ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new SmeltableOre(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return ItemStackUtils.IS_SMELTABLE.and(
            itemStack -> ColonyManager.getCompatibilityManager().isOre(itemStack)).test(stack);
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
