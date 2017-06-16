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
    private static final double BARBARIANS_MULTIPLIER              = 0.5;
    private static final double ARCHER_BARBARIANS_MULTIPLIER       = 0.25;
    private static final double CHIEF_BARBARIANS_MULTIPLIER        = 0.1;
    private static final int    PREFERRED_MAX_HORDE_SIZE           = 40;
    private static final int    PREFERRED_MAX_BARBARIANS           = 22;
    private static final int    PREFERRED_MAX_ARCHERS              = 16;
    private static final int    PREFERRED_MAX_CHIEFS               = 2;
    private static final float  WHOLE_CIRCLE                       = 360.0F;
    private static final float  HALF_A_CIRCLE                      = 180F;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianUtils()
    {
    }

    /**
     * Commences a raid for a colony.
     *
     * @param raidingWorld The world in which the raid is occurring
     * @param level        The raidLevel that the colony is at
     * @param colony       The colony at which the raid is occurring
     */
    public static void eventRaid(final World raidingWorld, final int level, final Colony colony)
    {
        if (level == LEVEL_AT_WHICH_TO_NOT_TRIGGER_RAID)
        {
            return;
        }

        int numberOfBarbarians = (int) (BARBARIANS_MULTIPLIER * level);
        int numberOfArcherBarbarians = (int) (ARCHER_BARBARIANS_MULTIPLIER * level);
        int numberOfChiefBarbarians = (int) (CHIEF_BARBARIANS_MULTIPLIER * level);

        int hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;

        if (hordeTotal > PREFERRED_MAX_HORDE_SIZE && MAX_SIZE == PREFERRED_MAX_HORDE_SIZE)
        {
            //set the preferred horde style if the total spawns is greater that the config's max size
            numberOfBarbarians = PREFERRED_MAX_BARBARIANS;
            numberOfArcherBarbarians = PREFERRED_MAX_ARCHERS;
            numberOfChiefBarbarians = PREFERRED_MAX_CHIEFS;
        }
        else if (hordeTotal > MAX_SIZE)
        {
            //Equilize the spawns so that there is less spawns than the config's max size
            numberOfBarbarians = equilizeBarbarianSpawns(hordeTotal, numberOfBarbarians);
            hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;
            numberOfArcherBarbarians = equilizeBarbarianSpawns(hordeTotal, numberOfArcherBarbarians);
            hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;
            numberOfChiefBarbarians = equilizeBarbarianSpawns(hordeTotal, numberOfChiefBarbarians);
        }

        //Make sure world isn't null
        if (raidingWorld == null)
        {
            return;
        }

        final BlockPos targetSpawnPoint = calculateSpawnLocation(raidingWorld, colony);

        spawn(BarbarianUtils.barbarian, numberOfBarbarians, targetSpawnPoint, raidingWorld);
        spawn(BarbarianUtils.archer, numberOfArcherBarbarians, targetSpawnPoint, raidingWorld);
        spawn(BarbarianUtils.chief, numberOfChiefBarbarians, targetSpawnPoint, raidingWorld);
    }

    /**
     * Reduces barbarian spawns to less than the maximum allowed (set via the config)
     *
     * @param total    The current horde size
     * @param numberOf The number of barbarians which we are reducing
     * @return the new number that the barbarians should be set to.
     */
    private static int equilizeBarbarianSpawns(final int total, final int numberOf)
    {
        int returnValue = numberOf;
        if (total > Configurations.maxBarbarianHordeSize)
        {
            returnValue = total - MAX_SIZE;

            if (returnValue < 0)
            {
                return 0;
            }
            return returnValue;
        }
        return returnValue;
    }

    /**
     * Returns whether a raid should happen depending on the Config
     * @param world The world in which the raid is possibly happening (Used to get a random number easily)
     * @return
     */
    public static boolean raidThisNight(final World world)
    {
        final float chance =(float) 1 / Configurations.averageNumberOfNightsBetweenRaids;
        return world.rand.nextFloat() < chance;
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *
     * @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     */
    public static void spawn(final ResourceLocation entityToSpawn, final int numberOfSpawns, final BlockPos spawnLocation, final World world)
    {

        if (spawnLocation != null && entityToSpawn != null && world != null)
        {
            final int x = spawnLocation.getX();
            final int y = spawnLocation.getY();
            final int z = spawnLocation.getZ();

            IntStream.range(0, numberOfSpawns).forEach(theInteger ->
            {
                final Entity entity = EntityList.createEntityByIDFromName(entityToSpawn, world);

                if (entity != null)
                {
                    setBarbarianItems(entityToSpawn, entity);
                    entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * WHOLE_CIRCLE), 0.0F);
                    world.spawnEntity(entity);
                }
            });
        }
    }

    /**
     * Sets the various items for each type of barbarian
     *
     * @param entityToSpawn The resourceLocation that is checked against
     * @param entity The entity to apply the following to.
     */
    private static void setBarbarianItems(final ResourceLocation entityToSpawn,final Entity entity)
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
    }

    /**
     * Calculate a random spawnpoint along the colony's border
     *
     * @param theWorld in the world.
     * @param colony   the Colony to spawn the barbarians near.
     * @return Returns the random blockPos
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

        final int randomDegree = theWorld.rand.nextInt( (int) WHOLE_CIRCLE);

        final double rads = randomDegree / HALF_A_CIRCLE * Math.PI;
        final double x = Math.round(center.getX() + radius * Math.sin(rads));
        final double z = Math.round(center.getZ() + radius * Math.cos(rads));
        return BlockPosUtil.getFloor(new BlockPos(x, center.getY(), z), theWorld).up();
    }
}
