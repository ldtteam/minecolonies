package com.minecolonies.core.colony.jobs.guard;

import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.guard.EntityAIRange;

/**
 * The Ranger's Job class
 *
 * @author Asherslab
 */
public class JobRanger extends AbstractJobGuard<JobRanger>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobRanger(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EntityAIRange generateGuardAI()
    {
        return new EntityAIRange(this);
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ARCHER_GUARD_ID;
    }

    /**
     * Equipment type of this guard.
     * @return the type.
     */
    public EquipmentTypeEntry getEquipmentType()
    {
        // Default bow.
        return ModEquipmentTypes.bow.get();
    }
}
