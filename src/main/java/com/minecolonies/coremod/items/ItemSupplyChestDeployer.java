package com.minecolonies.coremod.items;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Class to handle the placement of the supplychest and with it the supplyship.
 */
public class ItemSupplyChestDeployer extends AbstractItemMinecolonies
{
    /**
     * Structure name and location.
     */
    private static final String SUPPLY_SHIP_STRUCTURE_NAME = Structures.SCHEMATICS_PREFIX + "/SupplyShip";

    /**
     * Offset south/west of the supply chest.
     */
    private static final int OFFSET_DISTANCE = 14;

    /**
     * Offset south/east of the supply chest.
     */
    private static final int OFFSET_LEFT = 5;

    /**
     * Offset y of the supply chest.
     */
    private static final int OFFSET_Y = -2;

    /**
     * Our guide Book.
     */
    @GameRegistry.ItemStackHolder(value = "gbook:guidebook", nbt = "{Book:\"minecolonies:book/minecolonies.xml\"}")
    public static ItemStack guideBook;

    /**
     * Creates a new supplychest deployer. The item is not stackable.
     */
    public ItemSupplyChestDeployer()
    {
        super("supplyChestDeployer");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        if (worldIn.isRemote)
        {
            placeSupplyShip(pos, playerIn.getHorizontalFacing());
        }

        return EnumActionResult.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
        {
            placeSupplyShip(null, playerIn.getHorizontalFacing());
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /**
     * Checks if the player already placed a supply chest.
     *
     * @param player the player.
     * @return boolean, returns true when player hasn't placed before, or when
     * infinite placing is on.
     */
    private static boolean isFirstPlacing(@NotNull final EntityPlayer player)
    {
        //todo might want to invent something here for that.
        /* if (Configurations.allowInfiniteSupplyChests || !player.hasAchievement(ModAchievements.achievementGetSupply))
        {
            return true;
        }*/
        return true;
        /*LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.error.supplyChestAlreadyPlaced");
        return false;*/
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
        //todo ItemStackUtils.changeSize(stack, -1);

        fillChest((TileEntityChest) world.getTileEntity(pos.up()));
    }


    private void placeSupplyShip(@Nullable final BlockPos pos, @NotNull final EnumFacing direction)
    {
        if(pos == null)
        {
            MineColonies.proxy.openBuildToolWindow(null, SUPPLY_SHIP_STRUCTURE_NAME, 0, null);
            return;
        }

        final BlockPos tempPos;
        final int rotations;
        switch (direction)
        {
            case SOUTH:
                tempPos = pos.add(OFFSET_LEFT, OFFSET_Y, OFFSET_DISTANCE);
                rotations = ROTATE_THREE_TIMES;
                break;
            case NORTH:
                tempPos = pos.add(-OFFSET_LEFT, OFFSET_Y, -OFFSET_DISTANCE);
                rotations = ROTATE_ONCE;
                break;
            case EAST:
                tempPos = pos.add(OFFSET_DISTANCE, OFFSET_Y, -OFFSET_LEFT);
                rotations = ROTATE_TWICE;
                break;
            default:
                tempPos = pos.add(-OFFSET_DISTANCE, OFFSET_Y, OFFSET_LEFT);
                rotations = ROTATE_0_TIMES;
                break;
        }
        MineColonies.proxy.openBuildToolWindow(tempPos, SUPPLY_SHIP_STRUCTURE_NAME, rotations, WindowBuildTool.FreeMode.SUPPLYSHIP);
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
     * @param facing the enum facing.
     * @return facings it can be placed at.
     */
    @NotNull
    public static boolean canShipBePlaced(@NotNull final World world, @NotNull final BlockPos pos, final BlockPos size)
    {
        for(int z = pos.getZ() - size.getZ() / 2 + 1; z < pos.getZ() + size.getZ() / 2 + 1; z++)
        {
            for(int x = pos.getX() - size.getX() / 2 + 1; x < pos.getX() + size.getX() / 2 + 1; x++)
            {
                if(!checkIfWaterAndNotInColony(world, new BlockPos(x, pos.getY() + 2, z)))
                {
                    return false;
                }
            }
        }

        for(int z = pos.getZ() - size.getZ() / 2 + 1; z < pos.getZ() + size.getZ() / 2 + 1; z++)
        {
            for(int x = pos.getX() - size.getX() / 2 + 1; x < pos.getX() + size.getX() / 2 + 1; x++)
            {
                if(!world.isAirBlock(new BlockPos(x, pos.getY() + 7, z)))
                {
                    return false;
                }
            }
        }
        return true;
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
        chest.setInventorySlotContents(2, guideBook);
    }

    /**
     * Check if the there is water at one of three positions.
     *
     * @param world the world.
     * @param pos  the first position.
     * @return true if is water
     */
    private static boolean checkIfWaterAndNotInColony(final World world, final BlockPos pos)
    {
        return BlockUtils.isWater(world.getBlockState(pos)) && notInAnyColony(world, pos);
    }

    /**
     * Check if any of the coordinates is in any colony.
     *
     * @param world the world to check in.
     * @param pos  the first position.
     * @return true if no colony found.
     */
    private static boolean notInAnyColony(final World world, final BlockPos pos)
    {
        return !ColonyManager.isCoordinateInAnyColony(world, pos);
    }
}
