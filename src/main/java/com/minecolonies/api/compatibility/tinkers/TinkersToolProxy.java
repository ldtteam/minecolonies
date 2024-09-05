package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.tools.registry.ToolTypeEntry;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to check if certain tinkers items serve as weapons for the guards.
 */
public class TinkersToolProxy
{
    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    public boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return false;
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     * @param stack the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    public boolean isTinkersTool(@Nullable final ItemStack stack, final ToolTypeEntry toolType)
    {
        return false;
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    public double getAttackDamage(@NotNull final ItemStack stack)
    {
        return 0;
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    public int getToolLevel(@NotNull final ItemStack stack)
    {
        return -1;
    }

    /**
     * Checks to see if STACK is a tinker's tool, and if it is, it checks it's NBT tags to see if it's broken.
     *
     * @param stack the item in question.
     * @return boolean whether the stack is broken or not.
     */
    public boolean checkTinkersBroken(@Nullable final ItemStack stack) { return false; }
}
