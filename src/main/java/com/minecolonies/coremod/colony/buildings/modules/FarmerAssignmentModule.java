package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Assignment module for couriers
 */
public class FarmerAssignmentModule extends CraftingWorkerBuildingModule
{
    public FarmerAssignmentModule(final JobEntry entry,
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
        for (FarmerFieldModule module : building.getModules(FarmerFieldModule.class))
        {
            if (!module.getFarmerFields().isEmpty())
            {
                for (@NotNull final BlockPos field : module.getFarmerFields())
                {
                    if (building.getColony().getWorld().getBlockEntity(field) instanceof final ScarecrowTileEntity scarecrow)
                    {
                        scarecrow.setOwner(citizen.getId());
                    }
                }
            }
        }
    }
}
