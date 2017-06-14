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
     * ResourceLocations for barbarians
     */
    /* default */ public static ResourceLocation barbarian = EntityList.getKey(EntityBarbarian.class);
    /* default */ public static ResourceLocation archer    = EntityList.getKey(EntityArcherBarbarian.class);
    /* default */ public static ResourceLocation chief     = EntityList.getKey(EntityChiefBarbarian.class);

    public static void doRaid(World world, final int level, Colony colony)
    {
        eventRaid(world, level, colony);
    }

    /**
     * Commences a Raid event in a specific colony, with a specific RaidLevel
     *
     * @param raidingWorld
     * @param level
     * @param colony
     */

    private static void eventRaid(World raidingWorld, final int level, Colony colony)
    {
        int levelAtWhichToNotTriggerRaid = 1;
        if (level == levelAtWhichToNotTriggerRaid)
        {
            return;
        }

        int numberOfBarbarians = level;
        int numberOfArcherBarbarians = (int) (0.5 * level);
        int numberOfChiefBarbarians = (int) (0.1 * level);

        int hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;

        if (hordeTotal > Configurations.maxBarbarianHordeSize)
        {
            final int maxSize = Configurations.maxBarbarianHordeSize;
            if (hordeTotal > 40 && maxSize == 40)
            {
                numberOfBarbarians = 22;
                numberOfArcherBarbarians = 16;
                numberOfChiefBarbarians = 2;
            }

            numberOfBarbarians = hordeTotal - maxSize;

            //For error handling and correct hordeTotal
            if (numberOfBarbarians < 0)
            {
                numberOfBarbarians = 0;
            }
            hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;
            if (hordeTotal > maxSize)
            {
                numberOfArcherBarbarians = hordeTotal - maxSize;

                //For error handling and correct hordeTotal
                if (numberOfArcherBarbarians < 0)
                {
                    numberOfArcherBarbarians = 0;
                }
                hordeTotal = numberOfArcherBarbarians + numberOfBarbarians + numberOfChiefBarbarians;
                if (hordeTotal > maxSize)
                {
                    numberOfChiefBarbarians = hordeTotal - maxSize;

                    //For error handling and correct hordeTotal
                    if (numberOfChiefBarbarians < 0)
                    {
                        numberOfChiefBarbarians = 0;
                    }
                }
            }
        }

        int x = colony.getCenter().getX();
        int y = colony.getCenter().getY();
        int z = colony.getCenter().getZ();

        //Make sure world isn't null
        if (raidingWorld == null)
        {
            return;
        }
        switch (raidingWorld.rand.nextInt(7))
        {
            case 0:
                x += Configurations.workingRangeTownHall + 20;
                break;
            case 1:
                x -= Configurations.workingRangeTownHall + 20;
                break;
            case 2:
                z += Configurations.workingRangeTownHall + 20;
                break;
            case 3:
                z -= Configurations.workingRangeTownHall + 20;
                break;
            case 4:
                x += Configurations.workingRangeTownHall + 20;
                z += Configurations.workingRangeTownHall + 20;
                break;
            case 5:
                x += Configurations.workingRangeTownHall + 20;
                z -= Configurations.workingRangeTownHall + 20;
                break;
            case 6:
                x -= Configurations.workingRangeTownHall + 20;
                z += Configurations.workingRangeTownHall + 20;
                break;
            case 7:
                x -= Configurations.workingRangeTownHall + 20;
                z -= Configurations.workingRangeTownHall + 20;
                break;
            default:
                x += Configurations.workingRangeTownHall + 20;
                break;
        }

        y = raidingWorld.getTopSolidOrLiquidBlock(new BlockPos.MutableBlockPos(x, y, z)).getY(); //Make sure mob spawns on surface.

        spawn(BarbarianUtils.barbarian, numberOfBarbarians, x, y, z, raidingWorld);
        spawn(BarbarianUtils.archer, numberOfArcherBarbarians, x, y, z, raidingWorld);
        spawn(BarbarianUtils.chief, numberOfChiefBarbarians, x, y, z, raidingWorld);
    }

    /**
     * Spawns EntityToSPawn at the X, Y, Z
     *
     * @param entityToSpawn
     * @param numberOfSpawns
     * @param x
     * @param y
     * @param z
     * @param world
     */

    public static void spawn(final ResourceLocation entityToSpawn, int numberOfSpawns, int x, int y, int z, final World world)
    {
        IntStream.range(0, numberOfSpawns).forEach($ ->
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
                    entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
                    world.spawnEntity(entity);
                }
            }
        });
    }
}
