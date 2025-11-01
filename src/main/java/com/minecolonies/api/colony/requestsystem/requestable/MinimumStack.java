package com.minecolonies.api.colony.requestsystem.requestable;

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

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Minimum stack request type.
 */
public class MinimumStack extends Stack
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(MinimumStack.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     */
    public MinimumStack(@NotNull final ItemStack stack)
    {
        this(stack, true);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     * @param matchDurability whether or not to match Durability
     */
    public MinimumStack(@NotNull final ItemStack stack, boolean matchDurability)
    {
        this(stack, matchDurability, true, ItemStackUtils.EMPTY, Math.min(stack.getCount(), stack.getMaxStackSize()), Math.min(stack.getCount(), stack.getMaxStackSize()));
    }


    /**
     * Create a Stack deliverable.
     *
     * @param stack    the required stack.
     * @param count    the count.
     * @param minCount the min count.
     */
    public MinimumStack(@NotNull final ItemStack stack, final int count, final int minCount)
    {
        this(stack, count, minCount, true);
    }

    /**
     * Transform an itemStorage into this predicate.
     *
     * @param itemStorage the storage to use.
     */
    public MinimumStack(@NotNull final ItemStorage itemStorage)
    {
        this(itemStorage.getItemStack(), !itemStorage.ignoreDamageValue(), !itemStorage.ignoreNBT(), ItemStackUtils.EMPTY, itemStorage.getAmount(), itemStorage.getAmount());
    }

    /**
     * Create a Stack deliverable with variable nbt.
     * @param stack the stack to deliver.
     * @param count the count.
     * @param minCount the min count.
     * @param matchNBT if nbt has to match.
     */
    public MinimumStack(@NotNull final ItemStack stack, final int count, final int minCount, final boolean matchNBT)
    {
        this(stack, true, matchNBT, ItemStackUtils.EMPTY, count, minCount);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack       the required stack.
     * @param matchDamage   if damage has to be matched.
     * @param matchNBT    if NBT has to be matched.
     * @param result      the result stack.
     * @param count       the count.
     * @param minCount    the min count.
     */
    public MinimumStack(
      @NotNull final ItemStack stack,
      final boolean matchDamage,
      final boolean matchNBT,
      @NotNull final ItemStack result,
      final int count,
      final int minCount)
    {
        this(stack, matchDamage, matchNBT, ItemStackUtils.EMPTY, count, minCount, true);
    }

    /**
     * Create a Stack deliverable.
     *
     * @param stack       the required stack.
     * @param matchDamage   if damage has to be matched.
     * @param matchNBT    if NBT has to be matched.
     * @param result      the result stack.
     * @param count       the count.
     * @param minCount    the min count.
     * @param canBeResolvedByBuilding if can be resolved by building.
     */
    public MinimumStack(final ItemStack stack, final boolean matchDamage, final boolean matchNBT, final ItemStack result, final int count, final int minCount, final boolean canBeResolvedByBuilding)
    {
        super(stack, matchDamage, matchNBT, result, count, minCount, canBeResolvedByBuilding);
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static MinimumStack deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final Stack stack = Stack.deserialize(controller, compound);
        return new MinimumStack(stack.getStack(), stack.matchDamage(), stack.matchNBT(), stack.getResult(), stack.getCount(), stack.getMinimumCount(), stack.canBeResolvedByBuilding());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static MinimumStack deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final Stack stack = Stack.deserialize(controller, buffer);
        return new MinimumStack(stack.getStack(), stack.matchDamage(), stack.matchNBT(), stack.getResult(), stack.getCount(), stack.getMinimumCount(), stack.canBeResolvedByBuilding());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof MinimumStack))
        {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
