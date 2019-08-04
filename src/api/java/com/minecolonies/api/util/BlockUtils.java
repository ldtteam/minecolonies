package com.minecolonies.api.util;

import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import net.minecraft.block.*;
import net.minecraft.block.properties.BooleanProperty;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
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
    private static final List<BiPredicate<Block, BlockState>> freeToPlaceBlocks =
      Arrays.asList(
        (block, BlockState) -> block.equals(Blocks.AIR),
        (block, BlockState) -> BlockState.getMaterial().isLiquid(),
        (block, BlockState) -> BlockUtils.isWater(block.getDefaultState()),
        (block, BlockState) -> block.equals(Blocks.LEAVES),
        (block, BlockState) -> block.equals(Blocks.LEAVES2),
        (block, BlockState) -> block.equals(Blocks.DOUBLE_PLANT),
        (block, BlockState) -> block.equals(Blocks.GRASS),
        (block, BlockState) -> block instanceof DoorBlock
                                  && BlockState != null
                                  && BlockState.getValue(BooleanProperty.create("upper"))

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
     * Get the filler block at a certain location.
     * If block follows gravity laws return dirt.
     *
     * @param world    the world the block is in.
     * @param location the location it is at.
     * @return the BlockState of the filler block.
     */
    public static BlockState getSubstitutionBlockAtWorld(@NotNull final World world, @NotNull final BlockPos location)
    {
        final BlockState filler = world.getBiome(location).fillerBlock;
        if (filler.getBlock() == Blocks.SAND)
        {
            return Blocks.SANDSTONE.getDefaultState();
        }
        if (filler.getBlock() instanceof BlockFalling)
        {
            return Blocks.DIRT.getDefaultState();
        }
        return filler;
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
        return block instanceof IBuilderUndestroyable
                 || Objects.equals(block, Blocks.BEDROCK);
    }

    /**
     * Gets a rotation from a block facing.
     *
     * @param facing the block facing.
     * @return the int rotation.
     */
    public static int getRotationFromFacing(final Direction facing)
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
    public static boolean freeToPlace(@Nullable final Block block, final BlockState blockState)
    {
        if (block == null)
        {
            return true;
        }
        for (@NotNull final BiPredicate<Block, BlockState> predicate : freeToPlaceBlocks)
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
     * @param BlockState block state to be checked.
     * @return true if is water.
     */
    public static boolean isWater(final BlockState BlockState)
    {
        return Objects.equals(BlockState, Blocks.WATER.getDefaultState())
                 || Objects.equals(BlockState, Blocks.FLOWING_WATER.getDefaultState());
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
        return BlockUtils.getItem(world.getBlockState(pos.up())) instanceof ItemSeeds;
    }

    private static Item getItem(@NotNull final BlockState blockState)
    {
        if (blockState.getBlock().equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (blockState.getBlock() instanceof BlockBanner)
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
        else if (blockState.getBlock() instanceof DoorBlock)
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
        else if (blockState.getBlock() instanceof BlockFire)
        {
            return Items.FLINT_AND_STEEL;
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
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState)
    {
        if(blockState.getBlock() instanceof IFluidBlock)
        {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);

        if (item == null)
        {
            return null;
        }

        Block block = blockState.getBlock();
        if (item instanceof BlockItem)
        {
            block = Block.getBlockFromItem(item);
        }

        return new ItemStack(item, 1, getDamageValue(block, blockState));
    }

    /**
     * Get the damage value from a block and blockState, where the block is the
     * placeable and obtainable block. The blockstate might differ from the
     * block.
     *
     * @param block      the block.
     * @param blockState the state.
     * @return the int damage value.
     */
    private static int getDamageValue(final Block block, @NotNull final BlockState blockState)
    {
        if (block instanceof BlockFarmland || blockState.getBlock() instanceof BlockFarmland)
        {
            return 0;
        }
        if (block instanceof BlockCocoa)
        {
            return EnumDyeColor.BROWN.getDyeDamage();
        }
        else if (block instanceof BlockDirt)
        {
            if (blockState.getBlock() instanceof BlockGrassPath)
            {
                return Blocks.DIRT.getDefaultState().getValue(BlockDirt.VARIANT).getMetadata();
            }
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
            //todo farmland doesn't have damage at all, sucker!
            return block.damageDropped(blockState);
        }
    }

    /**
     * Compares two blocks and checks if they are equally dirt.
     * Meaning dirt and grass are equal. But podzol and coarse dirt not.
     *
     * @param structureBlock    the block of the structure.
     * @param worldBlock        the world block.
     * @param structureMetaData the structure metadata.
     * @param worldMetadata     the world metadata.
     * @return true if equal.
     */
    public static boolean isGrassOrDirt(
                                         @NotNull final Block structureBlock, @NotNull final Block worldBlock,
                                         @NotNull final BlockState structureMetaData, @NotNull final BlockState worldMetadata)
    {
        if ((structureBlock == Blocks.DIRT || structureBlock == Blocks.GRASS) && (worldBlock == Blocks.DIRT || worldBlock == Blocks.GRASS))
        {
            if (structureBlock == Blocks.DIRT
                  && (structureMetaData.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT
                        || structureMetaData.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL))
            {
                return false;
            }

            return worldBlock != Blocks.DIRT
                     || (worldMetadata.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.COARSE_DIRT
                           && worldMetadata.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.PODZOL);
        }
        return false;
    }

    /**
     * Checks if a certain block is a pathBlock (roadBlock).
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static boolean isPathBlock(final Block block)
    {
        return block == Blocks.GRAVEL || block == Blocks.STONEBRICK || block == Blocks.GRASS_PATH;
    }
}
