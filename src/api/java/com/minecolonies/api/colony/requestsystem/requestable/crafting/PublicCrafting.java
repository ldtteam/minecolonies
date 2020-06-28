package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

public class PublicCrafting extends AbstractCrafting
{
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
}
