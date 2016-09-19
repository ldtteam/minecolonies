package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for all Block type checking.
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place.
     */
    @NotNull
    private static List<BiPredicate<Block, IBlockState>> freeToPlaceBlocks =
      Arrays.asList(
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getMaterial().isLiquid(),
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
     * Private constructor to hide the public one.
     */
    private BlockUtils()
    {
        //Hides implicit constructor.
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
     * Checks if this block type is something we can place for free.
     * <p>
     * The builder uses this to determine if he need resources for the block.
     *
     * @param block the block to check.
     * @return true if we can just place it.
     */
    public static boolean freeToPlace(final Block block)
    {
        return freeToPlace(block, null);
    }

    /**
     * Checks if this block type is something we can place for free.
     * <p>
     * The builder uses this to determine if he need resources for the block.
     *
     * @param block      the block to check.
     * @param blockState the state this block has.
     * @return true if we can just place it.
     */
    public static boolean freeToPlace(@Nullable final Block block, final IBlockState blockState)
    {
        if (block == null)
        {
            return true;
        }
        for (@NotNull BiPredicate<Block, IBlockState> predicate : freeToPlaceBlocks)
        {
            if (predicate.test(block, blockState))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the block is water.
     *
     * @param iBlockState block state to be checked.
     * @return true if is water.
     */
    public static boolean isWater(IBlockState iBlockState)
    {
        return Objects.equals(iBlockState, Blocks.WATER.getDefaultState())
                 || Objects.equals(iBlockState, Blocks.FLOWING_WATER.getDefaultState());
    }

    /**
     * Checks if a certain block returns a seed as the item.
     *
     * @param world the world the block is in.
     * @param pos   the position the block is at.
     * @return true if is a seed.
     */
    public static boolean isBlockSeed(@NotNull World world, @NotNull BlockPos pos)
    {
        return BlockUtils.getItemStackFromBlockState(world.getBlockState(pos.up())) != null
                 && BlockUtils.getItemStackFromBlockState(world.getBlockState(pos.up())).getItem() instanceof ItemSeeds;
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull IBlockState blockState)
    {
        final Item item = getItem(blockState);

        if (item == null)
        {
            return null;
        }

        Block block = blockState.getBlock();
        if (item instanceof ItemBlock)
        {
            block = Block.getBlockFromItem(item);
        }

        return new ItemStack(item, 1, getDamageValue(block, blockState));
    }

    private static Item getItem(@NotNull IBlockState blockState)
    {
        if (blockState.getBlock() instanceof BlockBanner)
        {
            return Items.BANNER;
        }
        else if (blockState.getBlock() instanceof BlockBed)
        {
            return Items.BED;
        }
        else if (blockState.getBlock() instanceof BlockBrewingStand)
        {
            return Items.BREWING_STAND;
        }
        else if (blockState.getBlock() instanceof BlockCake)
        {
            return Items.CAKE;
        }
        else if (blockState.getBlock() instanceof BlockCauldron)
        {
            return Items.CAULDRON;
        }
        else if (blockState.getBlock() instanceof BlockCocoa)
        {
            return Items.DYE;
        }
        else if (blockState.getBlock() instanceof BlockCrops)
        {
            ItemStack stack = ((BlockCrops) blockState.getBlock()).getItem(null, null, blockState);
            if(stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        else if (blockState.getBlock() instanceof BlockDaylightDetector)
        {
            return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
        }
        else if (blockState.getBlock() instanceof BlockDoor)
        {
            Item item = blockState.getBlock() == Blocks.IRON_DOOR ? Items.IRON_DOOR :
                    (blockState.getBlock() == Blocks.SPRUCE_DOOR ? Items.SPRUCE_DOOR
                            : (blockState.getBlock() == Blocks.BIRCH_DOOR ? Items.BIRCH_DOOR
                                    : (blockState.getBlock() == Blocks.JUNGLE_DOOR ? Items.JUNGLE_DOOR
                                            : (blockState.getBlock() == Blocks.ACACIA_DOOR ? Items.ACACIA_DOOR
                                                    : (blockState.getBlock() == Blocks.DARK_OAK_DOOR ? Items.DARK_OAK_DOOR : Items.OAK_DOOR)))));

            return item == null ? Item.getItemFromBlock(blockState.getBlock()) : item;
        }
        else if (blockState.getBlock() instanceof BlockFarmland || blockState.getBlock() instanceof BlockGrassPath)
        {
            return Item.getItemFromBlock(Blocks.DIRT);
        }
        else if (blockState.getBlock() instanceof BlockFlowerPot)
        {
            return Items.FLOWER_POT;
        }
        else if (blockState.getBlock() instanceof BlockFurnace)
        {
            return Item.getItemFromBlock(Blocks.FURNACE);
        }
        else if (blockState.getBlock() instanceof BlockHugeMushroom)
        {
            // Can the builder even build this?
            return blockState.getBlock().getItemDropped(null, null, 0);
        }
        else if (blockState.getBlock() instanceof BlockNetherWart)
        {
            return Items.NETHER_WART;
        }
        else if (blockState.getBlock() instanceof BlockPistonExtension)
        {
            // Not really sure what we want to do here...
            return blockState.getValue(BlockPistonExtension.TYPE) == BlockPistonExtension.EnumPistonType.STICKY
                     ? Item.getItemFromBlock(Blocks.STICKY_PISTON)
                     : Item.getItemFromBlock(Blocks.PISTON);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneComparator)
        {
            return Items.COMPARATOR;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneLight)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_LAMP);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneRepeater)
        {
            return Items.REPEATER;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneTorch)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneWire)
        {
            return Items.REDSTONE;
        }
        else if (blockState.getBlock() instanceof BlockReed)
        {
            return Items.REEDS;
        }
        else if (blockState.getBlock() instanceof BlockSign)
        {
            return Items.SIGN;
        }
        else if (blockState.getBlock() instanceof BlockSkull)
        {
            return Items.SKULL;
        }
        else if (blockState.getBlock() instanceof BlockStem)
        {
            ItemStack stack = ((BlockStem) blockState.getBlock()).getItem(null, null, blockState);
            if(stack != null)
            {
                return stack.getItem();
            }
            return Items.MELON_SEEDS;
        }
        else if (blockState.getBlock() instanceof BlockStoneSlab)
        {
            //Builder won't know how to build double stone slab
            return Item.getItemFromBlock(Blocks.STONE_SLAB);
        }
        else if(blockState.getBlock() instanceof BlockPurpurSlab)
        {
            return Item.getItemFromBlock(Blocks.PURPUR_SLAB);
        }
        else if (blockState.getBlock() instanceof BlockStoneSlabNew)
        {
            return Item.getItemFromBlock(Blocks.STONE_SLAB2);
        }
        else if (blockState.getBlock() instanceof BlockTripWire)
        {
            return Items.STRING;
        }
        else if (blockState.getBlock() instanceof BlockWoodSlab)
        {
            //Builder will also have trouble with double wood slab
            return Item.getItemFromBlock(Blocks.WOODEN_SLAB);
        }
        else
        {
            return Item.getItemFromBlock(blockState.getBlock());
        }
    }

    private static int getDamageValue(Block block, @NotNull IBlockState blockState)
    {
        if (block instanceof BlockCocoa)
        {
            return EnumDyeColor.BROWN.getDyeDamage();
        }
        else if (block instanceof BlockDirt)
        {
            return blockState.getValue(BlockDirt.VARIANT).getMetadata();
        }
        else if (block instanceof BlockDoublePlant
                   && blockState.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER)
        {
            //If upper part we can't do much here
            return blockState.getValue(BlockDoublePlant.VARIANT).getMeta();
        }
        else if (block instanceof BlockNewLeaf)
        {
            return block.getMetaFromState(blockState) & 3;
        }
        else if (block instanceof BlockOre)
        {
            return 0;
        }
        else if (block instanceof BlockSilverfish || block instanceof BlockTallGrass)
        {
            return block.getMetaFromState(blockState);
        }
        else if (block instanceof BlockSlab)
        {
            return block.damageDropped(blockState) & 7;
        }
        else
        {
            return block.damageDropped(blockState);
        }
    }
}
