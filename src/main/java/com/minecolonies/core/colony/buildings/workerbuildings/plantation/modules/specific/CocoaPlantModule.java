package com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.generic.TreeSidePlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.Stream;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#COCOA_BEANS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link TreeSidePlantModule}</li>
 * </ol>
 */
public class CocoaPlantModule extends TreeSidePlantModule
{
    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public CocoaPlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }

    @Override
    public BlockState getPlantingBlockState(final Level world, final BlockPos workPosition, final BlockState blockState)
    {
        return Stream.of(workPosition.north(), workPosition.south(), workPosition.west(), workPosition.east())
                 .filter(position -> world.getBlockState(position).getBlock() == Blocks.JUNGLE_LOG)
                 .map(position -> BlockPosUtil.directionFromDelta(position.subtract(workPosition).getX(),
                   position.subtract(workPosition).getY(),
                   position.subtract(workPosition).getZ()))
                 .map(direction -> blockState.setValue(HorizontalDirectionalBlock.FACING, direction))
                 .findFirst()
                 .orElse(blockState);
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.AXE;
    }

    @Override
    protected boolean isValidClearingBlock(final BlockState blockState)
    {
        return blockState.getBlock() != Blocks.COCOA;
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        Block block = blockState.getBlock();
        if (block instanceof CocoaBlock cocoa)
        {
            return blockState.getValue(CocoaBlock.AGE) >= 2;
        }
        return false;
    }
}