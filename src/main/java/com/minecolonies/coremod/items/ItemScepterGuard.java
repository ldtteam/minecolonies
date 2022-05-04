package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterGuard extends AbstractItemMinecolonies
{
    /**
     * The compound tag for the last pos the tool has been clicked.
     */
    private static final String TAG_LAST_POS = "lastPos";

    /**
     * GuardScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterGuard(final Item.Properties properties)
    {
        super("scepterguard", properties.stacksTo(1).durability(2));
    }

    @NotNull
    @Override
    public ActionResultType useOn(final ItemUseContext ctx)
    {
        // if server world, do nothing
        if (ctx.getLevel().isClientSide)
        {
            return ActionResultType.FAIL;
        }

        final ItemStack scepter = ctx.getPlayer().getItemInHand(ctx.getHand());
        if (!scepter.hasTag())
        {
            scepter.setTag(new CompoundNBT());
        }
        final CompoundNBT compound = scepter.getTag();

        if (compound.getAllKeys().contains(TAG_LAST_POS))
        {
            final BlockPos lastPos = BlockPosUtil.read(compound, TAG_LAST_POS);
            if (lastPos.equals(ctx.getClickedPos()))
            {
                ctx.getPlayer().inventory.removeItemNoUpdate(ctx.getPlayer().inventory.selected);
                MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGETS_FINISHED).sendTo(ctx.getPlayer());
                return ActionResultType.FAIL;
            }
        }
        return handleItemUsage(ctx.getLevel(), ctx.getClickedPos(), compound, ctx.getPlayer());
    }

    /**
     * Handles the usage of the item.
     *
     * @param worldIn  the world it is used in.
     * @param pos      the position.
     * @param compound the compound.
     * @param playerIn the player using it.
     * @return if it has been successful.
     */
    @NotNull
    private static ActionResultType handleItemUsage(final World worldIn, final BlockPos pos, final CompoundNBT compound, final PlayerEntity playerIn)
    {
        if (!compound.getAllKeys().contains(TAG_ID))
        {
            return ActionResultType.FAIL;
        }
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), worldIn);
        if (colony == null)
        {
            return ActionResultType.FAIL;
        }

        final BlockPos guardTower = BlockPosUtil.read(compound, TAG_POS);
        final IBuilding hut = colony.getBuildingManager().getBuilding(guardTower);
        if (!(hut instanceof AbstractBuildingGuards))
        {
            return ActionResultType.FAIL;
        }
        final IGuardBuilding tower = (IGuardBuilding) hut;

        if (BlockPosUtil.getDistance2D(pos, guardTower) > tower.getPatrolDistance())
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_TOWER_TOO_FAR).sendTo(playerIn);
            return ActionResultType.FAIL;
        }

        if (hut.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.GUARD))
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_GUARD_TARGET, pos).sendTo(playerIn);
            tower.setGuardPos(pos);
            playerIn.inventory.removeItemNoUpdate(playerIn.inventory.selected);
        }
        else
        {
            if (!compound.getAllKeys().contains(TAG_LAST_POS))
            {
                tower.resetPatrolTargets();
            }
            tower.addPatrolTargets(pos);
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGET, pos).sendTo(playerIn);
        }
        BlockPosUtil.write(compound, TAG_LAST_POS, pos);

        return ActionResultType.SUCCESS;
    }
}
