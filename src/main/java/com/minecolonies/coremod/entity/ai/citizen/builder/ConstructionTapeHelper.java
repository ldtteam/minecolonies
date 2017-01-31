package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to place and remove constructionTapes from the buildings.
 */
public final class ConstructionTapeHelper
{
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;

    /**
     * Private Constructor to hide implicit one.
     */
    private ConstructionTapeHelper()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Proxy to place the tape also with the building only.
     * @param building the building.
     * @param nextLevel the next level.
     * @param world the world.
     */
    public static void placeConstructionTape(@NotNull AbstractBuilding building, @NotNull World world)
    {
        placeConstructionTape(new WorkOrderBuild(building, 1), world);
    }


    public static void placeConstructionTape(@NotNull WorkOrderBuild workOrder, @NotNull World world)
    {
        final StructureWrapper wrapper = new StructureWrapper(world, workOrder.getStructureName());
        final BlockPos pos = workOrder.getBuildingLocation();
        int tempRotation = 0;

        if (workOrder.getRotation() == 0 && !(workOrder instanceof WorkOrderBuildDecoration))
        {
            final IBlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                tempRotation = BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        else
        {
            tempRotation = workOrder.getRotation();
        }

        wrapper.rotate(tempRotation, world, workOrder.getBuildingLocation());
        wrapper.setPosition(pos);
        int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
        int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
        int x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
        int z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
        int y  = wrapper.getPosition().getY();

        if (x1 < x3)
        {
            for (int i = x1; i <= x3; i++)
            {
                BlockPos row1 = new BlockPos(i, y, z1);
                BlockPos row2 = new BlockPos(i, y, z3);

                world.setBlockState(row1, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.SOUTH));
                world.setBlockState(row2, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.NORTH));
            }
        }
        else
        {
            for (int i = x3; i <= x1; i++)
            {
                BlockPos row1 = new BlockPos(i, y, z1);
                BlockPos row2 = new BlockPos(i, y, z3);
                world.setBlockState(row1, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.SOUTH));
                world.setBlockState(row2, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.NORTH));
            }
        }
        if (z1 < z3)
        {
            for (int i = z1; i <= z3; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.EAST));
                world.setBlockState(row4, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.WEST));
            }
        }
        else
        {
            for (int i = z3; i <= z1; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.EAST));
                world.setBlockState(row4, ModBlocks.blockConstructionTape.getDefaultState().withProperty(FACING, EnumFacing.WEST));
            }
        }
        BlockPos corner1 = new BlockPos(x1, y, z1);
        BlockPos corner2 = new BlockPos(x1, y, z3);
        BlockPos corner3 = new BlockPos(x3, y, z1);
        BlockPos corner4 = new BlockPos(x3, y, z3);
        world.setBlockState(corner1, ModBlocks.blockConstructionTapeC.getDefaultState().withProperty(FACING, EnumFacing.SOUTH));
        world.setBlockState(corner2, ModBlocks.blockConstructionTapeC.getDefaultState().withProperty(FACING, EnumFacing.EAST));
        world.setBlockState(corner3, ModBlocks.blockConstructionTapeC.getDefaultState().withProperty(FACING, EnumFacing.WEST));
        world.setBlockState(corner4, ModBlocks.blockConstructionTapeC.getDefaultState().withProperty(FACING, EnumFacing.NORTH));
    }




    public static void removeConstructionTape(@NotNull WorkOrderBuild workOrder,@NotNull World world)
    {
        final StructureWrapper wrapper = new StructureWrapper(world, (workOrder.getStructureName()));
        final BlockPos pos = workOrder.getBuildingLocation();
        int tempRotation = 0;
        if (workOrder.getRotation() == 0 && !(workOrder instanceof WorkOrderBuildDecoration))
        {
            final IBlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                tempRotation = BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        else
        {
            tempRotation = workOrder.getRotation();
        }
        wrapper.rotate(tempRotation, world, workOrder.getBuildingLocation());
        wrapper.setPosition(pos);
        int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
        int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
        int x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
        int z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
        int y  = wrapper.getPosition().getY();
        if (x1 < x3)
        {
            for (int i = x1; i <= x3; i++)
            {
                BlockPos row1 = new BlockPos(i, y, z1);
                BlockPos row2 = new BlockPos(i, y, z3);
                world.setBlockState(row1, Blocks.AIR.getDefaultState());
                world.setBlockState(row2, Blocks.AIR.getDefaultState());
            }
        }
        else
        {
            for (int i = x3; i <= x1; i++)
            {
                BlockPos row1 = new BlockPos(i, y, z1);
                BlockPos row2 = new BlockPos(i, y, z3);
                world.setBlockState(row1, Blocks.AIR.getDefaultState());
                world.setBlockState(row2, Blocks.AIR.getDefaultState());
            }
        }
        if (z1 < z3)
        {
            for (int i = z1; i <= z3; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, Blocks.AIR.getDefaultState());
                world.setBlockState(row4, Blocks.AIR.getDefaultState());
            }
        }
        else
        {
            for (int i = z3; i <= z1; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, Blocks.AIR.getDefaultState());
                world.setBlockState(row4, Blocks.AIR.getDefaultState());
            }
        }
    }
}
