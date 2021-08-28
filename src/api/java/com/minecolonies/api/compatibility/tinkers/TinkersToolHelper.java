package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return false;
        //return !ItemStackUtils.isEmpty(stack) && stack.getItem().is(TinkerTags.Items.SWORD);
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
        return 0;
        //return ToolStack.from(stack).getStats().getFloat(ToolStats.ATTACK_DAMAGE);
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
        return -1;
        //return (ToolStack.from(stack).getStats().getInt(ToolStats.HARVEST_LEVEL));
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
        return false;
        //return !ItemStackUtils.isEmpty(stack) && ToolDamageUtil.isBroken(stack);
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     * @param stack the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    @Override
    public boolean isTinkersTool(@Nullable final ItemStack stack, IToolType toolType)
    {
        return false;


        /*if (ItemStackUtils.isEmpty(stack) || !stack.getToolTypes().contains(ToolType.get(toolType.getName())))
        {
            return false;
        }
        else
        {
            return stack.getItem().is(TinkerTags.Items.HARVEST);
        }*/
    }
}
