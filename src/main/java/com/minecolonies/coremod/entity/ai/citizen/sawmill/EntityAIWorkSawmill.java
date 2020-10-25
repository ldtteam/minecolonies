package com.minecolonies.coremod.entity.ai.citizen.sawmill;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSawmill;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkSawmill extends AbstractEntityAICrafting<JobSawmill, BuildingSawmill>
{
    /**
     * Crafting icon
     */
    private final static VisibleCitizenStatus CRAFTING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/sawmill.png"), "com.minecolonies.gui.visiblestatus.sawmill");

    /**
     * Initialize the sawmill and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkSawmill(@NotNull final JobSawmill sawmill)
    {
        super(sawmill);
    }

    @Override
    public Class<BuildingSawmill> getExpectedBuildingClass()
    {
        return BuildingSawmill.class;
    }

    @Override
    protected IAIState craft()
    {
        worker.getCitizenData().setVisibleStatus(CRAFTING);
        return super.craft();
    }
}
