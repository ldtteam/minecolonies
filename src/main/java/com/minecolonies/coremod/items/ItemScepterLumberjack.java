package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
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
    private static final String NBT_END_POS   = Constants.MOD_ID + ":" + "end_pos";

    /**
     * LumberjackScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterLumberjack(final Properties properties)
    {
        super("scepterlumberjack", properties.maxStackSize(1));
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        if (context.getWorld().isRemote)
        {
            return ActionResultType.FAIL;
        }

        final ItemStack scepter = context.getPlayer().getHeldItem(context.getHand());
        LanguageHandler.sendPlayerMessage(context.getPlayer(), "item.minecolonies.scepterlumberjack.usedend");
        if (setPosition(scepter.getOrCreateTag(), NBT_START_POS, context.getPos(), context.getPlayer()))
        {
            storeRestrictedArea(context.getPlayer(), scepter.getOrCreateTag(), context.getWorld());
        }
        return ActionResultType.FAIL;
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(@NotNull final BlockState state, @NotNull final World world, @NotNull final BlockPos pos, @NotNull final PlayerEntity player)
    {
        if (!world.isRemote)
        {
            final ItemStack tool = player.getHeldItemMainhand();
            LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.usedstart");
            if (setPosition(tool.getOrCreateTag(), NBT_END_POS, pos, player))
            {
                storeRestrictedArea(player, tool.getOrCreateTag(), world);
            }
        }

        return false;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 3.4028235E38F;
    }

    private void storeRestrictedArea(final PlayerEntity player, final CompoundNBT compound, final World worldIn)
    {
        final BlockPos startRestriction = BlockPosUtil.read(compound, NBT_START_POS);
        final BlockPos endRestriction = BlockPosUtil.read(compound, NBT_END_POS);

        // Check restricted area isn't too large
        final int minX = Math.min(startRestriction.getX(), endRestriction.getX());
        final int minZ = Math.min(startRestriction.getZ(), endRestriction.getZ());
        final int maxX = Math.max(startRestriction.getX(), endRestriction.getX());
        final int maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ());

        final int distX = maxX - minX;
        final int distZ = maxZ - minZ;

        final int area = distX * distZ;
        final int maxArea = (int) Math.floor(2 * Math.pow(EntityAIWorkLumberjack.SEARCH_RANGE, 2));

        if (area > maxArea)
        {
            LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.restrictiontoobig", area, maxArea);
            return;
        }

        LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.restrictionset", minX, maxX, minZ, maxZ, area, maxArea);
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), worldIn);
        final BlockPos hutPos = BlockPosUtil.read(compound, TAG_POS);
        final BuildingLumberjack hut = colony.getBuildingManager().getBuilding(hutPos, BuildingLumberjack.class);
        if (hut == null)
        {
            return;
        }

        hut.setRestrictedArea(startRestriction, endRestriction);
    }

    /**
     * Continue the flux.
     * @param compound the set compound.
     * @param key the key to set.
     * @param pos the pos to set for the key.
     * @param player the player entity.
     * @return true if continue.
     */
    private boolean setPosition(final CompoundNBT compound, final String key, final BlockPos pos, final PlayerEntity player)
    {
        if (compound.contains(key))
        {
            if (BlockPosUtil.read(compound, key).equals(pos))
            {
                player.inventory.removeStackFromSlot(player.inventory.currentItem);
                return false;
            }
        }

        BlockPosUtil.write(compound, key, pos);
        return compound.contains(NBT_END_POS) && compound.contains(NBT_START_POS);
    }
}
