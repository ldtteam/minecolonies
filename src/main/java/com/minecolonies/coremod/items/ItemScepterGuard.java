package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.client.gui.WindowGuardControl;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

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
     */
    public ItemScepterGuard()
    {
        super("scepterGuard");
        this.setMaxDamage(2);

        maxStackSize = 1;
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(
      final PlayerEntity playerIn,
      final World worldIn,
      final BlockPos pos,
      final Hand hand,
      final Direction facing,
      final float hitX,
      final float hitY,
      final float hitZ)
    {
        // if server world, do nothing
        if (worldIn.isRemote)
        {
            return ActionResultType.FAIL;
        }

        final ItemStack scepter = playerIn.getHeldItem(hand);
        if (!scepter.hasTag())
        {
            scepter.put(new CompoundNBT());
        }
        final CompoundNBT compound = scepter.getTag();

        if (compound.keySet().contains(TAG_LAST_POS))
        {
            final BlockPos lastPos = BlockPosUtil.read(compound, TAG_LAST_POS);
            if (lastPos.equals(pos))
            {
                playerIn.inventory.removeStackFromSlot(playerIn.inventory.currentItem);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolDoubleClick");
                return ActionResultType.FAIL;
            }
        }
        return handleItemUsage(worldIn, pos, compound, playerIn);
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, @NotNull final Hand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTag())
        {
            stack.put(new CompoundNBT());
        }
        final CompoundNBT compound = stack.getTag();

        if (worldIn.isRemote && compound != null)
        {
            if (!compound.keySet().contains(TAG_ID))
            {
                return ActionResult.newResult(ActionResultType.FAIL, stack);
            }
            final IColonyView colony = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_ID), Minecraft.getMinecraft().world.provider.getDimension());
            if (colony == null)
            {
                return ActionResult.newResult(ActionResultType.FAIL, stack);
            }
            final BlockPos guardTower = BlockPosUtil.read(compound, TAG_POS);
            final IBuildingView hut = colony.getBuilding(guardTower);

            if (hut instanceof AbstractBuildingGuards.View && playerIn.isSneaking())
            {
                final WindowGuardControl window = new WindowGuardControl((AbstractBuildingGuards.View) hut);
                window.open();
            }
        }

        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
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
        if (!compound.keySet().contains(TAG_ID))
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

        if(BlockPosUtil.getDistance2D(pos, guardTower) > tower.getPatrolDistance())
        {
            LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolClickGuardTooFar");
            return ActionResultType.FAIL;
        }

        final GuardTask task = GuardTask.values()[compound.getInt("task")];
        final ICitizenData citizen = tower.getMainCitizen();

        String name = "";
        if (citizen != null)
        {
            name = " " + citizen.getName();
        }

        if (task.equals(GuardTask.GUARD))
        {
            LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolClickGuard", pos, name);
            tower.setGuardPos(pos);
            playerIn.inventory.removeStackFromSlot(playerIn.inventory.currentItem);
        }
        else
        {
            if (!compound.keySet().contains(TAG_LAST_POS))
            {
                tower.resetPatrolTargets();
            }
            tower.addPatrolTargets(pos);
            LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolClickPatrol", pos, name);
        }
        BlockPosUtil.write(compound, TAG_LAST_POS, pos);

        return ActionResultType.SUCCESS;
    }
}
