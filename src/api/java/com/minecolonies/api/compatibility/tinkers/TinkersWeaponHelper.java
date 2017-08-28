package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
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
        return !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof SwordCore;
    }

    /**
     * Calculate the tool level of the stack.
     * @param stack the stack.
     * @return the tool level
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public int getToolLevel(@NotNull final ItemStack stack)
    {
        return ToolHelper.getHarvestLevelStat(stack);
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
    public static boolean isTinkersSword(@NotNull final ItemStack stack)
    {
        return new TinkersWeaponHelper().isTinkersWeapon(stack);
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getDamage(@NotNull final ItemStack stack)
    {
        return new TinkersWeaponHelper().getAttackDamage(stack);
    }

    /**
     * Calculate the tool level of the stack.
     * @param stack the stack.
     * @return the tool level
     */
    public static int getToolLvl(@NotNull final ItemStack stack)
    {
        return new TinkersWeaponHelper().getToolLevel(stack);
    }
}
