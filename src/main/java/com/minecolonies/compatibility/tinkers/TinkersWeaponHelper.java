package com.minecolonies.compatibility.tinkers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

/**
 * Class to check if certain tinkers items serve as weapons for the guards.
 */
public final class TinkersWeaponHelper extends TinkersWeaponProxy
{
    /**
     * Check if a certain itemstack is a tinkers weapon.
     * @param stack the stack to check for.
     * @return true if so.
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof SwordCore;
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     * @param stack the stack.
     * @return the attack damage.
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public double getAttackDamage(@NotNull final ItemStack stack)
    {
        return ToolHelper.getActualAttack(stack);
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     * @param stack the stack to check for.
     * @return true if so.
     */
    @Optional.Method(modid = "tconstruct")
    public static boolean isTinkersSword(@NotNull final ItemStack stack)
    {
        return new TinkersWeaponHelper().isTinkersWeapon(stack);
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     * @param stack the stack.
     * @return the attack damage.
     */
    @Optional.Method(modid = "tconstruct")
    public static double getDamage(@NotNull final ItemStack stack)
    {
        return new TinkersWeaponHelper().getAttackDamage(stack);
    }
}
