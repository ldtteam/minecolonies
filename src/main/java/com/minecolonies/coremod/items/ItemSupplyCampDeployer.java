package com.minecolonies.coremod.items;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Class to handle the placement of the supplychest and with it the supplycamp.
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
    private static final int OFFSET_DISTANCE = 5;

    /**
     * Offset south/east of the supply camp.
     */
    private static final int OFFSET_LEFT = 0;

    /**
     * Offset y of the supply camp.
     */
    private static final int OFFSET_Y = 0;

    /**
     * Creates a new supplycamp deployer. The item is not stackable.
     */
    public ItemSupplyCampDeployer()
    {
        super("supplyCampDeployer");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

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
        if (worldIn.isRemote)
        {
            placeSupplyCamp(pos, playerIn.getHorizontalFacing());
        }

        return EnumActionResult.FAIL;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack itemStackIn, final World worldIn, final EntityPlayer playerIn, final EnumHand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
        {
            placeSupplyCamp(null, playerIn.getHorizontalFacing());
        }

        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
    
    private void placeSupplyCamp(@Nullable final BlockPos pos, @NotNull final EnumFacing direction)
    {
        if(pos == null)
        {
            MineColonies.proxy.openBuildToolWindow(null, SUPPLY_CAMP_STRUCTURE_NAME, 0, null);
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
        MineColonies.proxy.openBuildToolWindow(tempPos, SUPPLY_CAMP_STRUCTURE_NAME, rotations, WindowBuildTool.FreeMode.SUPPLYCAMP);
    }

    /**
     * Checks if the camp can be placed.
     * @param world the world.
     * @param pos the position.
     * @param size the size.
     * @return true if so.
     */
    @NotNull
    public static boolean canCampBePlaced(@NotNull final World world, @NotNull final BlockPos pos, final BlockPos size)
    {
        for(int z = pos.getZ() - size.getZ() / 2 + 1; z < pos.getZ() + size.getZ() / 2 + 1; z++)
        {
            for(int x = pos.getX() - size.getX() / 2 + 1; x < pos.getX() + size.getX() / 2 + 1; x++)
            {
                if(!checkIfSolidAndNotInColony(world, new BlockPos(x, pos.getY(), z)))
                {
                    return false;
                }
            }
        }

        for(int z = pos.getZ() - size.getZ() / 2 + 1; z < pos.getZ() + size.getZ() / 2 + 1; z++)
        {
            for(int x = pos.getX() - size.getX() / 2 + 1; x < pos.getX() + size.getX() / 2 + 1; x++)
            {
                if(!world.isAirBlock(new BlockPos(x, pos.getY()+1, z)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the there is a solid block at a position and it's not in a colony.
     *
     * @param world the world.
     * @param pos  the position.
     * @return true if is water
     */
    private static boolean checkIfSolidAndNotInColony(final World world, final BlockPos pos)
    {
        return world.getBlockState(pos).getMaterial().isSolid() && notInAnyColony(world, pos);
    }

    /**
     * Check if a coordinate is in any colony.
     *
     * @param world the world to check in.
     * @param pos  the position.
     * @return true if no colony found.
     */
    private static boolean notInAnyColony(final World world, final BlockPos pos)
    {
        return !ColonyManager.isCoordinateInAnyColony(world, pos);
    }
}
