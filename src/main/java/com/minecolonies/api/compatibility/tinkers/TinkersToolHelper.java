package com.minecolonies.api.compatibility.tinkers;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/*import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;*/

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
        return !ItemStackUtils.isEmpty(stack) && false;//stack.is(TinkerTags.Items.SWORD);
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
        return -1;//ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE);
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
        return -1;//(ToolStack.from(stack).getStats().get(ToolStats.HARVEST_TIER).getLevel());
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
        return !ItemStackUtils.isEmpty(stack) && false;//ToolDamageUtil.isBroken(stack);
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     * @param stack the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    @Override
    public boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType)
    {
        return false;
        /*
        if (ItemStackUtils.isEmpty(stack) || !(stack.getItem() instanceof ModifiableItem))
        {
            return false;
        }

        if (ToolType.AXE.equals(toolType) && stack.canPerformAction(ToolActions.AXE_DIG))
        {
            return true;
        }

        if (ToolType.SHOVEL.equals(toolType) && stack.canPerformAction(ToolActions.SHOVEL_DIG))
        {
            return true;
        }

        if (ToolType.PICKAXE.equals(toolType) && stack.canPerformAction(ToolActions.PICKAXE_DIG))
        {
            return true;
        }

        if (ToolType.HOE.equals(toolType))
        {
            return stack.canPerformAction(ToolActions.HOE_DIG);
        }

        return stack.is(TinkerTags.Items.HARVEST);*/
    }
}
