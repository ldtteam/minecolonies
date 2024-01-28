package com.minecolonies.api.colony.requestsystem.requestable.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
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
    public PublicCrafting(@NotNull final ItemStack stack, final int count, final IToken<?> recipeToken)
    {
        super(stack, count, count, recipeToken);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final PublicCrafting input)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_STACK, input.getStack().serializeNBT());
        compound.putInt(NBT_COUNT, input.getCount());
        final CompoundTag tokenCompound = StandardFactoryController.getInstance().serialize(input.getRecipeID());
        compound.put(NBT_TOKEN, tokenCompound);
        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static PublicCrafting deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final int count = compound.getInt(NBT_COUNT);
        IToken<?> token = null;
        if (compound.contains(NBT_TOKEN))
        {
            token = StandardFactoryController.getInstance().deserialize(compound.getCompound(NBT_TOKEN));
        }
        return new PublicCrafting(stack, count, token);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final PublicCrafting input)
    {
        buffer.writeItem(input.getStack());
        buffer.writeInt(input.getCount());
        StandardFactoryController.getInstance().serialize(buffer, input.getRecipeID());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static PublicCrafting deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final ItemStack stack = buffer.readItem();
        final int count = buffer.readInt();
        final IToken<?> token = StandardFactoryController.getInstance().deserialize(buffer);

        return new PublicCrafting(stack, count, token);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
