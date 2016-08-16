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
                    (block, iBlockState) -> block.equals(Blocks.air),
                    (block, iBlockState) -> block.getMaterial().isLiquid(),
                    (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
                    (block, iBlockState) -> block.equals(Blocks.leaves),
                    (block, iBlockState) -> block.equals(Blocks.leaves2),
                    (block, iBlockState) -> block.equals(Blocks.double_plant),
                    (block, iBlockState) -> block.equals(Blocks.grass),
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
               || Objects.equals(block, Blocks.bedrock);
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
        return Objects.equals(iBlockState, Blocks.water.getDefaultState())
               || Objects.equals(iBlockState, Blocks.flowing_water.getDefaultState());
    }

    /**
     * Gets an item from a block.
     * @param block input block.
     * @return output item.
     */
    public static Item getItemFromBlock(Block block)
    {
        if(block instanceof BlockBanner)
        {
            return Items.banner;
        }
        else if(block instanceof BlockBed)
        {
            return Items.bed;
        }
        else if(block instanceof BlockBrewingStand)
        {
            return Items.brewing_stand;
        }
        else if(block instanceof BlockCake)
        {
            return Items.cake;
        }
        else if(block instanceof BlockCauldron)
        {
            return Items.cauldron;
        }
        else if(block instanceof BlockCocoa)
        {
            return Items.dye;
        }
        else if(block instanceof BlockCrops)
        {
            if(block instanceof BlockCarrot)
            {
                return Items.carrot;
            }
            else if(block instanceof BlockPotato)
            {
                return Items.potato;
            }
            return Items.wheat_seeds;
        }
        else if(block instanceof BlockDaylightDetector)
        {
            return Item.getItemFromBlock(Blocks.daylight_detector);
        }
        else if(block instanceof BlockDoor)
        {
            return block == Blocks.iron_door ? Items.iron_door : (block == Blocks.spruce_door ? Items.spruce_door :
                                                                  (block == Blocks.birch_door ? Items.birch_door :
                                                                   (block == Blocks.jungle_door ? Items.jungle_door :
                                                                    (block == Blocks.acacia_door ? Items.acacia_door :
                                                                     (block == Blocks.dark_oak_door ? Items.dark_oak_door : Items.oak_door)))));
        }
        else if(block instanceof BlockFarmland)
        {
            return Item.getItemFromBlock(Blocks.dirt);
        }
        else if(block instanceof BlockFlowerPot)
        {
            return Items.flower_pot;
        }
        else if(block instanceof BlockFurnace)
        {
            return Item.getItemFromBlock(Blocks.furnace);
        }
        else if(block instanceof BlockNetherWart)
        {
            return Items.nether_wart;
        }
        else if(block instanceof BlockRedstoneComparator)
        {
            return Items.comparator;
        }
        else if(block instanceof BlockRedstoneLight)
        {
            return Item.getItemFromBlock(Blocks.redstone_lamp);
        }
        else if(block instanceof BlockRedstoneRepeater)
        {
            return Items.repeater;
        }
        else if(block instanceof BlockRedstoneTorch)
        {
            return Item.getItemFromBlock(Blocks.redstone_torch);
        }
        else if(block instanceof BlockRedstoneWire)
        {
            return Items.redstone;
        }
        else if(block instanceof BlockReed)
        {
            return Items.reeds;
        }
        else if(block instanceof BlockSign)
        {
            return Items.sign;
        }
        else if(block instanceof BlockSkull)
        {
            return Items.skull;
        }
        else if(block instanceof BlockStem)
        {
            Item item =  block == Blocks.pumpkin ? Items.pumpkin_seeds : (block == Blocks.melon_block ? Items.melon_seeds : null);
            return item != null ? item : null;
        }
        else if(block instanceof BlockStoneSlab)
        {
            return Item.getItemFromBlock(Blocks.stone_slab);
        }
        else if(block instanceof BlockStoneSlabNew)
        {
            return Item.getItemFromBlock(Blocks.stone_slab2);
        }
        else if(block instanceof BlockTripWire)
        {
            return Items.string;
        }
        else if(block instanceof BlockWoodSlab)
        {
            return Item.getItemFromBlock(Blocks.wooden_slab);
        }

        return null;
    }
}
