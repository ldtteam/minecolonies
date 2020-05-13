package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.LIST_REQUEST_DISPLAY_STRING;

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
    private static final String TAG_DESCRIPTION = "Desc";
    private static final String NBT_COUNT       = "Count";
    private static final String NBT_MINCOUNT    = "MinCount";

    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * The list of stacks.
     */
    @NotNull
    private final List<ItemStack> theStacks = new ArrayList<>();

    /**
     * If meta should be matched.
     */
    private boolean matchMeta;

    /**
     * If nbt should be matched.
     */
    private boolean matchNBT;

    /**
     * If oredict should be matched.
     */
    private boolean matchOreDic;

    /**
     * Description of the request.
     */
    private final String description;

    /**
     * The result of the request.
     */
    @NotNull
    private ItemStack result;

    /**
     * The required count.
     */
    private int count;

    /**
     * The required count.
     */
    private int minCount;

    /**
     * Create a Stacks deliverable.
     * @param stacks the required stacks.
     * @param description the description.
     * @param count the count.
     */
    public StackList(@NotNull final List<ItemStack> stacks, final String description, final int count)
    {
        this(stacks, description, count, count);
    }

    /**
     * Create a Stacks deliverable.
     * @param stacks the required stacks.
     * @param description the description.
     * @param count the count.
     * @param minCount the min count.
     */
    public StackList(@NotNull final List<ItemStack> stacks, final String description, final int count, final int minCount)
    {
        this(stacks, true, true, false, ItemStackUtils.EMPTY, description, count, minCount);
    }

    /**
     * Create a Stacks deliverable.
     * @param stacks the required stacks.
     * @param matchMeta if meta has to be matched.
     * @param matchNBT if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result the result stack.
     * @param description the description.
     * @param count the count.
     * @param minCount the min count.
     */
    public StackList(
            @NotNull final List<ItemStack> stacks,
            final boolean matchMeta,
            final boolean matchNBT,
            final boolean matchOreDic,
            @NotNull final ItemStack result,
            final String description,
            final int count,
            final int minCount)
    {
        this.description = description;
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
        this.count = count;
        this.minCount = minCount;
    }

    /**
     * Serialize the deliverable.
     * @param controller the controller.
     * @param input the input.
     * @return the compound.
     */
    public static CompoundNBT serialize(final IFactoryController controller, final StackList input)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull final ListNBT neededResTagList = new ListNBT();
        for (@NotNull final ItemStack resource : input.theStacks)
        {
            neededResTagList.add(resource.serializeNBT());
        }
        compound.put(NBT_STACK_LIST, neededResTagList);

        compound.putBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.putBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.putBoolean(NBT_MATCHOREDIC, input.matchOreDic);

        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.put(NBT_RESULT, input.result.serializeNBT());
        }
        compound.putString(TAG_DESCRIPTION, input.description);
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
    public static StackList deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final List<ItemStack> stacks = new ArrayList<>();

        final ListNBT neededResTagList = compound.getList(NBT_STACK_LIST, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.size(); ++i)
        {
            final CompoundNBT neededRes = neededResTagList.getCompound(i);
            stacks.add(ItemStack.read(neededRes));
        }

        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final ItemStack result = compound.keySet().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;
        final String desc = compound.keySet().contains(TAG_DESCRIPTION) ? compound.getString(TAG_DESCRIPTION) : LIST_REQUEST_DISPLAY_STRING;
        int count = stacks.isEmpty() ? 0 : stacks.get(0).getCount();
        int minCount = count;
        if (compound.keySet().contains(NBT_COUNT))
        {
            count = compound.getInt(NBT_COUNT);
            minCount = compound.getInt(NBT_MINCOUNT);
        }

        return new StackList(stacks, matchMeta, matchNBT, matchOreDic, result, desc, count, minCount);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            for (final ItemStack tempStack : theStacks)
            {
                if (!Collections.disjoint(stack.getItem().getTags(), tempStack.getItem().getTags()))
                {
                    return true;
                }
            }
        }

        return ItemStackUtils.compareItemStackListIgnoreStackSize(getStacks(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getMinimumCount()
    {
        return count;
    }

    @Override
    public int getCount()
    {
        return minCount;
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

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new StackList(this.theStacks, this.matchMeta, this.matchNBT, this.matchOreDic, this.result, this.description, newCount, this.minCount);
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

        for (final ItemStack tempStack : stack1.getStacks())
        {
            if (!ItemStackUtils.compareItemStackListIgnoreStackSize(getStacks(), tempStack))
            {
                return false;
            }
        }
        for (final ItemStack tempStack : getStacks())
        {
            if (!ItemStackUtils.compareItemStackListIgnoreStackSize(stack1.getStacks(), tempStack))
            {
                return false;
            }
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

    /**
     * Getter for the display description of the list.
     * @return the description.
     */
    public String getDescription()
    {
        return description;
    }
}
