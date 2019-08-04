package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
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

    /**
     * If meta should match.
     */
    private boolean matchMeta = false;

    /**
     * If NBT should match.
     */
    private boolean matchNBT = false;

    /**
     * If oredict should match.
     */
    private boolean matchOreDic = false;

    /**
     * The required count.
     */
    private int count = 0;

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
        this.count = (Math.min(this.theStack.getCount(), this.theStack.getMaxStackSize()));
    }

    /**
     * Transform an itemStorage into this predicate.
     * @param itemStorage the storage to use.
     */
    public Stack (@NotNull final ItemStorage itemStorage)
    {
        this(itemStorage.getItemStack(), !itemStorage.ignoreDamageValue(), false, false, ItemStackUtils.EMPTY);
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

    /**
     * Set if NBT has to match
     * @param match true if so.
     * @return an instance of this.
     */
    public Stack setMatchNBT(final boolean match)
    {
        this.matchNBT = match;
        return this;
    }

    /**
     * Set if meta has to match
     * @param match true if so.
     * @return an instance of this.
     */
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
    public static CompoundNBT serialize(final IFactoryController controller, final Stack input)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_STACK, input.theStack.serializeNBT());
        compound.putBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.putBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.putBoolean(NBT_MATCHOREDIC, input.matchOreDic);
        compound.putInt("size", input.count);
        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.put(NBT_RESULT, input.result.serializeNBT());
        }

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
        final int size = compound.getInt("size");
        final Stack theStack = new Stack(stack, matchMeta, matchNBT, matchOreDic, result);
        theStack.setCount(size);
        return theStack;
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            for (final ResourceLocation tag: getStack().getItem().getTags())
            {
                final Tag<Item> theTag = new Tag<>(tag);
                if (theTag.contains(stack.getItem()));
                {
                    return true;
                }
            }
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getCount()
    {
        return this.count;
    }

    @NotNull
    public ItemStack getStack()
    {
        return theStack;
    }

    /**
     * Set the count of the stack.
     * @param count the count to set.
     */
    public void setCount(final int count)
    {
        this.count = count;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    public Stack setMatchOreDic(final boolean match)
    {
        this.matchOreDic = match;
        return this;
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

