package com.minecolonies.api.colony.requestsystem.requestable.crafting;

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

public class PrivateCrafting extends AbstractCrafting
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(PrivateCrafting.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    /**
     * Create a Stack deliverable.
     *
     * @param stack    the required stack.
     * @param count    the crafting count.
     * @param minCount the min count.
     */
    public PrivateCrafting(@NotNull final ItemStack stack, final int count, final int minCount)
    {
        super(stack, count, minCount);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundNBT serialize(final IFactoryController controller, final PrivateCrafting input)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_STACK, input.getStack().serializeNBT());
        compound.putInt(NBT_COUNT, input.getCount());
        compound.putInt(NBT_MIN_COUNT, input.getMinCount());

        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static PrivateCrafting deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final int count = compound.getInt(NBT_COUNT);
        final int minCount = compound.getInt(NBT_MIN_COUNT);

        return new PrivateCrafting(stack, count, minCount == 0 ? count : minCount);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final PacketBuffer buffer, final PrivateCrafting input)
    {
        buffer.writeItemStack(input.getStack());
        buffer.writeInt(input.getCount());
        buffer.writeInt(input.getMinCount());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static PrivateCrafting deserialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        final ItemStack stack = buffer.readItemStack();
        final int count = buffer.readInt();
        final int minCount = buffer.readInt();

        return new PrivateCrafting(stack, count, minCount == 0 ? count : minCount);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
