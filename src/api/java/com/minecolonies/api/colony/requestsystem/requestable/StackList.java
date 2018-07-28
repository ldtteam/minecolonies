package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items matching one of a list of stacks.
 */
public class StackList implements IDeliverable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK_LIST  = "StackList";
    private static final String NBT_MATCHMETA   = "MatchMeta";
    private static final String NBT_MATCHNBT    = "MatchNBT";
    private static final String NBT_MATCHOREDIC = "MatchOreDic";
    private static final String NBT_RESULT      = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final List<ItemStack> theStacks = new ArrayList<>();

    @NotNull
    private boolean matchMeta = false;

    @NotNull
    private boolean matchNBT = false;

    @NotNull
    private boolean matchOreDic = false;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    /**
     * Create a Stacks deliverable.
     * @param stacks the required stacks.
     */
    public StackList(@NotNull final List<ItemStack> stacks)
    {
        for (final ItemStack stack : stacks)
        {
            final ItemStack tempStack = stack.copy();
            tempStack.setCount(Math.min(tempStack.getCount(), tempStack.getMaxStackSize()));

            this.theStacks.add(tempStack);
        }

        if (stacks.isEmpty())
        {
            throw new IllegalArgumentException("Cannot deliver Empty List.");
        }

        setMatchMeta(true).setMatchNBT(true);
    }

    /**
     * Create a Stacks deliverable.
     * @param stacks the required stacks.
     * @param matchMeta if meta has to be matched.
     * @param matchNBT if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result the result stack.
     */
    public StackList(
            @NotNull final List<ItemStack> stacks,
            final boolean matchMeta,
            final boolean matchNBT,
            final boolean matchOreDic,
            @NotNull final ItemStack result)
    {
        for (final ItemStack stack : stacks)
        {
            final ItemStack tempStack = stack.copy();
            tempStack.setCount(Math.min(tempStack.getCount(), tempStack.getMaxStackSize()));

            this.theStacks.add(tempStack);
        }

        this.matchMeta = matchMeta;
        this.matchNBT = matchNBT;
        this.matchOreDic = matchOreDic;
        this.result = result;
    }

    public StackList setMatchNBT(final boolean match)
    {
        this.matchNBT = match;
        return this;
    }

    public StackList setMatchMeta(final boolean match)
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
    public static NBTTagCompound serialize(final IFactoryController controller, final StackList input)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        @NotNull final NBTTagList neededResTagList = new NBTTagList();
        for (@NotNull final ItemStack resource : input.theStacks)
        {
            neededResTagList.appendTag(resource.serializeNBT());
        }
        compound.setTag(NBT_STACK_LIST, neededResTagList);

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
    public static StackList deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final List<ItemStack> stacks = new ArrayList<>();

        final NBTTagList neededResTagList = compound.getTagList(NBT_STACK_LIST, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.tagCount(); ++i)
        {
            final NBTTagCompound neededRes = neededResTagList.getCompoundTagAt(i);
            stacks.add(new ItemStack(neededRes));
        }

        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new StackList(stacks, matchMeta, matchNBT, matchOreDic, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            for (final ItemStack tempStack : theStacks)
            {
                if (OreDictionary.itemMatches(tempStack, stack, matchMeta))
                {
                    return true;
                }
            }
        }

        return ItemStackUtils.compareItemStackListIgnoreStackSize(getStacks(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getCount()
    {
        return theStacks.isEmpty() ? 0 : theStacks.get(0).getCount();
    }

    @NotNull
    public List<ItemStack> getStacks()
    {
        return theStacks;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    public StackList setMatchOreDic(final boolean match)
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
        if (!(o instanceof StackList))
        {
            return false;
        }

        final StackList stack1 = (StackList) o;

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
        if (!ItemStackUtils.compareItemStackListIgnoreStackSize(getStacks(), stack1.getStack()))
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), stack1.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getStacks().hashCode();
        result1 = 31 * result1 + (matchMeta ? 1 : 0);
        result1 = 31 * result1 + (matchNBT ? 1 : 0);
        result1 = 31 * result1 + (matchOreDic ? 1 : 0);
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }
}

