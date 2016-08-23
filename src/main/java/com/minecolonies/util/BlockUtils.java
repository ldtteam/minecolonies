package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for all Block type checking
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place
     */
    private static List<BiPredicate<Block, IBlockState>> freeToPlaceBlocks =
            Arrays.asList(
                    (block, iBlockState) -> block.equals(Blocks.AIR),
                    (block, iBlockState) -> block.getMaterial(iBlockState).isLiquid(),
                    (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
                    (block, iBlockState) -> block.equals(Blocks.LEAVES),
                    (block, iBlockState) -> block.equals(Blocks.LEAVES2),
                    (block, iBlockState) -> block.equals(Blocks.DOUBLE_PLANT),
                    (block, iBlockState) -> block.equals(Blocks.GRASS),
                    (block, iBlockState) -> block instanceof BlockDoor
                                            && iBlockState != null
                                            && iBlockState.getValue(PropertyBool.create("upper"))

                         );


    /**
     * Private constructor to hide the public one
     */
    private BlockUtils()
    {
    }

    /**
     * Checks if this block type should be destroyed.
     * <p>
     * The builder uses this to check if he should clear this block.
     *
     * @param block the block type to check
     * @return true if you should back away
     */
    public static boolean shouldNeverBeMessedWith(Block block)
    {
        return block instanceof AbstractBlockHut
               || Objects.equals(block, Blocks.BEDROCK);
    }

    /**
     * Checks if this block type is something we can place for free
     * <p>
     * The builder uses this to determine if he need resources for the block
     *
     * @param block the block to check
     * @return true if we can just place it
     */
    public static boolean freeToPlace(final Block block)
    {
        return freeToPlace(block, null);
    }

    /**
     * Checks if this block type is something we can place for free
     * <p>
     * The builder uses this to determine if he need resources for the block
     *
     * @param block    the block to check
     * @param metadata the matadata this block has
     * @return true if we can just place it
     */
    public static boolean freeToPlace(final Block block, final IBlockState metadata)
    {
        if (block == null)
        {
            return true;
        }
        for (BiPredicate<Block, IBlockState> predicate : freeToPlaceBlocks)
        {
            if (predicate.test(block, metadata))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the block is water
     *
     * @param iBlockState block state to be checked
     * @return true if is water.
     */
    public static boolean isWater(IBlockState iBlockState)
    {
        return Objects.equals(iBlockState, Blocks.WATER.getDefaultState())
               || Objects.equals(iBlockState, Blocks.FLOWING_WATER.getDefaultState());
    }

    /**
     * Gets an item from a block.
     * @param block input block.
     * @return output item.
     */
    /*public static Item getItemFromBlock(Block block)
    {
        Item item = null;

        if(block instanceof BlockBanner)
        {
            item = Items.BANNER;
        }
        else if(block instanceof BlockBed)
        {
            item = Items.BED;
        }
        else if(block instanceof BlockBrewingStand)
        {
            item = Items.BREWING_STAND;
        }
        else if(block instanceof BlockCake)
        {
            item = Items.CAKE;
        }
        else if(block instanceof BlockCauldron)
        {
            item = Items.CAULDRON;
        }
        else if(block instanceof BlockCocoa)
        {
            item = Items.DYE;
        }
        else if(block instanceof BlockCrops)
        {
            if(block instanceof BlockCarrot)
            {
                item = Items.CARROT;
            }
            else if(block instanceof BlockPotato)
            {
                item = Items.POTATO;
            }
            else
            {
                item = Items.wheat_seeds;
            }
        }
        else if(block instanceof BlockDaylightDetector)
        {
            item = Item.getItemFromBlock(Blocks.daylight_detector);
        }
        else if(block instanceof BlockDoor)
        {
            item = block == Blocks.iron_door ? Items.iron_door
                                             : (block == Blocks.spruce_door ? Items.spruce_door
                                             : (block == Blocks.birch_door ? Items.birch_door
                                             : (block == Blocks.jungle_door ? Items.jungle_door
                                             : (block == Blocks.acacia_door ? Items.acacia_door
                                             : (block == Blocks.dark_oak_door ? Items.dark_oak_door
                                             : Items.oak_door)))));
        }
        else if(block instanceof BlockFarmland)
        {
            item = Item.getItemFromBlock(Blocks.dirt);
        }
        else if(block instanceof BlockFlowerPot)
        {
            item = Items.flower_pot;
        }
        else if(block instanceof BlockFurnace)
        {
            item = Item.getItemFromBlock(Blocks.furnace);
        }
        else if(block instanceof BlockNetherWart)
        {
            item = Items.nether_wart;
        }
        else if(block instanceof BlockRedstoneComparator)
        {
            item = Items.comparator;
        }
        else if(block instanceof BlockRedstoneLight)
        {
            item = Item.getItemFromBlock(Blocks.redstone_lamp);
        }
        else if(block instanceof BlockRedstoneRepeater)
        {
            item = Items.repeater;
        }
        else if(block instanceof BlockRedstoneTorch)
        {
            item = Item.getItemFromBlock(Blocks.redstone_torch);
        }
        else if(block instanceof BlockRedstoneWire)
        {
            item = Items.redstone;
        }
        else if(block instanceof BlockReed)
        {
            item = Items.reeds;
        }
        else if(block instanceof BlockSign)
        {
            item = Items.sign;
        }
        else if(block instanceof BlockSkull)
        {
            item = Items.skull;
        }
        else if(block instanceof BlockStem)
        {
            item =  block == Blocks.pumpkin ? Items.pumpkin_seeds
                                                 : (block == Blocks.melon_block ? Items.melon_seeds
                                                 : null);
        }
        else if(block instanceof BlockStoneSlab)
        {
            item = Item.getItemFromBlock(Blocks.stone_slab);
        }
        else if(block instanceof BlockStoneSlabNew)
        {
            item = Item.getItemFromBlock(Blocks.stone_slab2);
        }
        else if(block instanceof BlockTripWire)
        {
            item = Items.string;
        }
        else if(block instanceof BlockWoodSlab)
        {
            item = Item.getItemFromBlock(Blocks.wooden_slab);
        }

        return item;
    }*/
}
