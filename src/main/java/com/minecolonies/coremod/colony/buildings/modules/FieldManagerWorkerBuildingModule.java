package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;

import java.util.function.Function;

/**
 * Assignment module for workers that work on fields.
 * Requires a {@link FieldsModule} on the job.
 */
public class FieldManagerWorkerBuildingModule extends CraftingWorkerBuildingModule
{
    public FieldManagerWorkerBuildingModule(
      final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
    }

    @Override
    void onAssignment(final ICitizenData citizen)
    {
        super.onAssignment(citizen);
        final FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
        for (IField field : fieldsModule.getFields())
        {
            field.setOwner(citizen.getId());
        }
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        super.onRemoval(citizen);
        final FieldsModule fieldsModule = building.getFirstModuleOccurance(FieldsModule.class);
        for (IField field : fieldsModule.getFields())
        {
            fieldsModule.freeField(field.getPosition());
        }
    }
}
