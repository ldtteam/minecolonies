package com.minecolonies.coremod.entity.ai.citizen.archeologist;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingArcheologist;
import com.minecolonies.coremod.colony.jobs.JobArcheologist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityAIWorkArcheologist extends AbstractEntityAIStructure<JobArcheologist, BuildingArcheologist>
{
    /**
     * Creates this ai base class and set's up important things.
     * <p>
     * Always use this constructor!
     *
     * @param job the job class of the ai using this base class.
     */
    protected EntityAIWorkArcheologist(@NotNull final JobArcheologist job)
    {
        super(job);
    }

    @Override
    public Class<BuildingArcheologist> getExpectedBuildingClass()
    {
        return BuildingArcheologist.class;
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    public Tuple<BlockPos, BuildingStructureHandler.Stage> getProgressPos()
    {
        return Objects.requireNonNull(getOwnBuilding()).getProgress();
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public BlockState getSolidSubstitution(final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location).getBlockState();
    }

    @Override
    protected void executeSpecificCompleteActions()
    {

    }

    @Override
    protected boolean checkIfCanceled()
    {
        return false;
    }
}
