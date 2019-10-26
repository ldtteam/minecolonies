package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ICitizenAssignable;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.nbt.NBTTagCompound;
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
public abstract class AbstractCitizenAssignable extends AbstractSchematicProvider implements ICitizenAssignable
{
    /**
     * The colony the building belongs to.
     */
    @NotNull
    protected final IColony colony;

    /**
     * List of worker assosiated to the building.
     */
    private final List<ICitizenData> assignedCitizen = Lists.newArrayList();

    /**
     * Constructor for the abstract class which receives the position and colony.
     * @param pos
     * @param colony
     */
    public AbstractCitizenAssignable(final BlockPos pos, final IColony colony)
    {
        super(pos);
        this.colony = colony;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        assignedCitizen.clear();
    }

    /**
     * Returns the colony of the building.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @Override
    @NotNull
    public IColony getColony()
    {
        return colony;
    }

    /**
     * Method to do things when a block is destroyed.
     */
    @Override
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
    @Override
    public void onServerTick(final TickEvent.ServerTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * On tick of the colony.
     *
     * @param colony the colony which ticks.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        // Can be overridden by other buildings.
    }

    //------------------------- Starting Assigned Citizen handling -------------------------//

    /**
     * Get the main worker of the building (the first in the list).
     *
     * @return the matching CitizenData.
     */
    @Override
    public ICitizenData getMainCitizen()
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
    @Override
    public List<ICitizenData> getAssignedCitizen()
    {
        return new ArrayList<>(assignedCitizen);
    }

    /**
     * Method to remove a citizen.
     *
     * @param citizen Citizen to be removed.
     */
    @Override
    public void removeCitizen(final ICitizenData citizen)
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
    @Override
    public boolean isCitizenAssigned(final ICitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }

    /**
     * Returns the first worker in the list.
     *
     * @return the EntityCitizen of that worker.
     */
    @Override
    public Optional<AbstractEntityCitizen> getMainCitizenEntity()
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
    @Override
    public final boolean hasAssignedCitizen()
    {
        return !assignedCitizen.isEmpty();
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Override
    @Nullable
    public List<Optional<AbstractEntityCitizen>> getAssignedEntities()
    {
        return assignedCitizen.stream().filter(Objects::nonNull).map(ICitizenData::getCitizenEntity).collect(Collectors.toList());
    }

    /**
     * Assign the citizen to the current building.
     *
     * @param citizen {@link ICitizenData} of the worker
     */
    @Override
    public boolean assignCitizen(final ICitizenData citizen)
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
    @Override
    public boolean hasAssignedCitizen(final ICitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }


    /**
     * Checks if the building is full.
     *
     * @return true if so.
     */
    @Override
    public boolean isFull()
    {
        return assignedCitizen.size() >= getMaxInhabitants();
    }

    /**
     * Returns the max amount of inhabitants.
     *
     * @return Max inhabitants.
     */
    @Override
    public int getMaxInhabitants()
    {
        return 1;
    }


    //------------------------- Ending Assigned Citizen handling -------------------------//
}
