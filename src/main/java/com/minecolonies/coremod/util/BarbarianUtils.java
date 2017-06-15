package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.ai.mobs.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.stream.IntStream;

/**
 * Utils for The Barbarian Raid Event
 */
public final class BarbarianUtils
{

    /**
     * ResourceLocations for barbarians
     */
    private static final ResourceLocation barbarian = EntityList.getKey(EntityBarbarian.class);
    private static final ResourceLocation archer    = EntityList.getKey(EntityArcherBarbarian.class);
    private static final ResourceLocation chief     = EntityList.getKey(EntityChiefBarbarian.class);

    private static final int    LEVEL_AT_WHICH_TO_NOT_TRIGGER_RAID = 1;
    private static final int    MAX_SIZE                           = Configurations.maxBarbarianHordeSize;
    private static final double ARCHER_BARBARIANS_MULTIPLIER       = 0.5;
    private static final double CHIEF_BARBARIANS_MULTIPLIER        = 0.1;
    private static final int    NUMBER_OF_POSSIBLE_CASES           = 360;
    private static final int    PREFERED_MAX_HORDE_SIZE            = 40;
    private static final int    PREFERED_MAX_BARBARIANS            = 22;
    private static final int    PREFERED_MAX_ARCHERS               = 16;
    private static final int    PREFERED_MAX_CHIEFS                = 2;
    private static final float  WHOLE_CIRCLE                       = 360.0F;
    private static final float  HALF_A_CIRCLE                      = 180F;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianUtils()
    {
    }

    public static void doRaid(final World world, final int level, final Colony colony)
    {
        eventRaid(world, level, colony);
    }

    /**
     * Commences a Raid event in a specific colony, with a specific RaidLevel
     */

    private static void eventRaid(final World raidingWorld, final int level, final Colony colony)
    {
        if (level == LEVEL_AT_WHICH_TO_NOT_TRIGGER_RAID)
        {
            return;
        }

        int numberOfBarbarians = level;
        int numberOfArcherBarbarians = (int) (ARCHER_BARBARIANS_MULTIPLIER * level);
        int numberOfChiefBarbarians = (int) (CHIEF_BARBARIANS_MULTIPLIER * level);

        int hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;

        if (hordeTotal > PREFERED_MAX_HORDE_SIZE && MAX_SIZE == PREFERED_MAX_HORDE_SIZE && hordeTotal > MAX_SIZE)
        {
            numberOfBarbarians = PREFERED_MAX_BARBARIANS;
            numberOfArcherBarbarians = PREFERED_MAX_ARCHERS;
            numberOfChiefBarbarians = PREFERED_MAX_CHIEFS;
        }

        if (hordeTotal > Configurations.maxBarbarianHordeSize)
        {

            numberOfBarbarians = hordeTotal - MAX_SIZE;

            //For error handling and correct hordeTotal
            if (numberOfBarbarians < 0)
            {
                numberOfBarbarians = 0;
            }

            //Re-asses hordeTotal to see if it is now below or at MAX_SIZE
            hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;

            if (hordeTotal > MAX_SIZE)
            {

                numberOfArcherBarbarians = hordeTotal - MAX_SIZE;

                //For error handling and correct hordeTotal
                if (numberOfArcherBarbarians < 0)
                {
                    numberOfArcherBarbarians = 0;
                }

                //Re-asses hordeTotal to see if it is now below or at MAX_SIZE
                hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;

                if (hordeTotal > MAX_SIZE)
                {

                    numberOfChiefBarbarians = hordeTotal - MAX_SIZE;

                    //For error handling and correct hordeTotal
                    if (numberOfChiefBarbarians < 0)
                    {
                        numberOfChiefBarbarians = 0;
                    }
                }
            }
        }

        //Make sure world isn't null
        if (raidingWorld == null)
        {
            return;
        }

        spawn(BarbarianUtils.barbarian, numberOfBarbarians, colony, raidingWorld);
        spawn(BarbarianUtils.archer, numberOfArcherBarbarians, colony, raidingWorld);
        spawn(BarbarianUtils.chief, numberOfChiefBarbarians, colony, raidingWorld);
    }

    /**
     * Spawns EntityToSPawn at the X, Y, Z
     */

    public static void spawn(final ResourceLocation entityToSpawn, final int numberOfSpawns, final Colony colony, final World world)
    {

        final BlockPos targetSpawnPoint = calculateSpawnLocation(world, colony);

        if (targetSpawnPoint != null && entityToSpawn != null && world != null)
        {
            final int x = targetSpawnPoint.getX();
            final int y = targetSpawnPoint.getY();
            final int z = targetSpawnPoint.getZ();

            IntStream.range(0, numberOfSpawns).forEach(theInteger ->
            {
                final Entity entity = EntityList.createEntityByIDFromName(entityToSpawn, world);

                if (entity != null)
                {
                    if (entityToSpawn.equals(barbarian))
                    {
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
                    }
                    if (entityToSpawn.equals(archer))
                    {
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    }
                    if (entityToSpawn.equals(chief))
                    {
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
                    }
                    entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * WHOLE_CIRCLE), 0.0F);
                    world.spawnEntity(entity);
                }
            });
        }
    }

    /**
     * Calculate the colony border.
     *
     * @param theWorld in the world.
     * @param colony   the Colony to spawn the barbarians near.
     */
    private static BlockPos calculateSpawnLocation(final World theWorld, final Colony colony)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(theWorld, colony.getCenter());
        if (colonyView == null)
        {
            return null;
        }
        final BlockPos center = colonyView.getCenter();
        final int radius = Configurations.workingRangeTownHall;

        final int RandomDegree = theWorld.rand.nextInt(NUMBER_OF_POSSIBLE_CASES);

        for (double degrees = 0; degrees < (int) WHOLE_CIRCLE; degrees += 1)
        {
            if (degrees == RandomDegree)
            {
                final double rads = degrees / HALF_A_CIRCLE * Math.PI;
                final double x = Math.round(center.getX() + radius * Math.sin(rads));
                final double z = Math.round(center.getZ() + radius * Math.cos(rads));
                return BlockPosUtil.getFloor(new BlockPos(x, center.getY(), z), theWorld).up();
            }
        }
        return null;
    }
}
