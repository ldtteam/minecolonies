package com.minecolonies.api.entity.mobs.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.MobSpawnUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE;

/**
 * Utils for Colony pirate events
 */
public final class BarbarianEventUtils
{
    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianEventUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Trigger for the barbarian event.
     * @param world the world it happens in.
     * @param colony the colony it concerns.
     * @param target the target position.
     * @param raidNumber the number of raiders.
     * @param horde the detailed horde.
     */
    public static void barbarianEvent(final World world, final IColony colony, final BlockPos target, final int raidNumber, final MobEventsUtils.Horde horde)
    {
        BlockPos targetSpawnPoint = target;
        if (BlockPosUtil.getFloor(targetSpawnPoint, 0, world) == null)
        {
            targetSpawnPoint = new BlockPos(targetSpawnPoint.getX(), colony.getCenter().getY(), targetSpawnPoint.getZ());
            buildPlatform(targetSpawnPoint, world);
        }

        LanguageHandler.sendPlayersMessage(
          colony.getImportantMessageEntityPlayers(),
          RAID_EVENT_MESSAGE + raidNumber, colony.getName());

        MobSpawnUtils.spawn(BARBARIAN, horde.numberOfRaiders, targetSpawnPoint, world, colony);
        MobSpawnUtils.spawn(ARCHER, horde.numberOfArchers, targetSpawnPoint, world, colony);
        MobSpawnUtils.spawn(CHIEF, horde.numberOfBosses, targetSpawnPoint, world, colony);
    }

    /**
     * Build a platform to sustain the barbarians.
     * @param target the target to build it.
     * @param world the world to build it in.
     */
    private static void buildPlatform(final BlockPos target, final World world)
    {
        final IBlockState platformBlock = Blocks.WOODEN_SLAB.getDefaultState();

        for (int z = 0; z < 5; z++)
        {
            for (int x = 0; x < 5; x++)
            {
                final int sum = x * x + z * z;
                if (sum < (5 * 5) / 4)
                {
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() -z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() -z), platformBlock);
                }
            }
        }
    }
}
