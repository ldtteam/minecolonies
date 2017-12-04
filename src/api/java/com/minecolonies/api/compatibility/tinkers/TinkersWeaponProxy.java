package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class to check if certain tinkers items serve as weapons for the guards.
 */
public class TinkersWeaponProxy
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
}
