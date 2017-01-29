package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.ServerUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import net.java.games.input.Component;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * XXX
 */
public final class PlaceAndRemoveConstructionTape
{
    public static void placeConstructionTape(@NotNull WorkOrderBuild workOrder,@NotNull World world)
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
        wrapper.rotate(tempRotation);
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

                world.setBlockState(row1, ModBlocks.blockConstructionTape.getDefaultState());
                world.setBlockState(row2, ModBlocks.blockConstructionTape.getDefaultState());
            }
        }
        else
        {
            for (int i = x3; i <= x1; i++)
            {
                BlockPos row1 = new BlockPos(i, y, z1);
                BlockPos row2 = new BlockPos(i, y, z3);
                world.setBlockState(row1, ModBlocks.blockConstructionTape.getDefaultState());
                world.setBlockState(row2, ModBlocks.blockConstructionTape.getDefaultState());
            }
        }
        if (z1 < z3)
        {
            for (int i = z1; i <= z3; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, ModBlocks.blockConstructionTape.getDefaultState());
                world.setBlockState(row4, ModBlocks.blockConstructionTape.getDefaultState());
            }
        }
        else
        {
            for (int i = z3; i <= z1; i++)
            {
                BlockPos row3 = new BlockPos(x1, y, i);
                BlockPos row4 = new BlockPos(x3, y, i);
                world.setBlockState(row3, ModBlocks.blockConstructionTape.getDefaultState());
                world.setBlockState(row4, ModBlocks.blockConstructionTape.getDefaultState());
            }
        }
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
        wrapper.rotate(tempRotation);
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
