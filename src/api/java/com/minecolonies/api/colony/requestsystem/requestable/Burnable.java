package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Burnable requestable. Delivers a stack of burnable fuel.
 */
public class Burnable implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>> TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Burnable.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Burnable(final int count) {this.count = count;}

    public Burnable(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param burnable   the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final Burnable burnable)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(NBT_COUNT, burnable.count);

        if (!ItemStackUtils.isEmpty(burnable.result))
        {
            compound.put(NBT_RESULT, burnable.result.serializeNBT());
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
    public static Burnable deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Burnable(count, result);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Burnable input)
    {
        buffer.writeInt(input.count);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItem(input.result);
        }
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Burnable deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final int count = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;

        return new Burnable(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return FurnaceBlockEntity.isFuel(stack);
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new Burnable(newCount);
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
        if (!(o instanceof Burnable))
        {
            return false;
        }

        final Burnable burnable = (Burnable) o;

        if (getCount() != burnable.getCount())
        {
            return false;
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), burnable.getResult());
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
