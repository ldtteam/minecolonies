package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.Nullable;

/**
 * This class is to store a check to see if a tinker's tool is broken.
 */
public final class ToolBrokenCheck
{
    private static final String STATS  = "Stats";
    private static final String BROKEN = "Broken";

    /**
     * Checks to see if STACK is a tinker's tool, and if it is, it checks it's NBT tags to see if it's broken.
     *
     * @param stack the item in question.
     * @return boolean whether the stack is broken or not.
     */
    public static boolean checkTinkersBroken(@Nullable final ItemStack stack)
    {
        if (stack.hasTag())
        {
            final CompoundNBT tags = stack.getTag();
            if (tags.contains(STATS))
            {
                final CompoundNBT stats = tags.getCompound(STATS);
                return stats.getBoolean(BROKEN);
            }
        }
        return false;
    }
}