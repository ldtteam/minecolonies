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

public class PublicCrafting extends AbstractCrafting
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>> TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(PublicCrafting.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     * @param count the crafting count.
     */
    public PublicCrafting(@NotNull final ItemStack stack, final int count)
    {
        super(stack, count, count);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundNBT serialize(final IFactoryController controller, final PublicCrafting input)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_STACK, input.getStack().serializeNBT());
        compound.putInt(NBT_COUNT, input.getCount());

        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static PublicCrafting deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final int count = compound.getInt(NBT_COUNT);

        return new PublicCrafting(stack, count);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final PacketBuffer buffer, final PublicCrafting input)
    {
        buffer.writeItemStack(input.getStack());
        buffer.writeInt(input.getCount());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static PublicCrafting deserialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        final ItemStack stack = buffer.readItemStack();
        final int count = buffer.readInt();

        return new PublicCrafting(stack, count);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
