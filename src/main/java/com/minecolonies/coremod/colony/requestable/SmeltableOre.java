package com.minecolonies.coremod.colony.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
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
 * Smeltable requestable. Delivers a stack of a smeltable ore.
 */
public class SmeltableOre implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(SmeltableOre.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    private ItemStack result;

    public SmeltableOre(final int count)
    {
        this.count = count;
    }

    public SmeltableOre(final int count, final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public static CompoundNBT serialize(final IFactoryController controller, final SmeltableOre ore)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBT_COUNT, ore.count);

        if (!ItemStackUtils.isEmpty(ore.result))
        {
            compound.put(NBT_RESULT, ore.result.serializeNBT());
        }

        return compound;
    }

    public static SmeltableOre deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.keySet().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new SmeltableOre(count, result);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final PacketBuffer buffer, final SmeltableOre input)
    {
        buffer.writeInt(input.getCount());

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
    public static SmeltableOre deserialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        final int count = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? buffer.readItemStack() : ItemStack.EMPTY;

        return new SmeltableOre(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return ItemStackUtils.IS_SMELTABLE.and(itemStack -> IColonyManager.getInstance().getCompatibilityManager().isOre(itemStack)).test(stack);
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new SmeltableOre(newCount);
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
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
