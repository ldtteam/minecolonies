package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract class handling requests from the building side.
 */
public abstract class AbstractCitizenAssignable extends AbstractSchematicProvider
{
    /**
     * The colony the building belongs to.
     */
    @NotNull
    protected final Colony colony;

    /**
     * List of worker assosiated to the building.
     */
    private final List<CitizenData> assignedCitizen = new ArrayList();

    /**
     * Constructor for the abstract class which receives the position and colony.
     * @param pos
     * @param colony
     */
    public AbstractCitizenAssignable(final BlockPos pos, final Colony colony)
    {
        super(pos);
        this.colony = colony;
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        assignedCitizen.clear();
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
    }

    /**
     * Returns the colony of the building.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @NotNull
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Method to do things when a block is destroyed.
     */
    public void onDestroyed()
    {
        // EntityCitizen will detect the workplace is gone and fix up it's Entity properly
        assignedCitizen.clear();
    }

    /**
     * On tick of the server.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(final TickEvent.ServerTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * On tick of the world.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    //------------------------- Starting Assigned Citizen handling -------------------------//

    /**
     * Get the main worker of the building (the first in the list).
     *
     * @return the matching CitizenData.
     */
    public CitizenData getMainCitizen()
    {
        if (assignedCitizen.isEmpty())
        {
            return null;
        }
        return assignedCitizen.get(0);
    }

    /**
     * Returns the worker of the current building.
     *
     * @return {@link CitizenData} of the current building
     */
    public List<CitizenData> getAssignedCitizen()
    {
        return new ArrayList<>(assignedCitizen);
    }

    /**
     * Method to remove a citizen.
     *
     * @param citizen Citizen to be removed.
     */
    public void removeCitizen(final CitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            assignedCitizen.remove(citizen);
            markDirty();
        }
    }

    /**
     * Returns if the {@link CitizenData} is the same as the worker.
     *
     * @param citizen {@link CitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    public boolean isCitizenAssigned(final CitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }

    /**
     * Returns the first worker in the list.
     *
     * @return the EntityCitizen of that worker.
     */
    public Optional<EntityCitizen> getMainCitizenEntity()
    {
        if (assignedCitizen.isEmpty())
        {
            return Optional.empty();
        }
        return assignedCitizen.get(0).getCitizenEntity();
    }

    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    public final boolean hasAssignedCitizen()
    {
        return !assignedCitizen.isEmpty();
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    public List<Optional<EntityCitizen>> getAssignedEntities()
    {
        return assignedCitizen.stream().filter(Objects::nonNull).map(CitizenData::getCitizenEntity).collect(Collectors.toList());
    }

    /**
     * Assign the citizen to the current building.
     *
     * @param citizen {@link CitizenData} of the worker
     */
    public boolean assignCitizen(final CitizenData citizen)
    {
        if (assignedCitizen.contains(citizen) || isFull())
        {
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            assignedCitizen.add(citizen);
        }

        markDirty();
        return true;
    }

    /**
     * Returns whether the citizen has this as home or not.
     *
     * @param citizen Citizen to check.
     * @return True if citizen lives here, otherwise false.
     */
    public boolean hasAssignedCitizen(final CitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }


    /**
     * Checks if the building is full.
     *
     * @return true if so.
     */
    public boolean isFull()
    {
        return assignedCitizen.size() >= getMaxInhabitants();
    }

    /**
     * Returns the max amount of inhabitants.
     *
     * @return Max inhabitants.
     */
    public int getMaxInhabitants()
    {
        return 1;
    }


    //------------------------- Ending Assigned Citizen handling -------------------------//
}
