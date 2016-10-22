package com.compatibility;

import com.compatibility.tinkers.ToolBrokenCheck;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

public class Compatibility
{
    public static boolean checkMiningCompatibility(@Nullable ItemStack stack, @Nullable String tool)
    {
        if (ToolBrokenCheck.checkTinkersBroken(stack, tool))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
