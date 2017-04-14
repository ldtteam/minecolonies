package com.minecolonies.coremod.items;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
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
    private static final String SUPPLY_CAMP_STRUCTURE_NAME = "SupplyCamp";

    /**
     * Creates a new supplychest deployer. The item is not stackable.
     */
    public ItemSupplyCampDeployer()
    {
        super("supplyCampDeployer");

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
        if (worldIn == null || playerIn == null || worldIn.isRemote || stack.stackSize == 0 || !isFirstPlacing(playerIn))
        {
            return EnumActionResult.FAIL;
        }

        final EnumFacing dir = playerIn.getHorizontalFacing();
        if (spawnCamp(worldIn, pos, dir))
        {
            worldIn.setBlockState(pos.up(), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, dir));

            fillChest((TileEntityChest) worldIn.getTileEntity(pos.up()));

            stack.stackSize--;
            playerIn.addStat(ModAchievements.achievementGetSupply);

            return EnumActionResult.SUCCESS;
        }
        LanguageHandler.sendPlayerMessage(playerIn, "item.supplyCampDeployer.invalid");
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
        return checkAndPlaceSupplyCamp(world, pos, chestFacing);
    }

    private boolean checkAndPlaceSupplyCamp(final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing direction)
    {
        if(isInsideAColony(world, pos))
        {
            return false;
        }

        EnumFacing facing = direction;
        for(int i = 0; i < 4; i++)
        {
            switch (facing)
            {
                case SOUTH:
                    if(StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(-10, 0, -1), 1, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.EAST;
                    break;
                case NORTH:
                    if(StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(-6, 0, -14), 3, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.WEST;
                    break;
                case EAST:
                    if(StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME,
                            pos.add(-1, 0, -6), 0, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.NORTH;
                    break;
                case WEST:
                    if(StructureWrapper.tryToLoadAndPlaceSupplyCampWithRotation(world, SUPPLY_CAMP_STRUCTURE_NAME, pos.add(-14, 0, -10), 2, Mirror.NONE))
                    {
                        return true;
                    }
                    facing = EnumFacing.SOUTH;
                    break;
                default:
                    facing = EnumFacing.SOUTH;
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
     * @param world the world to check in.
     * @param pos the first position.
     * @return false if no colony found.
     */
    private static boolean isInsideAColony(final World world, final BlockPos pos)
    {
        return ColonyManager.isCoordinateInAnyColony(world, pos);
    }
}
