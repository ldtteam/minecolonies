package com.minecolonies.coremod.util;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;
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
    private static final List<BiPredicate<Block, IBlockState>> freeToPlaceBlocks =
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
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     * @return returns the Rotation object.
     */
    public static Rotation getRotation(final int rotation)
    {
        switch (rotation)
        {
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }

    /**
     * Checks if this block type should be destroyed.
     * <p>
     * The builder uses this to check if he should clear this block.
     *
     * @param block the block type to check
     * @return true if you should back away
     */
    public static boolean shouldNeverBeMessedWith(final Block block)
    {
        return block instanceof AbstractBlockHut
                || Objects.equals(block, Blocks.BEDROCK);
    }

    /**
     * Gets a rotation from a block facing.
     *
     * @param facing the block facing.
     * @return the int rotation.
     */
    public static int getRotationFromFacing(final EnumFacing facing)
    {
        switch (facing)
        {
            case SOUTH:
                return 2;
            case EAST:
                return 1;
            case WEST:
                return 3;
            default:
                return 0;
        }
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
        for (@NotNull final BiPredicate<Block, IBlockState> predicate : freeToPlaceBlocks)
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
    public static boolean isWater(final IBlockState iBlockState)
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
    public static boolean isBlockSeed(@NotNull final World world, @NotNull final BlockPos pos)
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
    public static ItemStack getItemStackFromBlockState(@NotNull final IBlockState blockState)
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

    private static Item getItem(@NotNull final IBlockState blockState)
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
            final ItemStack stack = ((BlockCrops) blockState.getBlock()).getItem(null, null, blockState);
            if (stack != null)
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
            final Item item = blockState.getBlock() == Blocks.IRON_DOOR ? Items.IRON_DOOR
                    : (blockState.getBlock() == Blocks.SPRUCE_DOOR ? Items.SPRUCE_DOOR
                            : (blockState.getBlock() == Blocks.BIRCH_DOOR ? Items.BIRCH_DOOR
                                    : (blockState.getBlock() == Blocks.JUNGLE_DOOR ? Items.JUNGLE_DOOR
                                            : (blockState.getBlock() == Blocks.ACACIA_DOOR ? Items.ACACIA_DOOR
                                                    : (blockState.getBlock() == Blocks.DARK_OAK_DOOR
                                                            ? Items.DARK_OAK_DOOR
                                                            : Items.OAK_DOOR)))));

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
            final ItemStack stack = ((BlockStem) blockState.getBlock()).getItem(null, null, blockState);
            if (stack != null)
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
        else if (blockState.getBlock() instanceof BlockPurpurSlab)
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
            return GameData.getBlockItemMap().get(blockState.getBlock());
        }
    }

    /**
     * Get the damage value from a block and blockState, where the block is the placeable and obtainable block.
     * The blockstate might differ from the block.
     * @param block the block.
     * @param blockState the state.
     * @return the int damage value.
     */
    private static int getDamageValue(final Block block, @NotNull final IBlockState blockState)
    {
        if (block instanceof BlockCocoa)
        {
            return EnumDyeColor.BROWN.getDyeDamage();
        }
        else if (block instanceof BlockDirt && !(blockState.getBlock() instanceof BlockFarmland))
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

    /**
     * Checks if a certain block is a pathBlock (roadBlock).
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static boolean isPathBlock(final Block block)
    {
        return block == Blocks.GRAVEL || block == Blocks.STONEBRICK;
    }
}
