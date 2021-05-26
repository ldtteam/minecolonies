package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * This class is to store a check to see if a tinker's tool is broken.
 */
public final class ToolBrokenCheck
{

    /**
     * Checks to see if STACK is a tinker's tool, and if it is, it checks it's NBT tags to see if it's broken.
     *
     * @param stack the item in question.
     * @return boolean whether the stack is broken or not.
     */
    public static boolean checkTinkersBroken(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && ToolStack.copyFrom(stack).isBroken();
    }
}