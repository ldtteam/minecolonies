package com.minecolonies.core.util;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.interfaces.IDOBlock;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
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
     * Checks if the given block belongs to DO
     *
     * @param block
     * @return
     */
    public static boolean isDoBlock(final Block block)
    {
        return block instanceof IDOBlock;
    }

    private DomumOrnamentumUtils()
    {
        // there are no instances here, only Zuul
    }
}
