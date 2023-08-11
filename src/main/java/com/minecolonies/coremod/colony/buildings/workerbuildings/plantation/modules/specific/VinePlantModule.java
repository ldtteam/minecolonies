package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.PercentageHarvestPlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#VINE}.
 * All possible positions of the vines should be tagged with the vine tag.
 * The planter will automatically plant an X amount of vines down, depending on the amount of tagged positions.
 * After that any excess that will grow will be harvested.
 * The planter will make an attempt to not plant the vines next to one another as much as possible.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link PercentageHarvestPlantModule}</li>
 * </ol>
 */
public class VinePlantModule extends PercentageHarvestPlantModule
{
    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public VinePlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
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
        return blockState
                 .setValue(VineBlock.UP, Boolean.valueOf(VineBlock.isAcceptableNeighbour(world, workPosition.above(), Direction.DOWN)))
                 .setValue(VineBlock.NORTH, Boolean.valueOf(VineBlock.isAcceptableNeighbour(world, workPosition.north(), Direction.SOUTH)))
                 .setValue(VineBlock.SOUTH, Boolean.valueOf(VineBlock.isAcceptableNeighbour(world, workPosition.south(), Direction.NORTH)))
                 .setValue(VineBlock.WEST, Boolean.valueOf(VineBlock.isAcceptableNeighbour(world, workPosition.west(), Direction.EAST)))
                 .setValue(VineBlock.EAST, Boolean.valueOf(VineBlock.isAcceptableNeighbour(world, workPosition.east(), Direction.WEST)));
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.SHEARS;
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.is(Blocks.VINE);
    }

    @Override
    protected int getMinimumPlantPercentage()
    {
        return 20;
    }
}