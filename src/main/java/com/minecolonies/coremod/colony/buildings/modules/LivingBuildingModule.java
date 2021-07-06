package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.AbstractCitizenAssignable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RESIDENTS;

/**
 * The class of the citizen hut.
 */
public class LivingBuildingModule extends AbstractBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        if (compound.getAllKeys().contains(TAG_RESIDENTS))
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
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        if (building.hasAssignedCitizen())
        {
            @NotNull final int[] residentIds = new int[building.getAssignedCitizen().size()];
            for (int i = 0; i < building.getAssignedCitizen().size(); ++i)
            {
                residentIds[i] = building.getAssignedCitizen().get(i).getId();
            }
            compound.putIntArray(TAG_RESIDENTS, residentIds);
        }
    }

    @Override
    public void onDestroyed()
    {
        building.getAssignedCitizen().stream()
          .filter(Objects::nonNull)
          .forEach(citizen -> citizen.setHomeBuilding(null));
    }

    @Override
    public boolean removeCitizen(@NotNull final ICitizenData citizen)
    {
        if (building.isCitizenAssigned(citizen))
        {
            ((AbstractCitizenAssignable) building).removeAssignedCitizen(citizen);
            building.getColony().getCitizenManager().calculateMaxCitizens();
            markDirty();
            citizen.setHomeBuilding(null);
            return true;
        }
        return false;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (!building.isFull() && !building.getColony().isManualHousing())
        {
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    /**
     * Looks for a homeless citizen to add to the current building Calls. {@link #assignCitizen(ICitizenData)}
     */
    private void addHomelessCitizens()
    {
        for (@NotNull final ICitizenData citizen : building.getColony().getCitizenManager().getCitizens())
        {
            if (building.isFull())
            {
                break;
            }
            moveCitizenToHut(citizen);
        }
    }

    /**
     * Moves the citizen into his new hut
     *
     * @param citizen the citizen to move
     */
    private void moveCitizenToHut(final ICitizenData citizen)
    {
        // Move the citizen to a better hut
        if (citizen.getHomeBuilding() instanceof LivingBuildingModule && citizen.getHomeBuilding().getBuildingLevel() < building.getBuildingLevel())
        {
            citizen.getHomeBuilding().removeCitizen(citizen);
        }
        if (citizen.getHomeBuilding() == null)
        {
            assignCitizen(citizen);
        }
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (citizen.getHomeBuilding() != null)
        {
            citizen.getHomeBuilding().removeCitizen(citizen);
        }

        if (!buildingAssignmentLogic(citizen))
        {
            return false;
        }

        citizen.setHomeBuilding(building);
        return true;
    }

    @Override
    public int getModuleMax()
    {
        return building.getMaxInhabitants();
    }

    private boolean buildingAssignmentLogic(final ICitizenData citizen)
    {
        if (building.getAssignedCitizen().contains(citizen) || building.isFull())
        {
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            ((AbstractCitizenAssignable) building).addAssignedCitizen(citizen);
        }

        building.getColony().getCitizenManager().calculateMaxCitizens();
        markDirty();
        return true;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        for (final Optional<AbstractEntityCitizen> entityCitizen : Objects.requireNonNull(building.getAssignedEntities()))
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(building.getAssignedCitizen().size());
        for (@NotNull final ICitizenData citizen : building.getAssignedCitizen())
        {
            buf.writeInt(citizen.getId());
        }
    }

    @Override
    public void onBuildingMove(final IBuilding oldBuilding)
    {
        final List<ICitizenData> residents = oldBuilding.getAssignedCitizen();
        for (final ICitizenData citizen : residents)
        {
            citizen.setHomeBuilding(building);
            this.assignCitizen(citizen);
        }
    }
}
