package com.compatibility.tinkers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

public class ToolBrokenCheck
{
    private static final String STATS = "Stats";
    private static final String BROKEN = "Broken";
    public static boolean checkTinkersBroken(@Nullable ItemStack stack, @Nullable String tool)
    {
        if (stack.hasTagCompound())
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
