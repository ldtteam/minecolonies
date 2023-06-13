package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#BAMBOO}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link UpwardsGrowingPlantModule}</li>
 * </ol>
 */
public class BambooPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * The minimum height bamboo can grow to.
     */
    private static final int MIN_HEIGHT = 6;

    /**
     * The maximum height twisting vines can grow to.
     */
    private static final int MAX_HEIGHT = BambooBlock.MAX_HEIGHT;

    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public BambooPlantModule(final String fieldTag, final String workTag, final Item item)
    {
        super(fieldTag, workTag, item);
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getBlock() == Blocks.BAMBOO || blockState.getBlock() == Blocks.BAMBOO_SAPLING;
    }

    @Override
    protected int getMinimumPlantLength()
    {
        return MIN_HEIGHT;
    }

    @Override
    protected @Nullable Integer getMaximumPlantLength()
    {
        return MAX_HEIGHT;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.AXE;
    }
}