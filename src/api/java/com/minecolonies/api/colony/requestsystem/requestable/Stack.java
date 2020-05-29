package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items.
 */
public class Stack implements IDeliverable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK       = "Stack";
    private static final String NBT_MATCHMETA   = "MatchMeta";
    private static final String NBT_MATCHNBT    = "MatchNBT";
    private static final String NBT_MATCHOREDIC = "MatchOreDic";
    private static final String NBT_RESULT      = "Result";
    private static final String NBT_COUNT       = "Count";
    private static final String NBT_MINCOUNT    = "MinCount";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack theStack;

    /**
     * If meta should match.
     */
    private boolean matchMeta;

    /**
     * If NBT should match.
     */
    private boolean matchNBT;

    /**
     * If oredict should match.
     */
    private boolean matchOreDic;

    /**
     * The required count.
     */
    private int count;

    /**
     * The required count.
     */
    private int minCount;

    @NotNull
    private ItemStack result;

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     */
    public Stack(@NotNull final ItemStack stack)
    {
        this(stack, true, false, false, ItemStackUtils.EMPTY, Math.min(stack.getCount(), stack.getMaxStackSize()), Math.min(stack.getCount(), stack.getMaxStackSize()));
    }

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     * @param count the count.
     * @param minCount the min count.
     */
    public Stack(@NotNull final ItemStack stack, final int count, final int minCount)
    {
        this(stack, true, false, false, ItemStackUtils.EMPTY, count, minCount);
    }

    /**
     * Transform an itemStorage into this predicate.
     * @param itemStorage the storage to use.
     */
    public Stack(@NotNull final ItemStorage itemStorage)
    {
        this(itemStorage.getItemStack(), !itemStorage.ignoreDamageValue(), false, false, ItemStackUtils.EMPTY, itemStorage.getAmount(), itemStorage.getAmount());
    }

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     * @param matchMeta if meta has to be matched.
     * @param matchNBT if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result the result stack.
     * @param count the count.
     * @param minCount the min count.
     */
    public Stack(
            @NotNull final ItemStack stack,
            final boolean matchMeta,
            final boolean matchNBT,
            final boolean matchOreDic,
            @NotNull final ItemStack result ,
            final int count,
            final int minCount)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        this.theStack = stack.copy();
        this.matchMeta = matchMeta;
        this.matchNBT = matchNBT;
        this.matchOreDic = matchOreDic;
        this.result = result;
        this.count = count;
        this.minCount = minCount;
    }

    /**
     * Serialize the deliverable.
     * @param controller the controller.
     * @param input the input.
     * @return the compound.
     */
    public static CompoundNBT serialize(final IFactoryController controller, final Stack input)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_STACK, input.theStack.serializeNBT());
        compound.putBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.putBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.putBoolean(NBT_MATCHOREDIC, input.matchOreDic);
        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.put(NBT_RESULT, input.result.serializeNBT());
        }
        compound.putInt(NBT_COUNT, input.getCount());
        compound.putInt(NBT_MINCOUNT, input.getMinimumCount());

        return compound;
    }

    /**
     * Deserialize the deliverable.
     * @param controller the controller.
     * @param compound the compound.
     * @return the deliverable.
     */
    public static Stack deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final ItemStack result = compound.keySet().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        int count = compound.getInt("size");
        int minCount = count;
        if (compound.keySet().contains(NBT_COUNT))
        {
            count = compound.getInt(NBT_COUNT);
            minCount = compound.getInt(NBT_MINCOUNT);
        }

        return new Stack(stack, matchMeta, matchNBT, matchOreDic, result, count, minCount);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            if (!Collections.disjoint(stack.getItem().getTags(), theStack.getItem().getTags()))
            {
                return true;
            }
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getCount()
    {
        return this.count;
    }

    @Override
    public int getMinimumCount()
    {
        return minCount;
    }

    @NotNull
    public ItemStack getStack()
    {
        return theStack;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new Stack(this.theStack, this.matchMeta, this.matchNBT, this.matchOreDic, this.result, newCount, this.minCount);
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
        if (!(o instanceof Stack))
        {
            return false;
        }

        final Stack stack1 = (Stack) o;

        if (matchMeta != stack1.matchMeta)
        {
            return false;
        }
        if (matchNBT != stack1.matchNBT)
        {
            return false;
        }
        if (matchOreDic != stack1.matchOreDic)
        {
            return false;
        }
        if (!ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack1.getStack()))
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), stack1.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getStack().hashCode();
        result1 = 31 * result1 + (matchMeta ? 1 : 0);
        result1 = 31 * result1 + (matchNBT ? 1 : 0);
        result1 = 31 * result1 + (matchOreDic ? 1 : 0);
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }
}
