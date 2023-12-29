package com.minecolonies.coremod.util;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.interfaces.IDOBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.util.Constants;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for interacting with Domum Ornamentum
 */
public class DomumOrnamentumUtils
{
    /**
     * Extracts the Domum Ornamentum block type from the stack.
     *
     * @param stack the stack to inspect.
     * @return the {@link IMateriallyTexturedBlock}, or null if the stack doesn't have one.
     */
    @Nullable
    public static IMateriallyTexturedBlock getBlock(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem bi &&
                 bi.getBlock() instanceof IMateriallyTexturedBlock doBlock ? doBlock : null;
    }

    /**
     * Extracts the first acceptable Domum Ornamentum stack from the given request.
     *
     * @param request the request to inspect.
     * @return the first acceptable DO stack, or empty if this isn't a request for a DO block.
     */
    @NotNull
    public static ItemStack getRequestedStack(@NotNull final IRequest<?> request)
    {
        if (request.getRequest() instanceof IConcreteDeliverable deliverable)
        {
            for (final ItemStack stack : deliverable.getRequestedItems())
            {
                if (getBlock(stack) != null)
                {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gets the specific texture data from the given DO stack.
     *
     * @param stack the stack to inspect.
     * @return the texture data, or {@link MaterialTextureData#EMPTY} if not a DO stack or otherwise unset.
     * @deprecated inline this method to get current code
     */
    @NotNull
    @Deprecated(forRemoval = true, since = "1.20.1")
    public static MaterialTextureData getTextureData(@NotNull final ItemStack stack)
    {
        return MaterialTextureData.deserializeFromItemStack(stack);
    }

    /**
     * Checks if the given block belongs to DO
     *
     * @param block
     * @return
     */
    public static boolean isDoBlock(final Block block)
    {
        return block instanceof IDOBlock;
    }

    /**
     * Get the texture data from nbt
     *
     * @param nbt
     * @return
     */
    public static MaterialTextureData getTextureDataFromNBT(final CompoundTag nbt)
    {
        if (nbt != null && nbt.contains(Constants.BLOCK_ENTITY_TEXTURE_DATA, Tag.TAG_COMPOUND))
        {
            return MaterialTextureData.deserializeFromNBT(nbt.getCompound(Constants.BLOCK_ENTITY_TEXTURE_DATA));
        }

        return MaterialTextureData.EMPTY;
    }

    private DomumOrnamentumUtils()
    {
        // there are no instances here, only Zuul
    }
}
