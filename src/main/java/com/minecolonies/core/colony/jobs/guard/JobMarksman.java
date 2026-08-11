package com.minecolonies.core.colony.jobs.guard;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;

/**
 * The Markman's Job class
 *
 * @author Asherslab
 */
public class JobMarksman extends JobRanger
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobMarksman(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EquipmentTypeEntry getEquipmentType()
    {
        return ModEquipmentTypes.crossbow.get();
    }
}
