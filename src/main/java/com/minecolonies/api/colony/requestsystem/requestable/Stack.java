package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items.
 */
public class Stack implements IConcreteDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Stack.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK       = "Stack";
    private static final String NBT_MATCHMETA   = "MatchMeta";
    private static final String NBT_MATCHNBT    = "MatchNBT";
    private static final String NBT_MATCHOREDIC  = "MatchOreDic";
    private static final String NBT_BUILDING_RES = "BuildingRes";
    private static final String NBT_RESULT       = "Result";
    private static final String NBT_COUNT       = "Count";
    private static final String NBT_MINCOUNT    = "MinCount";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack theStack;

    /**
     * If damage should match.
     */
    private boolean matchDamage;

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
     * If this request can be resolved by building.
     */
    private boolean canBeResolvedByBuilding;

    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     */
    public Stack(@NotNull final ItemStack stack)
    {
        this(stack, true);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     * @param matchDurability whether or not to match Durability
     */
    public Stack(@NotNull final ItemStack stack, boolean matchDurability)
    {
        this(stack, matchDurability, true, false, ItemStackUtils.EMPTY, Math.min(stack.getCount(), stack.getMaxStackSize()), Math.min(stack.getCount(), stack.getMaxStackSize()));
    }


    /**
     * Create a Stack deliverable.
     *
     * @param stack    the required stack.
     * @param count    the count.
     * @param minCount the min count.
     */
    public Stack(@NotNull final ItemStack stack, final int count, final int minCount)
    {
        this(stack, count, minCount, true);
    }

    /**
     * Transform an itemStorage into this predicate.
     *
     * @param itemStorage the storage to use.
     */
    public Stack(@NotNull final ItemStorage itemStorage)
    {
        this(itemStorage.getItemStack(), !itemStorage.ignoreDamageValue(), !itemStorage.ignoreNBT(), false, ItemStackUtils.EMPTY, itemStorage.getAmount(), itemStorage.getAmount());
    }

    /**
     * Create a Stack deliverable with variable nbt.
     * @param stack the stack to deliver.
     * @param count the count.
     * @param minCount the min count.
     * @param matchNBT if nbt has to match.
     */
    public Stack(@NotNull final ItemStack stack, final int count, final int minCount, final boolean matchNBT)
    {
        this(stack, true, matchNBT, false, ItemStackUtils.EMPTY, count, minCount);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack       the required stack.
     * @param matchDamage   if damage has to be matched.
     * @param matchNBT    if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result      the result stack.
     * @param count       the count.
     * @param minCount    the min count.
     */
    public Stack(
      @NotNull final ItemStack stack,
      final boolean matchDamage,
      final boolean matchNBT,
      final boolean matchOreDic,
      @NotNull final ItemStack result,
      final int count,
      final int minCount)
    {
        this(stack, matchDamage, matchNBT, matchOreDic, ItemStackUtils.EMPTY, count, minCount, true);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack       the required stack.
     * @param matchDamage   if damage has to be matched.
     * @param matchNBT    if NBT has to be matched.
     * @param matchOreDic if the oredict has to be matched.
     * @param result      the result stack.
     * @param count       the count.
     * @param minCount    the min count.
     * @param canBeResolvedByBuilding if can be resolved by building.
     */
    public Stack(final ItemStack stack, final boolean matchDamage, final boolean matchNBT, final boolean matchOreDic, final ItemStack result, final int count, final int minCount, final boolean canBeResolvedByBuilding)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        this.theStack = stack.copy();
        this.matchDamage = matchDamage;
        this.matchNBT = matchNBT;
        this.matchOreDic = matchOreDic;
        this.result = result;
        this.count = count;
        this.minCount = Math.min(minCount, count);
        this.canBeResolvedByBuilding = canBeResolvedByBuilding;
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final Stack input)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_STACK, input.theStack.serializeNBT());
        compound.putBoolean(NBT_MATCHMETA, input.matchDamage);
        compound.putBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.putBoolean(NBT_MATCHOREDIC, input.matchOreDic);
        compound.putBoolean(NBT_BUILDING_RES, input.canBeResolvedByBuilding);

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
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static Stack deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        final boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        final boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        final boolean canBeResolved = compound.contains(NBT_BUILDING_RES) ? compound.getBoolean(NBT_BUILDING_RES) : true;

        final ItemStack result = compound.contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        int count = compound.getInt("size");
        int minCount = count;
        if (compound.contains(NBT_COUNT))
        {
            count = compound.getInt(NBT_COUNT);
            minCount = compound.getInt(NBT_MINCOUNT);
        }

        return new Stack(stack, matchMeta, matchNBT, matchOreDic, result, count, minCount, canBeResolved);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Stack input)
    {
        buffer.writeItem(input.theStack);
        buffer.writeBoolean(input.matchDamage);
        buffer.writeBoolean(input.matchNBT);
        buffer.writeBoolean(input.matchOreDic);
        buffer.writeBoolean(input.canBeResolvedByBuilding);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItem(input.result);
        }
        buffer.writeInt(input.getCount());
        buffer.writeInt(input.getMinimumCount());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Stack deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final ItemStack stack = buffer.readItem();
        final boolean matchMeta = buffer.readBoolean();
        final boolean matchNBT = buffer.readBoolean();
        final boolean matchOreDic = buffer.readBoolean();
        final boolean canBeResolved = buffer.readBoolean();

        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;

        int count = buffer.readInt();
        int minCount = buffer.readInt();
        return new Stack(stack, matchMeta, matchNBT, matchOreDic, result, count, minCount, canBeResolved);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            if (!Collections.disjoint(stack.getTags().toList(), theStack.getTags().toList()))
            {
                return true;
            }
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack, matchDamage, matchNBT);
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

    /**
     * Check if nbt should be matched.
     * @return true if so.
     */
    public boolean matchNBT()
    {
        return matchNBT;
    }

    /**
     * Check if damage should be matched.
     * @return true if so.
     */
    public boolean matchDamage()
    {
        return matchDamage;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new Stack(this.theStack, this.matchDamage, this.matchNBT, this.matchOreDic, this.result, newCount, this.minCount);
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

        if (matchDamage != stack1.matchDamage)
        {
            return false;
        }
        if (canBeResolvedByBuilding != stack1.canBeResolvedByBuilding)
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
        result1 = 31 * result1 + (matchDamage ? 1 : 0);
        result1 = 31 * result1 + (matchNBT ? 1 : 0);
        result1 = 31 * result1 + (matchOreDic ? 1 : 0);
        result1 = 31 * result1 + (canBeResolvedByBuilding ? 1 : 0);
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }

    @Override
    public boolean canBeResolvedByBuilding()
    {
        return canBeResolvedByBuilding;
    }

    public void setCanBeResolvedByBuilding(final boolean canBeResolvedByBuilding)
    {
        this.canBeResolvedByBuilding = canBeResolvedByBuilding;
    }

    @Override
    public List<ItemStack> getRequestedItems()
    {
        return Lists.newArrayList(theStack);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
