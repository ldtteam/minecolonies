package com.minecolonies.core.entity.ai.citizen.herders;

import com.minecolonies.core.colony.buildings.workerbuildings.BuildingSwineHerder;
import com.minecolonies.core.colony.jobs.JobSwineHerder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static com.minecolonies.core.entity.ai.citizen.herders.EntityAIWorkRabbitHerder.RENDER_META_CARROT;

/**
 * The AI behind the {@link JobSwineHerder} for Breeding and Killing Pigs.
 */
public class EntityAIWorkSwineHerder extends AbstractEntityAIHerder<JobSwineHerder, BuildingSwineHerder>
{
    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkSwineHerder(@NotNull final JobSwineHerder job)
    {
        super(job);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.CARROT))
        {
            renderMeta += RENDER_META_CARROT;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    public Class<BuildingSwineHerder> getExpectedBuildingClass()
    {
        return BuildingSwineHerder.class;
    }

    @Override
    public double getButcheringAttackDamage()
    {
        return Math.max(1.0, getPrimarySkillLevel() / 10.0);
    }
}
