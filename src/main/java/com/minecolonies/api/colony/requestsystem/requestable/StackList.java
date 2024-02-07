package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_SYSTEM_STACK_LIST;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items matching one of a list of stacks.
 */
public class StackList implements IConcreteDeliverable, INonExhaustiveDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>> TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(StackList.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK_LIST  = "StackList";
    private static final String NBT_MATCHMETA   = "MatchMeta";
    private static final String NBT_MATCHNBT    = "MatchNBT";
    private static final String NBT_MATCHOREDIC = "MatchOreDic";
    private static final String NBT_RESULT      = "Result";
    private static final String TAG_DESCRIPTION = "Desc";
    private static final String NBT_COUNT       = "Count";
    private static final String NBT_MINCOUNT    = "MinCount";
    private static final String NBT_LEFTOVER    = "leftover";

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
     * Count to leave behind in warehouse.
     */
    private int leftOver;

    /**
     * Create a Stacks deliverable.
     *
     * @param stacks      the required stacks.
     * @param description the description.
     * @param count       the count.
     */
    public StackList(@NotNull final List<ItemStack> stacks, final String description, final int count)
    {
        this(stacks, description, count, count);
    }

    /**
     * Create a Stacks deliverable.
     *
     * @param stacks      the required stacks.
     * @param description the description.
     * @param count       the count.
     * @param minCount    the min count.
     */
    public StackList(@NotNull final List<ItemStack> stacks, final String description, final int count, final int minCount)
    {
        this(stacks, true, true, false, ItemStackUtils.EMPTY, description, count, minCount, 0);
    }

    /**
     * Create a Stacks deliverable.
     *
     * @param stacks      the required stacks.
     * @param description the description.
     * @param count       the count.
     * @param minCount    the min count.
     * @param leftOver    the amount to be left over.
     */
    public StackList(@NotNull final List<ItemStack> stacks, final String description, final int count, final int minCount, final int leftOver)
    {
        this(stacks, true, true, false, ItemStackUtils.EMPTY, description, count, minCount, leftOver);
    }

    /**
     * Create a Stacks deliverable.
     *
     * @param stacks      the required stacks.
     * @param matchMeta   if meta has to be matched.
     * @param matchNBT    if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result      the result stack.
     * @param description the description.
     * @param count       the count.
     * @param minCount    the min count.
     * @param leftOver    the left over amount.
     */
    public StackList(
      @NotNull final List<ItemStack> stacks,
      final boolean matchMeta,
      final boolean matchNBT,
      final boolean matchOreDic,
      @NotNull final ItemStack result,
      final String description,
      final int count,
      final int minCount,
      final int leftOver)
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
        this.leftOver = leftOver;
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final StackList input)
    {
        final CompoundTag compound = new CompoundTag();
        @NotNull final ListTag neededResTagList = new ListTag();
        for (@NotNull final ItemStack resource : input.theStacks)
        {
            neededResTagList.add(resource.save(new CompoundTag()));
        }
        compound.put(NBT_STACK_LIST, neededResTagList);

        compound.putBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.putBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.putBoolean(NBT_MATCHOREDIC, input.matchOreDic);

        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.put(NBT_RESULT, input.result.save(new CompoundTag()));
        }
        compound.putString(TAG_DESCRIPTION, input.description);
        compound.putInt(NBT_COUNT, input.getCount());
        compound.putInt(NBT_MINCOUNT, input.getMinimumCount());
        compound.putInt(NBT_LEFTOVER, input.getLeftOver());

        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static StackList deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final List<ItemStack> stacks = new ArrayList<>();

        final ListTag neededResTagList = compound.getList(NBT_STACK_LIST, Tag.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.size(); ++i)
        {
            final CompoundTag neededRes = neededResTagList.getCompound(i);
            stacks.add(ItemStack.of(neededRes));
        }

        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final ItemStack result = compound.contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;
        final String desc = compound.contains(TAG_DESCRIPTION) ? compound.getString(TAG_DESCRIPTION) : REQUEST_SYSTEM_STACK_LIST;
        int count = stacks.isEmpty() ? 0 : stacks.get(0).getCount();
        int minCount = count;
        if (compound.contains(NBT_COUNT))
        {
            count = compound.getInt(NBT_COUNT);
            minCount = compound.getInt(NBT_MINCOUNT);
        }
        int leftOver = compound.getInt(NBT_LEFTOVER);

        return new StackList(stacks, matchMeta, matchNBT, matchOreDic, result, desc, count, minCount, leftOver);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final StackList input)
    {
        buffer.writeInt(input.theStacks.size());
        input.theStacks.forEach(res -> buffer.writeItem(res));

        buffer.writeBoolean(input.matchMeta);
        buffer.writeBoolean(input.matchNBT);
        buffer.writeBoolean(input.matchOreDic);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItem(input.result);
        }
        buffer.writeUtf(input.description);
        buffer.writeInt(input.getCount());
        buffer.writeInt(input.getMinimumCount());
        buffer.writeInt(input.getLeftOver());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static StackList deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final List<ItemStack> stacks = new ArrayList<>();

        final int stacksSize = buffer.readInt();
        for (int i = 0; i < stacksSize; ++i)
        {
            stacks.add(buffer.readItem());
        }

        final boolean matchMeta = buffer.readBoolean();
        final boolean matchNBT = buffer.readBoolean();
        final boolean matchOreDic = buffer.readBoolean();
        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;
        final String desc = buffer.readUtf(32767);
        int count = buffer.readInt();
        int minCount = buffer.readInt();
        int leftOver = buffer.readInt();

        return new StackList(stacks, matchMeta, matchNBT, matchOreDic, result, desc, count, minCount, leftOver);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            for (final ItemStack tempStack : theStacks)
            {
                if (!Collections.disjoint(stack.getTags().toList(), tempStack.getTags().toList()))
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
        return minCount;
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public int getLeftOver()
    {
        return leftOver;
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
        return new StackList(this.theStacks, this.matchMeta, this.matchNBT, this.matchOreDic, this.result, this.description, newCount, this.minCount, this.leftOver);
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
     *
     * @return the description.
     */
    public String getDescription()
    {
        return description;
    }

    @Override
    public List<ItemStack> getRequestedItems() 
    {
        return theStacks;
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
