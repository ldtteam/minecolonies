package com.minecolonies.coremod.items;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Lumberjack Scepter Item class. Used to give tasks to Lumberjacks.
 */
public class ItemScepterLumberjack extends AbstractItemMinecolonies
{
    private static final String NBT_START_POS = Constants.MOD_ID + ":" + "start_pos";
    private static final String NBT_END_POS = Constants.MOD_ID + ":" + "end_pos";

    private boolean hasSetFirstPosition = false;

    /**
     * LumberjackScepter constructor. Sets max stack to 1, like other tools.
     */
    public ItemScepterLumberjack()
    {
        super("scepterLumberjack");
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

        if (!hasSetFirstPosition)
        {
            LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.scepterLumberjack.usedStart");
            setPosition(compound, NBT_START_POS, pos);
        }
        else
        {
            LanguageHandler.sendPlayerMessage(playerIn, "item.minecolonies.scepterLumberjack.usedEnd");
            setPosition(compound, NBT_END_POS, pos);
            storeRestrictedArea(playerIn, hand, worldIn);
        }

        return EnumActionResult.SUCCESS;
    }

    private void storeRestrictedArea(final EntityPlayer player, final EnumHand hand, final World worldIn)
    {
        final ItemStack scepter = player.getHeldItem(hand);
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound compound = scepter.getTagCompound();

        final BlockPos startRestriction = BlockPosUtil.readFromNBT(compound, NBT_START_POS);
        final BlockPos endRestriction = BlockPosUtil.readFromNBT(compound, NBT_END_POS);

        // Check restricted area isn't too large
        final int minX = Math.min(startRestriction.getX(), endRestriction.getX());
        final int minZ = Math.min(startRestriction.getZ(), endRestriction.getZ());
        final int maxX = Math.max(startRestriction.getX(), endRestriction.getX());
        final int maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ());

        final int distX = maxX - minX;
        final int distZ = maxZ - minZ;

        final int area = distX * distZ;

        final int radius = EntityAIWorkLumberjack.SEARCH_RANGE;
        final double maxArea = 3.14 * Math.pow(radius, 2);

        if (area > maxArea)
        {
            LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterLumberjack.restrictionTooBig", area, maxArea);
            return;
        }

        LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterLumberjack.restrictionSet", area, maxArea);

        final Colony colony = ColonyManager.getColonyByWorld(compound.getInteger(TAG_ID), worldIn);
        final BlockPos hutPos = BlockPosUtil.readFromNBT(compound, TAG_POS);
        final AbstractBuilding hut = colony.getBuildingManager().getBuilding(hutPos);

        final AbstractFilterableListBuilding abstractBuilding = (AbstractFilterableListBuilding) hut;

        final BuildingLumberjack lumberjackBuilding = (BuildingLumberjack) abstractBuilding;

        lumberjackBuilding.setRestrictedArea(startRestriction, endRestriction);

        player.inventory.removeStackFromSlot(player.inventory.currentItem);
    }

    private void setPosition(final NBTTagCompound compound, final String NBT, final BlockPos pos)
    {
        hasSetFirstPosition = !hasSetFirstPosition;
        BlockPosUtil.writeToNBT(compound, NBT, pos);
    }
}
