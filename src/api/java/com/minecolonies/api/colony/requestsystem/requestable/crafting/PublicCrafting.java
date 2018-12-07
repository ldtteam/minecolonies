package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class PublicCrafting extends AbstractCrafting
{
    /**
     * Create a Stack deliverable.
     *
     * @param stack the required stack.
     */
    public PublicCrafting(@NotNull final ItemStack stack)
    {
        super(stack);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static NBTTagCompound serialize(final IFactoryController controller, final PublicCrafting input)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_STACK, input.getStack().serializeNBT());

        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static PublicCrafting deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_STACK));

        return new PublicCrafting(stack);
    }
}
