package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.item.SwordCore;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Class to check if certain tinkers items serve as weapons for the guards.
 */
public final class TinkersToolHelper extends TinkersToolProxy
{
    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    public static boolean isTinkersSword(@NotNull final ItemStack stack)
    {
        return new TinkersToolHelper().isTinkersWeapon(stack);
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    @Override
    public boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof SwordCore;
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    @Override
    public double getAttackDamage(@NotNull final ItemStack stack)
    {
        return ToolAttackUtil.getActualDamage(ToolStack.copyFrom(stack), null);
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    @Override
    public int getToolLevel(@NotNull final ItemStack stack)
    {
        if (checkTinkersBroken(stack))
        {
            return -1;
        }
        return (ToolStack.copyFrom(stack).getStats().getHarvestLevel());
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getDamage(@NotNull final ItemStack stack)
    {
        return new TinkersToolHelper().getAttackDamage(stack);
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    public static int getToolLvl(@NotNull final ItemStack stack)
    {
        return new TinkersToolHelper().getToolLevel(stack);
    }

    /**
     * Checks to see if STACK is a tinker's tool, and if it is, it checks it's NBT tags to see if it's broken.
     *
     * @param stack the item in question.
     * @return boolean whether the stack is broken or not.
     */
    @Override
    public boolean checkTinkersBroken(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && ToolStack.copyFrom(stack).isBroken();
    }
}