package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Assignment module for couriers
 */
public class FarmerAssignmentModule extends CraftingWorkerBuildingModule implements IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
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
                    final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
                    if (scareCrow instanceof ScarecrowTileEntity)
                    {
                        ((ScarecrowTileEntity) scareCrow).setOwner(citizen.getId());
                    }
                }
            }
        }
    }
}
