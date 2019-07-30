package com.minecolonies.coremod.items;

import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.client.gui.WindowGuardControl;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
    public EnumActionResult onItemUse(
      final EntityPlayer playerIn,
      final World worldIn,
      final BlockPos pos,
      final EnumHand hand,
      final EnumFacing facing,
      final float hitX,
      final float hitY,
      final float hitZ)
    {
        // if server world, do nothing
        if (worldIn.isRemote)
        {
            return EnumActionResult.FAIL;
        }

        final ItemStack scepter = playerIn.getHeldItem(hand);
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        if (compound.hasKey(TAG_LAST_POS))
        {
            final BlockPos lastPos = BlockPosUtil.readFromNBT(compound, TAG_LAST_POS);
            if (lastPos.equals(pos))
            {
                playerIn.inventory.removeStackFromSlot(playerIn.inventory.currentItem);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolDoubleClick");
                return EnumActionResult.FAIL;
            }
        }
        return handleItemUsage(worldIn, pos, compound, playerIn);
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, @NotNull final EnumHand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = stack.getTagCompound();

        if (worldIn.isRemote && compound != null)
        {
            if (!compound.hasKey(TAG_ID))
            {
                return ActionResult.newResult(EnumActionResult.FAIL, stack);
            }
            final ColonyView colony = ColonyManager.getColonyView(compound.getInteger(TAG_ID), Minecraft.getMinecraft().world.provider.getDimension());
            if (colony == null)
            {
                return ActionResult.newResult(EnumActionResult.FAIL, stack);
            }
            final BlockPos guardTower = BlockPosUtil.readFromNBT(compound, TAG_POS);
            final AbstractBuildingView hut = colony.getBuilding(guardTower);

            if (hut instanceof AbstractBuildingGuards.View && playerIn.isSneaking())
            {
                final WindowGuardControl window = new WindowGuardControl((AbstractBuildingGuards.View) hut);
                window.open();
            }
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
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
    private static EnumActionResult handleItemUsage(final World worldIn, final BlockPos pos, final NBTTagCompound compound, final EntityPlayer playerIn)
    {
        if (!compound.hasKey(TAG_ID))
        {
            return EnumActionResult.FAIL;
        }
        final Colony colony = ColonyManager.getColonyByWorld(compound.getInteger(TAG_ID), worldIn);
        if (colony == null)
        {
            return EnumActionResult.FAIL;
        }

        final BlockPos guardTower = BlockPosUtil.readFromNBT(compound, TAG_POS);
        final AbstractBuilding hut = colony.getBuildingManager().getBuilding(guardTower);
        if (!(hut instanceof AbstractBuildingGuards))
        {
            return EnumActionResult.FAIL;
        }
        final AbstractBuildingGuards tower = (AbstractBuildingGuards) hut;

        if(BlockPosUtil.getDistance2D(pos, guardTower) > tower.getPatrolDistance())
        {
            LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolClickGuardTooFar");
            return EnumActionResult.FAIL;
        }

        final GuardTask task = GuardTask.values()[compound.getInteger("task")];
        final CitizenData citizen = tower.getMainCitizen();

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
            if (!compound.hasKey(TAG_LAST_POS))
            {
                tower.resetPatrolTargets();
            }
            tower.addPatrolTargets(pos);
            LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.job.guard.toolClickPatrol", pos, name);
        }
        BlockPosUtil.writeToNBT(compound, TAG_LAST_POS, pos);

        return EnumActionResult.SUCCESS;
    }
}
