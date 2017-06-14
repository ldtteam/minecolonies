package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.Colony;
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
public class BarbarianUtils
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianUtils()
    {
    }

    /**
     * ResourceLocations for barbarians
     */
    /* default */ public static ResourceLocation barbarian = EntityList.getKey(EntityBarbarian.class);
    /* default */ public static ResourceLocation archer    = EntityList.getKey(EntityArcherBarbarian.class);
    /* default */ public static ResourceLocation chief     = EntityList.getKey(EntityChiefBarbarian.class);

    /* default */ private static final int LEVEL_AT_WHICH_TO_NOT_TRIGGER_RAID = 1;
    /* default */ private static final int MAX_SIZE                           = Configurations.maxBarbarianHordeSize;
    /* default */ private static final int NUMBER_OF_BLOCKS_OUTSIDE_RADIUS    = 20;
    /* default */ private static final double ARCHER_BARBARIANS_MULTIPLIER = 0.5;
    /* default */ private static final double CHIEF_BARBARIANS_MULTIPLIER = 0.1;
    /* default */ private static final int NUMBER_OF_POSSIBLE_CASES = 7;
    /* default */ private static final int PREFERED_MAX_HORDE_SIZE = 40;
    /* default */ private static final float WHOLE_CIRCLE = 360.0F;

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
            numberOfBarbarians = 22;
            numberOfArcherBarbarians = 16;
            numberOfChiefBarbarians = 2;
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

        int x = colony.getCenter().getX();
        int y = colony.getCenter().getY();
        int z = colony.getCenter().getZ();

        switch (raidingWorld.rand.nextInt(NUMBER_OF_POSSIBLE_CASES))
        {
            case 0:
                x += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 1:
                x -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 2:
                z += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 3:
                z -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 4:
                x += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                z += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 5:
                x += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                z -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 6:
                x -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                z += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            case 7:
                x -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                z -= Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
            default:
                x += Configurations.workingRangeTownHall + NUMBER_OF_BLOCKS_OUTSIDE_RADIUS;
                break;
        }

        //Make sure mob spawns on surface.
        y = raidingWorld.getTopSolidOrLiquidBlock(new BlockPos.MutableBlockPos(x, y, z)).getY();

        spawn(BarbarianUtils.barbarian, numberOfBarbarians, x, y, z, raidingWorld);
        spawn(BarbarianUtils.archer, numberOfArcherBarbarians, x, y, z, raidingWorld);
        spawn(BarbarianUtils.chief, numberOfChiefBarbarians, x, y, z, raidingWorld);
    }

    /**
     * Spawns EntityToSPawn at the X, Y, Z
     */

    public static void spawn(final ResourceLocation entityToSpawn, final int numberOfSpawns, final int x, final int y, final int z, final World world)
    {
        IntStream.range(0, numberOfSpawns).forEach(theInteger ->
        {
            if (entityToSpawn != null && world != null)
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
            }
        });
    }
}
