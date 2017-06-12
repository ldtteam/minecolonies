package com.minecolonies.coremod.items;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to handle the placement of the supplychest and with it the supplyship.
 */
public class ItemSupplyChestDeployer extends AbstractItemMinecolonies
{
    /**
     * The space on the right side of the ship (right side of the chest).
     */
    private static final int SPACE_RIGHT = 11;
    /**
     * The space on the left side of the ship (left side of the chest).
     */
    private static final int SPACE_LEFT  = 20;
    /**
     * The total length of the ship.
     */
    private static final int LENGTH      = 32;
    /**
     * The total width of the ship.
     */
    private static final int WIDTH       = 20;
    /**
     * The distance between the ship and the chest.
     */
    private static final int DISTANCE    = 4;

    private static final String SUPPLY_SHIP_STRUCTURE_NAME = Structures.SCHEMATICS_PREFIX + "/SupplyShip";

    /**
     * Offset south/west of the supply camp.
     */
    private static final int OFFSET_SOUTH_WEST = -11;

    /**
     * Offset south/east of the supply camp.
     */
    private static final int OFFSET_SOUTH_EAST = 5;

    /**
     * Offset north/east of the supply camp.
     */
    private static final int OFFSET_NORTH_EAST = -20;

    /**
     * Offset north/west of the supply camp.
     */
    private static final int OFFSET_NORTH_WEST = -21;

    /**
     * Offset y of the supplyCamp
     */
    private static final int OFFSET_Y = -2;

    /**
     * Creates a new supplychest deployer. The item is not stackable.
     */
    public ItemSupplyChestDeployer()
    {
        super("supplyChestDeployer");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    /**
     * This method will be executed on placement of the ship.
     * If the ship can be placed at the current position the function will execute successfully.
     *
     * @param stack    the item.
     * @param playerIn the player placing.
     * @param worldIn  the world.
     * @param pos      the position.
     * @param hand     the hand used
     * @param facing   the direction it faces (not used).
     * @param hitX     the hitBox x position (not used).
     * @param hitY     the hitBox y position (not used).
     * @param hitZ     the hitBox z position (not used).
     * @return if the chest has been successfully placed.
     */
    @NotNull
    @Override
    public EnumActionResult onItemUse(
            final ItemStack stack,
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        if (worldIn == null || playerIn == null || worldIn.isRemote || ItemStackUtils.isEmpty(stack) || !isFirstPlacing(playerIn))
        {
            return EnumActionResult.FAIL;
        }

        @NotNull final EnumFacing enumfacing = canShipBePlaced(worldIn, pos);
        if (enumfacing != EnumFacing.DOWN)
        {
            spawnShip(worldIn, pos, enumfacing);
            ItemStackUtils.changeSize(stack, -1 );

            playerIn.addStat(ModAchievements.achievementGetSupply);

            return EnumActionResult.SUCCESS;
        }
        LanguageHandler.sendPlayerMessage(playerIn, "item.supplyChestDeployer.invalid");
        return EnumActionResult.FAIL;
    }

    /**
     * Checks if the player already placed a supply chest.
     *
     * @param player the player.
     * @return boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    private static boolean isFirstPlacing(@NotNull final EntityPlayer player)
    {
        if (Configurations.allowInfiniteSupplyChests || !player.hasAchievement(ModAchievements.achievementGetSupply))
        {
            return true;
        }
        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.error.supplyChestAlreadyPlaced");
        return false;
    }

    /**
     * Checks if the ship can be placed and returns the direction it can face.
     * <pre>
     * 0: cannot be placed.
     * 2: can be placed at north.
     * 3: can be placed at south.
     * 4: can be placed at west.
     * 5: can be placed at east.
     * </pre>
     *
     * @param world world obj.
     * @param pos   coordinate clicked.
     * @return facings it can be placed at.
     */
    @NotNull
    private static EnumFacing canShipBePlaced(@NotNull final World world, @NotNull final BlockPos pos)
    {
        if (check(world, pos.west(), true, false))
        {
            return EnumFacing.WEST;
        }
        else if (check(world, pos.east(), true, true))
        {
            return EnumFacing.EAST;
        }
        else if (check(world, pos.south(), false, true))
        {
            return EnumFacing.SOUTH;
        }
        else if (check(world, pos.north(), false, false))
        {
            return EnumFacing.NORTH;
        }
        return EnumFacing.DOWN;
    }

    /**
     * Spawns the ship and supply chest.
     *
     * @param world world obj.
     * @param pos   coordinate clicked.
     */
    private void spawnShip(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing chestFacing)
    {
        world.setBlockState(pos.up(), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, chestFacing));

        placeSupplyShip(world, pos, chestFacing);

        fillChest((TileEntityChest) world.getTileEntity(pos.up()));
    }

    /**
     * Checks the area for the ship to be placed.
     *
     * @param world                  world obj.
     * @param pos                    coordinate clicked.
     * @param shouldCheckX           boolean whether the x-sides should be checks.
     * @param isCoordPositivelyAdded boolean whether the x or z side should be check on the positive side (true) or negative  side (false).
     * @return whether the space in the I shape is free or not.
     */
    private static boolean check(@NotNull final World world, @NotNull final BlockPos pos, final boolean shouldCheckX, final boolean isCoordPositivelyAdded)
    {
        final int k = isCoordPositivelyAdded ? 1 : -1;

        final int spaceRightK = SPACE_RIGHT * k;
        final int spaceLeftK = SPACE_LEFT * k;
        final int widthK = WIDTH * k;

        if (shouldCheckX)
        {
            return checkX(world, pos, k, spaceRightK, spaceLeftK, widthK, isCoordPositivelyAdded);
        }

        return checkZ(world, pos, k, spaceRightK, spaceLeftK, widthK, isCoordPositivelyAdded);
    }

    private void placeSupplyShip(final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing direction)
    {
        switch (direction)
        {

            case SOUTH:
                StructureWrapper.loadAndPlaceStructureWithRotation(world, SUPPLY_SHIP_STRUCTURE_NAME, pos.add(OFFSET_SOUTH_WEST, OFFSET_Y, OFFSET_SOUTH_EAST),
                        Constants.ROTATE_THREE_TIMES, Mirror.NONE);
                break;
            case NORTH:
                StructureWrapper.loadAndPlaceStructureWithRotation(world, SUPPLY_SHIP_STRUCTURE_NAME, pos.add(OFFSET_NORTH_EAST, OFFSET_Y, OFFSET_NORTH_WEST),
                        Constants.ROTATE_ONCE, Mirror.NONE);
                break;
            case EAST:
                StructureWrapper.loadAndPlaceStructureWithRotation(world, SUPPLY_SHIP_STRUCTURE_NAME, pos.add(OFFSET_SOUTH_EAST, OFFSET_Y, OFFSET_NORTH_EAST),
                        Constants.ROTATE_TWICE, Mirror.NONE);
                break;
            case WEST:
                StructureWrapper.loadAndPlaceStructureWithRotation(world, SUPPLY_SHIP_STRUCTURE_NAME, pos.add(OFFSET_NORTH_WEST, OFFSET_Y, OFFSET_SOUTH_WEST),
                        Constants.ROTATE_0_TIMES, Mirror.NONE);
                break;
            default:
                break;
        }
    }

    /**
     * Fills the content of the supplychest with the buildTool and townHall.
     *
     * @param chest the chest to fill.
     */
    private static void fillChest(@Nullable final TileEntityChest chest)
    {
        if (chest == null)
        {
            Log.getLogger().error("Supply chest tile entity was null.");
            return;
        }
        chest.setInventorySlotContents(0, new ItemStack(ModBlocks.blockHutTownHall));
        chest.setInventorySlotContents(1, new ItemStack(ModItems.buildTool));
    }

    /**
     * Checks the x axis for water.
     *
     * @param world                  the world.
     * @param pos                    the starting pos.
     * @param k                      the symbol.
     * @param spaceRightK            the space to the left.
     * @param spaceLeftK             the space to the right.
     * @param widthK                 the width.
     * @param isCoordPositivelyAdded if is positive or not.
     * @return true if it can be placed.
     */
    private static boolean checkX(
            final World world,
            final BlockPos pos,
            final int k,
            final int spaceRightK,
            final int spaceLeftK,
            final int widthK,
            final boolean isCoordPositivelyAdded)
    {
        for (int i = DISTANCE; i < WIDTH; i++)
        {
            final int j = k * i;
            if (!checkIfWaterAndNotInColony(world, pos.add(j, 0, 0), pos.add(j, 0, spaceRightK), pos.add(j, 0, -spaceLeftK)))
            {
                return false;
            }
        }

        final int horizontalX = isCoordPositivelyAdded ? SPACE_LEFT : SPACE_RIGHT;
        final int widthKHalf = widthK / 2;

        for (int i = 0; i < LENGTH; i++)
        {
            if (!checkIfWaterAndNotInColony(world, pos.add(DISTANCE * k, 0, -horizontalX + i), pos.add(widthKHalf, 0, -horizontalX + i), pos.add(widthK, 0, -horizontalX + i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the z axis for water.
     *
     * @param world                  the world.
     * @param pos                    the starting pos.
     * @param k                      the symbol.
     * @param spaceRightK            the space to the left.
     * @param spaceLeftK             the space to the right.
     * @param widthK                 the width.
     * @param isCoordPositivelyAdded if is positive or not.
     * @return true if it can be placed.
     */
    private static boolean checkZ(
            final World world,
            final BlockPos pos,
            final int k,
            final int spaceRightK,
            final int spaceLeftK,
            final int widthK,
            final boolean isCoordPositivelyAdded)
    {
        for (int i = DISTANCE; i < WIDTH; i++)
        {
            final int j = k * i;
            if (!checkIfWaterAndNotInColony(world, pos.add(0, 0, j), pos.add(-spaceRightK, 0, j), pos.add(spaceLeftK, 0, j)))
            {
                return false;
            }
        }

        final int horizontalZ = isCoordPositivelyAdded ? SPACE_RIGHT : SPACE_LEFT;

        for (int i = 0; i < LENGTH; i++)
        {
            if (!checkIfWaterAndNotInColony(world, pos.add(-horizontalZ + i, 0, DISTANCE * k), pos.add(-horizontalZ + i, 0, DISTANCE * k), pos.add(-horizontalZ + i, 0, widthK)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the there is water at one of three positions.
     *
     * @param world the world.
     * @param pos1  the first position.
     * @param pos2  the second position.
     * @param pos3  the third position.
     * @return true if is water
     */
    private static boolean checkIfWaterAndNotInColony(final World world, final BlockPos pos1, final BlockPos pos2, final BlockPos pos3)
    {
        return BlockUtils.isWater(world.getBlockState(pos1)) && BlockUtils.isWater(world.getBlockState(pos2)) && BlockUtils.isWater(world.getBlockState(pos3))
                && notInAnyColony(world, pos1, pos2, pos3);
    }

    /**
     * Check if any of the coordinates is in any colony.
     *
     * @param world the world to check in.
     * @param pos1  the first position.
     * @param pos2  the second position.
     * @param pos3  the third position.
     * @return true if no colony found.
     */
    private static boolean notInAnyColony(final World world, final BlockPos pos1, final BlockPos pos2, final BlockPos pos3)
    {
        return !ColonyManager.isCoordinateInAnyColony(world, pos1) && !ColonyManager.isCoordinateInAnyColony(world, pos2) && !ColonyManager.isCoordinateInAnyColony(world, pos3);
    }
}
