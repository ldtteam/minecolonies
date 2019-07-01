package com.minecolonies.api.util;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Utility class for summoning in fireworks.
 */

public final class FireworkUtils
{
    /**
     * Private constructor to hide the public one
     */
    private FireworkUtils()
    {

    }

    /**
     * Spawns in a given number of fireworks at the corners of a given AABB in a given world
     *
     * @param realaabb       AABB of the building
     * @param world          which world to spawn it in from
     * @param explosionLevel how many fireworks to spawn in each corner
     */
    public static void spawnFireworksAtAABBCorners(final AxisAlignedBB realaabb, final World world, final int explosionLevel)
    {
        final EntityFireworkRocket firework = new EntityFireworkRocket(world, realaabb.maxX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack(explosionLevel));
        world.spawnEntity(firework);

        final EntityFireworkRocket fireworka = new EntityFireworkRocket(world, realaabb.maxX, realaabb.maxY, realaabb.minZ, genFireworkItemStack(explosionLevel));
        world.spawnEntity(fireworka);

        final EntityFireworkRocket fireworkb = new EntityFireworkRocket(world, realaabb.minX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack(explosionLevel));
        world.spawnEntity(fireworkb);

        final EntityFireworkRocket fireworkc = new EntityFireworkRocket(world, realaabb.minX, realaabb.maxY, realaabb.minZ, genFireworkItemStack(explosionLevel));
        world.spawnEntity(fireworkc);
    }

    /**
     * Generates random firework with various properties.
     *
     * @return ItemStack of random firework
     */
    private static ItemStack genFireworkItemStack(final int explosionAmount)
    {
        final Random rand = new Random();
        final ItemStack fireworkItem = new ItemStack(Items.FIREWORKS);
        final NBTTagCompound itemStackCompound = fireworkItem.getTagCompound() != null ? fireworkItem.getTagCompound() : new NBTTagCompound();
        final NBTTagCompound fireworksCompound = new NBTTagCompound();
        final NBTTagList explosionsTagList = new NBTTagList();
        for (int i = 0; i < explosionAmount; i++)
        {
            final NBTTagCompound explosionTag = new NBTTagCompound();

            explosionTag.setBoolean(TAG_FLICKER, rand.nextInt(2) == 0);
            explosionTag.setBoolean(TAG_TRAIL, rand.nextInt(2) == 0);
            explosionTag.setInteger(TAG_TYPE, rand.nextInt(5));

            final int numberOfColours = rand.nextInt(3) + 1;
            final int[] colors = new int[numberOfColours];

            for (int ia = 0; ia < numberOfColours; ia++)
            {
                colors[ia] = ItemDye.DYE_COLORS[rand.nextInt(15)];
            }
            explosionTag.setIntArray(TAG_COLORS, colors);
            explosionsTagList.appendTag(explosionTag);
        }
        fireworksCompound.setTag(TAG_EXPLOSIONS, explosionsTagList);
        itemStackCompound.setTag(TAG_FIREWORKS, fireworksCompound);
        fireworkItem.setTagCompound(itemStackCompound);
        return fireworkItem;
    }
}
