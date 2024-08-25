package com.minecolonies.core.items;

import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.items.component.BuildingId;
import com.minecolonies.api.items.component.PatrolTarget;
import com.minecolonies.api.items.component.ModDataComponents;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterGuard extends AbstractItemMinecolonies
{
    /**
     * GuardScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterGuard(final Item.Properties properties)
    {
        super("scepterguard", properties.stacksTo(1).durability(2).component(ModDataComponents.PATROL_TARGET, PatrolTarget.EMPTY));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext ctx)
    {
        // if server world, do nothing
        if (ctx.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack scepter = ctx.getPlayer().getItemInHand(ctx.getHand());
        final PatrolTarget lastPosComp = PatrolTarget.readFromItemStack(scepter);
        if (lastPosComp.pos().equals(ctx.getClickedPos()))
        {
            ctx.getPlayer().getInventory().removeItemNoUpdate(ctx.getPlayer().getInventory().selected);
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGETS_FINISHED).sendTo(ctx.getPlayer());
            return InteractionResult.FAIL;
        }
        return handleItemUsage(ctx.getLevel(), ctx.getClickedPos(), scepter, ctx.getPlayer());
    }

    /**
     * Handles the usage of the item.
     *
     * @param worldIn  the world it is used in.
     * @param pos      the position.
     * @param stack    the stack.
     * @param playerIn the player using it.
     * @return if it has been successful.
     */
    @NotNull
    private static InteractionResult handleItemUsage(final Level worldIn, final BlockPos pos, final ItemStack stack, final Player playerIn)
    {
        if (!(BuildingId.readBuildingFromItemStack(stack) instanceof final IGuardBuilding tower))
        {
            return InteractionResult.FAIL;
        }

        if (BlockPosUtil.getDistance2D(pos, tower.getID()) > tower.getPatrolDistance())
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_TOWER_TOO_FAR).sendTo(playerIn);
            return InteractionResult.FAIL;
        }

        if (tower.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.GUARD))
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_GUARD_TARGET, pos.toShortString()).sendTo(playerIn);
            tower.setGuardPos(pos);
            playerIn.getInventory().removeItemNoUpdate(playerIn.getInventory().selected);
        }
        else
        {
            if (!PatrolTarget.readFromItemStack(stack).hasPos())
            {
                tower.resetPatrolTargets();
            }
            tower.addPatrolTargets(pos);
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGET, pos.toShortString()).sendTo(playerIn);
        }
        new PatrolTarget(pos).writeToItemStack(stack);
        return InteractionResult.SUCCESS;
    }
}
