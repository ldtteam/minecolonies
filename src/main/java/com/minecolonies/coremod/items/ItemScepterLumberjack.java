package com.minecolonies.coremod.items;

import com.ldtteam.structurize.items.AbstractItemWithPosSelector;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Lumberjack Scepter Item class. Used to give tasks to Lumberjacks.
 */
public class ItemScepterLumberjack extends AbstractItemWithPosSelector
{
    /**
     * LumberjackScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterLumberjack(final Properties properties)
    {
        super(properties.maxStackSize(1));
        setRegistryName("scepterlumberjack");
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return (AbstractItemWithPosSelector) ModItems.scepterLumberjack;
    }

    @Override
    public ActionResultType onAirRightClick(final BlockPos start,
        final BlockPos end,
        final World world,
        final PlayerEntity player,
        final ItemStack itemStack)
    {
        final int minX = Math.min(start.getX(), end.getX());
        final int minZ = Math.min(start.getZ(), end.getZ());
        final int maxX = Math.max(start.getX(), end.getX());
        final int maxZ = Math.max(start.getZ(), end.getZ());
        final int area = (maxX - minX + 1) * (maxZ - minZ + 1);
        final int maxArea = (int) Math.floor(Math.PI * Math.pow(EntityAIWorkLumberjack.SEARCH_RANGE, 2));

        if (area > maxArea)
        {
            if (world.isRemote)
            {
                LanguageHandler.sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.restrictiontoobig", area, maxArea);
            }
            return ActionResultType.FAIL;
        }

        if (world.isRemote)
        {
            LanguageHandler
                .sendPlayerMessage(player, "item.minecolonies.scepterlumberjack.restrictionset", minX, maxX, minZ, maxZ, area, maxArea);
            return ActionResultType.CONSUME;
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(itemStack.getTag().getInt(TAG_ID), world);
        final BuildingLumberjack lumberjackBuilding = (BuildingLumberjack) colony.getBuildingManager()
            .getBuilding(BlockPosUtil.read(itemStack.getTag(), TAG_POS));

        lumberjackBuilding.setRestrictedArea(start, end);

        player.inventory.removeStackFromSlot(player.inventory.currentItem);
        return ActionResultType.CONSUME;
    }
}
