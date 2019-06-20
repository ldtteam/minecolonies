package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Utility class for summoning in fireworks.
 */

public abstract class FireworkUtils
{
    public static void spawnFireworksAtBuildingCorners(final AxisAlignedBB realaabb, final IColony colony, final int newLevel)
    {
        final EntityFireworkRocket firework = new EntityFireworkRocket(colony.getWorld(), realaabb.maxX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack(newLevel));

        colony.getWorld().spawnEntity(firework);
        final EntityFireworkRocket fireworka = new EntityFireworkRocket(colony.getWorld(), realaabb.maxX, realaabb.maxY, realaabb.minZ, genFireworkItemStack(newLevel));

        colony.getWorld().spawnEntity(fireworka);
        final EntityFireworkRocket fireworkb = new EntityFireworkRocket(colony.getWorld(), realaabb.minX, realaabb.maxY, realaabb.maxZ, genFireworkItemStack(newLevel));

        colony.getWorld().spawnEntity(fireworkb);
        final EntityFireworkRocket fireworkc = new EntityFireworkRocket(colony.getWorld(), realaabb.minX, realaabb.maxY, realaabb.minZ, genFireworkItemStack(newLevel));

        colony.getWorld().spawnEntity(fireworkc);
    }

    /**
     * Generates random firework with various properties.
     *
     * @return ItemStack of random firework
     */
    private static ItemStack genFireworkItemStack(final int explosionAmount)
    {
        final Random rand = new Random();
        final ItemStack fireworkItem = new ItemStack(new ItemFirework());
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
