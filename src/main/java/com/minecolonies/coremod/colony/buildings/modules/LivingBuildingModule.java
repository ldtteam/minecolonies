package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.IAssignsCitizen;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LIVING_RESIDENTS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RESIDENTS;

/**
 * The living module for citizen to call their home.
 */
public class LivingBuildingModule extends AbstractAssignedCitizenModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
                if (citizen != null)
                {
                    assignCitizen(citizen);
                }
            }
        }
        else if (compound.contains(TAG_LIVING_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_LIVING_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
                if (citizen != null)
                {
                    assignCitizen(citizen);
                }
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        super.serializeNBT(compound);
        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            compound.putIntArray(TAG_LIVING_RESIDENTS, residentIds);
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (!isFull() && ((this.getHiringMode() == HiringMode.DEFAULT && !building.getColony().isManualHousing()) || getHiringMode() == HiringMode.AUTO))
        {
            // 'Capture' as many citizens into this house as possible
            for (@NotNull final ICitizenData citizen : building.getColony().getCitizenManager().getCitizens())
            {
                if (isFull())
                {
                    break;
                }

                if (citizen.getHomeBuilding() == null)
                {
                    assignCitizen(citizen);
                }
            }
        }
    }

    @Override
    void onAssignment(final ICitizenData citizen)
    {
        citizen.setHomeBuilding(building);
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        citizen.setHomeBuilding(null);
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public int getModuleMax()
    {
        return building.getBuildingLevel();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        for (final Optional<AbstractEntityCitizen> entityCitizen : Objects.requireNonNull(getAssignedEntities()))
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    protected String getModuleSerializationIdentifier()
    {
        return "living";
    }
}
