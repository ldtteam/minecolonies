package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * General purpose utilities class.
 * todo: split up into logically distinct parts
 */
public final class Utils
{
    public static final  String PICKAXE            = "pickaxe";
    public static final  String SHOVEL             = "shovel";
    public static final  String AXE                = "axe";
    public static final  String HOE                = "hoe";
    /**
     * Minecraft id for sound events
     */
    private static final int    SOUND_EVENT_ID     = 2001;
    /**
     * How much we have to bitshift to get metadata in
     */
    private static final int    METADATA_BITSHIFT  = 12;
    /**
     * The compound id for fortune enchantment
     */
    private static final int    FORTUNE_ENCHANT_ID = 35;

    /**
     * Private constructor to hide the implicit public one
     */
    private Utils()
    {
    }

    /**
     * Find the closest block near the points
     *
     * @param world   the world
     * @param point   the point where to search
     * @param radiusX x search distance
     * @param radiusY y search distance
     * @param radiusZ z search distance
     * @param height  check if blocks above the found block are air or block
     * @param blocks  Blocks to test for
     * @return the coordinates of the found block
     */
    @Nullable
    public static BlockPos scanForBlockNearPoint(@NotNull World world, @NotNull BlockPos point, int radiusX, int radiusY, int radiusZ, int height, Block... blocks)
    {
        @Nullable BlockPos closestCoords = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = point.getX() - radiusX; i <= point.getX() + radiusX; i++)
        {
            for (int j = point.getY() - radiusY; j <= point.getY() + radiusY; j++)
            {
                for (int k = point.getZ() - radiusZ; k <= point.getZ() + radiusZ; k++)
                {
                    if (checkHeight(world, i, j, k, height, blocks))
                    {
                        @NotNull BlockPos tempCoords = new BlockPos(i, j, k);

                        double distance = BlockPosUtil.getDistanceSquared(tempCoords, point);
                        if (closestCoords == null || distance < minDistance)
                        {
                            closestCoords = tempCoords;
                            minDistance = distance;
                        }
                    }
                }
            }
        }
        return closestCoords;
    }

    /**
     * Checks if the blocks above that point are all of the spezified block types.
     *
     * @param world  the world we check on
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param z      the z coordinate
     * @param height the number of blocks above to check
     * @param blocks the block types required
     * @return true if all blocks are of that type
     */
    private static boolean checkHeight(@NotNull World world, int x, int y, int z, int height, @NotNull Block... blocks)
    {
        for (int dy = 0; dy < height; dy++)
        {
            if (!arrayContains(blocks, world.getBlockState(new BlockPos(x, y + dy, z)).getBlock()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether or not the array contains the object given
     *
     * @param array Array to scan
     * @param key   Object to look for
     * @return True if found, otherwise false
     */
    private static boolean arrayContains(@NotNull Object[] array, Object key)
    {
        for (Object o : array)
        {
            if (Objects.equals(key, o))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches a block in a custom range
     *
     * @param world World instance
     * @param block searched Block
     * @param posX  X-coordinate
     * @param posY  Y-coordinate
     * @param posZ  Z-coordinate
     * @param range the range to check around the point
     * @return true if he found the block
     */
    public static boolean isBlockInRange(@NotNull World world, Block block, int posX, int posY, int posZ, int range)
    {
        for (int x = posX - range; x < posX + range; x++)
        {
            for (int z = posZ - range; z < posZ + range; z++)
            {
                for (int y = posY - range; y < posY + range; y++)
                {
                    if (Objects.equals(world.getBlockState(new BlockPos(x, y, z)).getBlock(), block))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the highest block in one y coordinate, but ignores leaves etc.
     *
     * @param world world obj
     * @param x     x coordinate
     * @param z     z coordinate
     * @return yCoordinate
     */
    public static int findTopGround(@NotNull World world, int x, int z)
    {
        int yHolder = 1;
        while (!world.canBlockSeeSky(new BlockPos(x, yHolder, z)))
        {
            yHolder++;
        }
        while (!world.getBlockState(new BlockPos(x, yHolder, z)).isOpaqueCube() ||
                 arrayContains(
                   new Block[] {Blocks.AIR, Blocks.LEAVES, Blocks.LEAVES2}
                   , world.getBlockState(new BlockPos(x, yHolder, z)).getBlock()))
        {
            yHolder--;
        }
        return yHolder;
    }

    /**
     * Checks if the flag is set in the data
     * E.G.
     * - Flag: 000101
     * - Data: 100101
     * - All Flags are set in data, so returns true.
     * Some more flags are set, but not take into account
     *
     * @param data Data to check flag in
     * @param flag Flag to check whether it is set or not
     * @return True if flag is set, otherwise false.
     */
    public static boolean testFlag(int data, int flag)
    {
        return mask(data, flag) == flag;
    }

    /**
     * Returns what flags are set, and given in mask
     * E.G.
     * - Flag: 000101
     * - Mask: 100101
     * - The 4th and 6th bit are set, so only those will be returned
     *
     * @param data Data to check
     * @param mask Mask to check
     * @return Byte in which both data bits and mask bits are set
     */
    public static int mask(int data, int mask)
    {
        return data & mask;
    }

    /**
     * Sets a flag in in the data
     * E.G.
     * - Flag: 000101
     * - Mask: 100001
     * - The 4th bit will now be set, both the 1st and 6th bit are maintained
     *
     * @param data Data to set flag in
     * @param flag Flag to set
     * @return Data with flags set
     */
    public static int setFlag(int data, int flag)
    {
        return data | flag;
    }

    /**
     * Unsets a flag
     * E.G.
     * - Flag: 000101
     * - Mask: 100101
     * - The 4th and 6th bit will be unset, the 1st bit is maintained
     *
     * @param data Data to remove flag from
     * @param flag Flag to remove
     * @return Data with flag unset
     */
    public static int unsetFlag(int data, int flag)
    {
        return data & ~flag;
    }

    /**
     * Toggles flags
     * E.G.
     * - Flag: 000101
     * - Mask: 100101
     * - The 4th and 6th will be toggled, the 1st bit is maintained
     *
     * @param data Data to toggle flag in
     * @param flag Flag to toggle
     * @return Data with flag toggled
     */
    public static int toggleFlag(int data, int flag)
    {
        return data ^ flag;
    }

    /**
     * Plays the block break effect at specific location
     *
     * @param world    World to play effect in
     * @param pos      Coordinates
     * @param block    Block that makes the sound
     * @param metadata Metadata of the block that makes sound
     */
    public static void blockBreakSoundAndEffect(@NotNull World world, BlockPos pos, Block block, int metadata, EntityCitizen citizen)
    {
        final SoundType soundType = block.getSoundType(world.getBlockState(pos), world, pos, citizen);
        world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, soundType.getVolume(), soundType.getPitch());
    }

    /**
     * Checks if a pickaxe can be used for that mining level.
     *
     * @param minlevel the level needs to have
     * @param level    the level it has
     * @return whether the pickaxe qualifies
     */
    public static boolean checkIfPickaxeQualifies(int minlevel, int level)
    {
        return checkIfPickaxeQualifies(minlevel, level, false);
    }

    /**
     * Checks if a pickaxe can be used for that mining level.
     * Be aware, it will return false for mining stone
     * with an expensive pickaxe. So set {@code beEfficient} to false
     * for that if you need it the other way around.
     *
     * @param minlevel    the level needs to have
     * @param level       the level it has
     * @param beEfficient if he should stop using diamond picks on stone
     * @return whether the pickaxe qualifies
     */
    public static boolean checkIfPickaxeQualifies(int minlevel, int level, boolean beEfficient)
    {
        //Minecraft handles this as "everything is allowed"
        if (minlevel < 0)
        {
            return true;
        }
        if (beEfficient && minlevel == 0)
        {
            //Code to not overuse on high level pickaxes
            return level >= 0 && level <= 1;
        }
        return level >= minlevel;
    }

    /**
     * Checks if this tool is useful for the miner.
     *
     * @param itemStack Item to check
     * @return True if mining tool, otherwise false
     */
    public static boolean isMiningTool(@Nullable ItemStack itemStack)
    {
        return isPickaxe(itemStack) || isShovel(itemStack);
    }

    /**
     * Checks if this ItemStack can be used as a Shovel.
     *
     * @param itemStack Item to check
     * @return True if item is shovel, otherwise false
     */
    public static boolean isShovel(@Nullable ItemStack itemStack)
    {
        return isTool(itemStack, SHOVEL);
    }

    /**
     * Checks if this ItemStack can be used as a Hoe.
     *
     * @param itemStack Item to check
     * @return True if item is hoe, otherwise false
     */
    public static boolean isHoe(@Nullable ItemStack itemStack)
    {
        return isTool(itemStack, HOE);
    }

    /**
     * Checks if this ItemStack can be used as a Tool of type.
     *
     * @param itemStack Item to check
     * @param toolType  Type of the tool
     * @return true if item can be used, otherwise false
     */
    public static boolean isTool(@Nullable ItemStack itemStack, String toolType)
    {
        return getMiningLevel(itemStack, toolType) >= 0 || (itemStack != null && itemStack.getItem() instanceof ItemHoe && "hoe".equals(toolType));
    }

    /**
     * Calculate the mining level an item has as a tool of certain type.
     *
     * @param stack the stack to test
     * @param tool  the tool category
     * @return integer value for mining level &gt;= 0 is okay
     */
    @SuppressWarnings("deprecation")
    public static int getMiningLevel(@Nullable ItemStack stack, @Nullable String tool)
    {
        if (tool == null)
        {
            //empty hand is best on blocks who don't care (0 better 1)
            return stack == null ? 0 : 1;
        }
        if (stack == null)
        {
            return -1;
        }
        //todo: use 'better' version of this thing
        return stack.getItem().getHarvestLevel(stack, tool);
    }

    /**
     * Checks if this ItemStack can be used as an Axe.
     *
     * @param itemStack Item to check
     * @return True if item is axe, otherwise false
     */
    public static boolean isAxe(@Nullable ItemStack itemStack)
    {
        return isTool(itemStack, AXE);
    }

    /**
     * Checks if this ItemStack can be used as a Pick axe.
     *
     * @param itemStack Item to check
     * @return True if item is a pick axe, otherwise false
     */
    public static boolean isPickaxe(@Nullable ItemStack itemStack)
    {
        return isTool(itemStack, PICKAXE);
    }

    /**
     * Calculates the fortune level this tool has.
     *
     * @param tool the tool to check
     * @return fortune level
     */
    public static int getFortuneOf(@Nullable ItemStack tool)
    {
        if (tool == null)
        {
            return 0;
        }
        //calculate fortune enchantment
        int fortune = 0;
        if (tool.isItemEnchanted())
        {
            NBTTagList t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.tagCount(); i++)
            {
                short id = t.getCompoundTagAt(i).getShort("id");
                if (id == FORTUNE_ENCHANT_ID)
                {
                    fortune = t.getCompoundTagAt(i).getShort("lvl");
                }
            }
        }
        return fortune;
    }
}
