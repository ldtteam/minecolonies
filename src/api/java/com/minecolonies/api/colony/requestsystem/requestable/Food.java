package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Eatable requestable. Delivers a stack of food.
 */
public class Food implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Food.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Food(final int count) {this.count = count;}

    public Food(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param food       the input.
     * @return the compound.
     */
    public static CompoundNBT serialize(final IFactoryController controller, final Food food)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.put(NBT_RESULT, food.result.serializeNBT());
        }

        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static Food deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.keySet().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Food(count, result);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final PacketBuffer buffer, final Food input)
    {
        buffer.writeInt(input.count);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItemStack(input.result);
        }
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Food deserialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        final int count = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? buffer.readItemStack() : ItemStack.EMPTY;

        return new Food(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return stack.getItem().isFood();
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(@NotNull final int newCount)
    {
        return new Food(newCount);
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public int getMinimumCount()
    {
        return 1;
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
        if (!(o instanceof Food))
        {
            return false;
        }

        final Food food = (Food) o;

        if (getCount() != food.getCount())
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), food.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getCount();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
