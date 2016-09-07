package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.BlockPos;
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
                 || Objects.equals(block, Blocks.bedrock);
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
        return Objects.equals(iBlockState, Blocks.water.getDefaultState())
                 || Objects.equals(iBlockState, Blocks.flowing_water.getDefaultState());
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
            return Items.banner;
        }
        else if (blockState.getBlock() instanceof BlockBed)
        {
            return Items.bed;
        }
        else if (blockState.getBlock() instanceof BlockBrewingStand)
        {
            return Items.brewing_stand;
        }
        else if (blockState.getBlock() instanceof BlockCake)
        {
            return Items.cake;
        }
        else if (blockState.getBlock() instanceof BlockCauldron)
        {
            return Items.cauldron;
        }
        else if (blockState.getBlock() instanceof BlockCocoa)
        {
            return Items.dye;
        }
        else if (blockState.getBlock() instanceof BlockCrops)
        {
            if (blockState.getBlock() instanceof BlockCarrot)
            {
                return Items.carrot;
            }
            else if (blockState.getBlock() instanceof BlockPotato)
            {
                return Items.potato;
            }
            return Items.wheat_seeds;
        }
        else if (blockState.getBlock() instanceof BlockDaylightDetector)
        {
            return Item.getItemFromBlock(Blocks.daylight_detector);
        }
        else if (blockState.getBlock() instanceof BlockDoor)
        {
            return ((BlockDoor) blockState.getBlock()).getItem();
        }
        else if (blockState.getBlock() instanceof BlockFarmland)
        {
            return Item.getItemFromBlock(Blocks.dirt);
        }
        else if (blockState.getBlock() instanceof BlockFlowerPot)
        {
            return Items.flower_pot;
        }
        else if (blockState.getBlock() instanceof BlockFurnace)
        {
            return Item.getItemFromBlock(Blocks.furnace);
        }
        else if (blockState.getBlock() instanceof BlockHugeMushroom)
        {
            // Can the builder even build this?
            return blockState.getBlock().getItemDropped(null, null, 0);
        }
        else if (blockState.getBlock() instanceof BlockNetherWart)
        {
            return Items.nether_wart;
        }
        else if (blockState.getBlock() instanceof BlockPistonExtension)
        {
            // Not really sure what we want to do here...
            return blockState.getValue(BlockPistonExtension.TYPE) == BlockPistonExtension.EnumPistonType.STICKY
                     ? Item.getItemFromBlock(Blocks.sticky_piston)
                     : Item.getItemFromBlock(Blocks.piston);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneComparator)
        {
            return Items.comparator;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneLight)
        {
            return Item.getItemFromBlock(Blocks.redstone_lamp);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneRepeater)
        {
            return Items.repeater;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneTorch)
        {
            return Item.getItemFromBlock(Blocks.redstone_torch);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneWire)
        {
            return Items.redstone;
        }
        else if (blockState.getBlock() instanceof BlockReed)
        {
            return Items.reeds;
        }
        else if (blockState.getBlock() instanceof BlockSign)
        {
            return Items.sign;
        }
        else if (blockState.getBlock() instanceof BlockSkull)
        {
            return Items.skull;
        }
        else if (blockState.getBlock() instanceof BlockStem)
        {
            return ((BlockStem) blockState.getBlock()).getSeedItem();
        }
        else if (blockState.getBlock() instanceof BlockStoneSlab)
        {
            //Builder won't know how to build double stone slab
            return Item.getItemFromBlock(Blocks.stone_slab);
        }
        else if (blockState.getBlock() instanceof BlockStoneSlabNew)
        {
            return Item.getItemFromBlock(Blocks.stone_slab2);
        }
        else if (blockState.getBlock() instanceof BlockTripWire)
        {
            return Items.string;
        }
        else if (blockState.getBlock() instanceof BlockWoodSlab)
        {
            //Builder will also have trouble with double wood slab
            return Item.getItemFromBlock(Blocks.wooden_slab);
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
