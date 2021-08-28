package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

import net.minecraft.world.item.Item.Properties;

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
        super("scepterlumberjack", properties.stacksTo(1));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack scepter = context.getPlayer().getItemInHand(context.getHand());
        LanguageHandler.sendPlayerMessage(context.getPlayer(), "item.minecolonies.scepterlumberjack.usedstart");
        if (setPosition(scepter.getOrCreateTag(), NBT_START_POS, context.getClickedPos(), context.getPlayer()))
        {
            storeRestrictedArea(context.getPlayer(), scepter.getOrCreateTag(), context.getLevel());
        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canAttackBlock(@NotNull final BlockState state, @NotNull final Level world, @NotNull final BlockPos pos, @NotNull final Player player)
    {
        if (!world.isClientSide)
        {
            final ItemStack tool = player.getMainHandItem();
            LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.usedend");
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

    private void storeRestrictedArea(final Player player, final CompoundTag compound, final Level worldIn)
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
     * Set the position into the compound with the right key.
     * Decide if flux continues or stops.
     * @param compound the set compound.
     * @param key the key to set.
     * @param pos the pos to set for the key.
     * @param player the player entity.
     * @return true if continue.
     */
    private boolean setPosition(final CompoundTag compound, final String key, final BlockPos pos, final Player player)
    {
        if (compound.contains(key))
        {
            if (BlockPosUtil.read(compound, key).equals(pos))
            {
                player.inventory.removeItemNoUpdate(player.inventory.selected);
                return false;
            }
        }

        BlockPosUtil.write(compound, key, pos);
        return compound.contains(NBT_END_POS) && compound.contains(NBT_START_POS);
    }
}
