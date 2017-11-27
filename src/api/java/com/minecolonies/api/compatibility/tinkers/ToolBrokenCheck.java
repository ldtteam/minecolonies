package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        if (!ItemStackUtils.isEmpty(stack) && stack.hasTagCompound())
        {
            final NBTTagCompound tags = stack.getTagCompound();
            if (tags.hasKey(STATS))
            {
                final NBTTagCompound stats = tags.getCompoundTag(STATS);
                if (stats.getBoolean(BROKEN))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
