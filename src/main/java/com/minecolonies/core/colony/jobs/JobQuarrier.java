package com.minecolonies.core.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.jobs.IJobWithExternalWorkStations;
import com.minecolonies.core.colony.buildings.modules.QuarryModule;
import com.minecolonies.core.entity.ai.workers.production.EntityAIQuarrier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.FIRE_DAMAGE_PREDICATE;
import static com.minecolonies.api.research.util.ResearchConstants.FIRE_RES;

/**
 * Special quarrier job. Defines miner model and specialized job behaviour.
 */
public class JobQuarrier extends AbstractJobStructure<EntityAIQuarrier, JobQuarrier> implements IJobWithExternalWorkStations
{
    /**
     * Creates a new instance of the miner job.
     *
     * @param entity the entity to add the job to.
     */
    public JobQuarrier(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.MINER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIQuarrier generateAI()
    {
        return new EntityAIQuarrier(this);
    }

    @Override
    public double getDiseaseModifier()
    {
        return 2;
    }

    /**
     * Finds the quarry our miner is assigned to
     *
     * @return quarry building or null
     */
    public IBuilding findQuarry()
    {
        for (final IBuilding building : getColony().getBuildingManager().getBuildings().values())
        {
            if (building.getBuildingType().getRegistryName().getPath().contains("quarry") && building.getFirstModuleOccurance(QuarryModule.class).hasAssignedCitizen(getCitizen()))
            {
                return building;
            }
        }

        return null;
    }

    @Override
    public boolean assignTo(final IAssignsJob module)
    {
        if (module == null || !module.getJobEntry().equals(getJobRegistryEntry()))
        {
            return false;
        }

        if (module instanceof QuarryModule)
        {
            return true;
        }

        return super.assignTo(module);
    }

    @Override
    public List<IBuilding> getWorkStations()
    {
        final IBuilding building = findQuarry();
        return building == null ? Collections.emptyList() : ImmutableList.of(building);
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource.typeHolder().is(FIRE_DAMAGE_PREDICATE))
        {
            return getColony().getResearchManager().getResearchEffects().getEffectStrength(FIRE_RES) > 0;
        }

        return super.ignoresDamage(damageSource);
    }
}
