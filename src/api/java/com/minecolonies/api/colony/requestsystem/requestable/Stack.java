package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

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
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack theStack;

    @NotNull
    private boolean matchMeta = false;

    @NotNull
    private boolean matchNBT = false;

    @NotNull
    private boolean matchOreDic = false;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     */
    public Stack(@NotNull final ItemStack stack)
    {
        this.theStack = stack.copy();

        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        setMatchMeta(true).setMatchNBT(true);
        this.theStack.setCount(Math.min(this.theStack.getCount(), this.theStack.getMaxStackSize()));
    }

    /**
     * Create a Stack deliverable.
     * @param stack the required stack.
     * @param matchMeta if meta has to be matched.
     * @param matchNBT if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result the result stack.
     */
    public Stack(
            @NotNull final ItemStack stack,
            @NotNull final boolean matchMeta,
            @NotNull final boolean matchNBT,
            @NotNull final boolean matchOreDic,
            @NotNull final ItemStack result)
    {
        this.theStack = stack;
        this.matchMeta = matchMeta;
        this.matchNBT = matchNBT;
        this.matchOreDic = matchOreDic;
        this.result = result;
    }

    public Stack setMatchNBT(final boolean match)
    {
        this.matchNBT = match;
        return this;
    }

    public Stack setMatchMeta(final boolean match)
    {
        this.matchMeta = match;
        return this;
    }

    /**
     * Serialize the deliverable.
     * @param controller the controller.
     * @param input the input.
     * @return the compound.
     */
    public static NBTTagCompound serialize(final IFactoryController controller, final Stack input)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_STACK, input.theStack.serializeNBT());
        compound.setBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.setBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.setBoolean(NBT_MATCHOREDIC, input.matchOreDic);

        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.setTag(NBT_RESULT, input.result.serializeNBT());
        }

        return compound;
    }

    /**
     * Deserialize the deliverable.
     * @param controller the controller.
     * @param compound the compound.
     * @return the deliverable.
     */
    public static Stack deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_STACK));
        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Stack(stack, matchMeta, matchNBT, matchOreDic, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            return OreDictionary.itemMatches(getStack(), stack, matchMeta) && getCount() <= stack.getCount();
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getCount()
    {
        return theStack.getCount();
    }

    @NotNull
    public ItemStack getStack()
    {
        return theStack;
    }    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    public Stack setMatchOreDic(final boolean match)
    {
        this.matchOreDic = match;
        return this;
    }    @NotNull
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

