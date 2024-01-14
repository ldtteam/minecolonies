package com.minecolonies.core.entity.ai.citizen.baker;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.core.colony.jobs.JobBaker;
import com.minecolonies.core.entity.ai.basic.AbstractEntityAIRequestSmelter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Baker AI class.
 */
public class EntityAIWorkBaker extends AbstractEntityAIRequestSmelter<JobBaker, BuildingBaker>
{
    /**
     * Baking icon
     */
    private final static VisibleCitizenStatus BAKING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/baker.png"), "com.minecolonies.gui.visiblestatus.baker");

    /**
     * Constructor for the Baker. Defines the tasks the bakery executes.
     *
     * @param job a bakery job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingBaker> getExpectedBuildingClass()
    {
        return BuildingBaker.class;
    }

    /**
     * Returns the bakery's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }

    @Override
    protected IAIState craft()
    {
        worker.getCitizenData().setVisibleStatus(BAKING);
        return super.craft();
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return true;
    }

}
