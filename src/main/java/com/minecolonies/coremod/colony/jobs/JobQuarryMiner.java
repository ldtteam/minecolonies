package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.modules.QuarryModule;
import com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIQuarryMiner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.FIRE_RES;

/**
 * Class used for variables regarding his job.
 */
public class JobQuarryMiner extends AbstractJobStructure<EntityAIQuarryMiner, JobQuarryMiner>
{
    /**
     * Creates a new instance of the miner job.
     *
     * @param entity the entity to add the job to.
     */
    public JobQuarryMiner(final ICitizenData entity)
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
    public EntityAIQuarryMiner generateAI()
    {
        return new EntityAIQuarryMiner(this);
    }

    @Override
    public int getDiseaseModifier()
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
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource == DamageSource.LAVA || damageSource == DamageSource.IN_FIRE || damageSource == DamageSource.ON_FIRE)
        {
            return getColony().getResearchManager().getResearchEffects().getEffectStrength(FIRE_RES) > 0;
        }

        return super.ignoresDamage(damageSource);
    }
}
