package com.minecolonies.compatibility.tinkers;

import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

/**
 * Class to check if certain tinkers items serve as weapons for the guards.
 */
public final class TinkersWeaponHelper
{
    private TinkersWeaponHelper()
    {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     * @param stack the stack to check for.
     * @return true if so.
     */
    public static boolean isTinkersWeapon(final ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof SwordCore;
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getAttackDamage(final ItemStack stack)
    {
        return ToolHelper.getActualAttack(stack);
    }
}
