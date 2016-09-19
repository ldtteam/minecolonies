package com.minecolonies.items;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.util.BlockUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Log;
import com.minecolonies.util.SchematicWrapper;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn == null || playerIn == null || worldIn.isRemote || stack.stackSize == 0 || !isFirstPlacing(playerIn))
        {
            return EnumActionResult.FAIL;
        }

        @NotNull EnumFacing enumfacing = canShipBePlaced(worldIn, pos);
        if (enumfacing != EnumFacing.DOWN)
        {
            spawnShip(worldIn, pos, playerIn, enumfacing);
            stack.stackSize--;

            playerIn.addStat(ModAchievements.achievementGetSupply);

            return EnumActionResult.SUCCESS;
        }
        LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.supplyChestDeployer.invalid");
        return EnumActionResult.FAIL;
    }

    /**
     * Checks if the player already placed a supply chest.
     *
     * @param player the player.
     * @return boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    boolean isFirstPlacing(@NotNull EntityPlayer player)
    {
        if (Configurations.allowInfiniteSupplyChests || !player.hasAchievement(ModAchievements.achievementGetSupply))
        {
            return true;
        }
        LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.error.supplyChestAlreadyPlaced");
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
    public EnumFacing canShipBePlaced(@NotNull World world, @NotNull BlockPos pos)
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
     * @param world        world obj.
     * @param pos          coordinate clicked.
     * @param entityPlayer the player.
     */
    private void spawnShip(@NotNull World world, @NotNull BlockPos pos, EntityPlayer entityPlayer, @NotNull EnumFacing chestFacing)
    {
        world.setBlockState(pos.up(), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, chestFacing));

        placeSupplyShip(world, pos, chestFacing);

        fillChest((TileEntityChest) world.getTileEntity(pos.up()));
        //PlayerProperties.get(entityPlayer).placeSupplyChest();
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
    private boolean check(@NotNull World world, @NotNull BlockPos pos, boolean shouldCheckX, boolean isCoordPositivelyAdded)
    {
        int k = isCoordPositivelyAdded ? 1 : -1;


        int horizontalX = isCoordPositivelyAdded ? SPACE_LEFT : SPACE_RIGHT;
        int horizontalZ = isCoordPositivelyAdded ? SPACE_RIGHT : SPACE_LEFT;

        int spaceRightK = SPACE_RIGHT * k;
        int spaceLeftK = SPACE_LEFT * k;

        int widthK = WIDTH * k;
        int widthKHalf = widthK / 2;

        if (shouldCheckX)
        {
            for (int i = DISTANCE; i < WIDTH; i++)
            {
                int j = k * i;
                if (!BlockUtils.isWater(world.getBlockState(pos.add(j, 0, 0))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(j, 0, spaceRightK))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(j, 0, -spaceLeftK))))
                {
                    return false;
                }
            }
            for (int i = 0; i < LENGTH; i++)
            {
                if (!BlockUtils.isWater(world.getBlockState(pos.add(DISTANCE * k, 0, -horizontalX + i))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(widthKHalf, 0, -horizontalX + i))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(widthK, 0, -horizontalX + i))))
                {
                    return false;
                }
            }
        }
        else
        {
            for (int i = DISTANCE; i < WIDTH; i++)
            {
                int j = k * i;
                if (!BlockUtils.isWater(world.getBlockState(pos.add(0, 0, j))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(-spaceRightK, 0, j))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(spaceLeftK, 0, j))))
                {
                    return false;
                }
            }

            for (int i = 0; i < LENGTH; i++)
            {
                if (!BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, DISTANCE * k))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, widthKHalf))) ||
                      !BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, widthK))))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeSupplyShip(World world, @NotNull BlockPos pos, @NotNull EnumFacing direction)
    {
        switch (direction)
        {
            case SOUTH://North 2
                SchematicWrapper.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-11, -2, 5), 3);
                break;
            case NORTH://South 3
                SchematicWrapper.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-20, -2, -21), 1);
                break;
            case EAST://West 4
                SchematicWrapper.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(5, -2, -20), 2);
                break;
            case WEST://East 5
                SchematicWrapper.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-21, -2, -11), 0);
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
    private void fillChest(@Nullable TileEntityChest chest)
    {
        if (chest == null)
        {
            Log.getLogger().error("Supply chest tile entity was null.");
            return;
        }
        chest.setInventorySlotContents(0, new ItemStack(ModBlocks.blockHutTownHall));
        chest.setInventorySlotContents(1, new ItemStack(ModItems.buildTool));
    }
}
