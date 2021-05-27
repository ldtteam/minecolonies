package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
<<<<<<< HEAD
=======
import net.minecraft.inventory.Inventory;
>>>>>>> 2c474acf7870c10226699c1b31243f024c0fcf79
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;
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
        if (ModList.get().isLoaded("tconstruct"))
        {
            return !ItemStackUtils.isEmpty(stack) && ToolStack.copyFrom(stack).isBroken();
        }
        else
        {
            return false;
        }
    }
}