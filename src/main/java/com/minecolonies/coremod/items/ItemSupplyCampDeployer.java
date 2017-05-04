package com.minecolonies.coremod.items;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
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
public class ItemSupplyCampDeployer extends AbstractItemMinecolonies
{
    /**
     * The name of the structure
     */
    private static final String SUPPLY_CAMP_STRUCTURE_NAME = Structures.SCHEMATICS_PREFIX + "/SupplyCamp";

    /**
     * Offset south/west of the supply camp.
     */
    private static final int OFFSET_SOUTH_WEST = -10;

    /**
     * Offset south/east of the supply camp.
     */
    private static final int OFFSET_SOUTH_EAST = -1;

    /**
     * Offset north/east of the supply camp.
     */
    private static final int OFFSET_NORTH_EAST = -6;

    /**
     * Offset north/west of the supply camp.
     */
    private static final int OFFSET_NORTH_WEST = -14;

    /**
     * Amount of directions to try to place the supply camp.
     */
    private static final int CHECK_X_DIRECTIONS = 4;

    /**
     * Creates a new supplychest deployer. The item is not stackable.
     */
    public ItemSupplyCampDeployer()
    {
        super("supplyCampDeployer");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(
            final EntityPlayer player,
            final World worldIn,
            final BlockPos pos,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        if(worldIn == null || player == null)
        {
            return EnumActionResult.FAIL;
        }

        final ItemStack stack = player.getHeldItem(hand);
        if (worldIn.isRemote || stack.getCount() == 0 || !isFirstPlacing(player))
        {
            return EnumActionResult.FAIL;
        }

        final EnumFacing dir = player.getHorizontalFacing();
        if (spawnCamp(worldIn, pos, dir))
        {
            worldIn.setBlockState(pos.up(), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, dir));

            fillChest((TileEntityChest) worldIn.getTileEntity(pos.up()));

            stack.setCount(stack.getCount()-1);
            player.addStat(ModAchievements.achievementGetSupply);

            return EnumActionResult.SUCCESS;
        }
        LanguageHandler.sendPlayerMessage(player, "item.supplyCampDeployer.invalid");
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
     * Spawns the ship and supply chest.
     *
     * @param world world obj.
     * @param pos   coordinate clicked.
     */
    private boolean spawnCamp(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing chestFacing)
    {
        if (isInsideAColony(world, pos))
        {
            return false;
        }
        return checkAndPlaceSupplyCamp(world, pos, chestFacing);
    }

    private boolean checkAndPlaceSupplyCamp(final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing direction)
    {
        EnumFacing facing = direction;
        for (int i = 0; i < CHECK_X_DIRECTIONS; i++)
        {
            switch (facing)
            {
                case NORTH:
                    if (StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(OFFSET_NORTH_EAST, 0, OFFSET_NORTH_WEST), Constants.ROTATE_THREE_TIMES, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.WEST;
                    break;
                case EAST:
                    if (StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(OFFSET_SOUTH_EAST, 0, OFFSET_NORTH_EAST), Constants.ROTATE_0_TIMES, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.NORTH;
                    break;
                case WEST:
                    if (StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(OFFSET_NORTH_WEST, 0, OFFSET_SOUTH_WEST), Constants.ROTATE_TWICE, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.SOUTH;
                    break;
                default:
                    if (StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(OFFSET_SOUTH_WEST, 0, OFFSET_SOUTH_EAST), Constants.ROTATE_ONCE, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.EAST;
            }
        }
        return false;
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
     * Check if any of the coordinates is in any colony.
     *
     * @param world the world to check in.
     * @param pos   the first position.
     * @return false if no colony found.
     */
    private static boolean isInsideAColony(final World world, final BlockPos pos)
    {
        return ColonyManager.isCoordinateInAnyColony(world, pos);
    }
}
