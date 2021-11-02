package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract assignment module.
 */
public abstract class AbstractAssignedCitizenModule extends AbstractBuildingModule implements IAssignsCitizen, IPersistentModule
{
    /**
     * List of worker assosiated to the building.
     */
    protected final List<ICitizenData> assignedCitizen = Lists.newArrayList();

    @Override
    public boolean removeCitizen(@NotNull final ICitizenData citizen)
    {
        if (assignedCitizen.contains(citizen))
        {
            assignedCitizen.remove(citizen);
            markDirty();
            onRemoval(citizen);
            return true;
        }
        return false;
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (assignedCitizen.contains(citizen) || isFull() || citizen == null)
        {
            return false;
        }

        assignedCitizen.add(citizen);
        onAssignment(citizen);
        markDirty();
        return true;
    }

    /**
     * Action to do on assignment.
     * @param citizen the assigned citizen.
     */
    abstract void onAssignment(final ICitizenData citizen);

    /**
     * Action to do on un-assignment.
     * @param citizen the assigned citizen.
     */
    abstract void onRemoval(final ICitizenData citizen);

    @Override
    public List<ICitizenData> getAssignedCitizen()
    {
        return assignedCitizen;
    }

    @Override
    public boolean isFull()
    {
        return assignedCitizen.size() >= getModuleMax();
    }

    @Override
    public boolean hasAssignedCitizen(final ICitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }

    @Override
    @Nullable
    public List<Optional<AbstractEntityCitizen>> getAssignedEntities()
    {
        return assignedCitizen.stream().filter(Objects::nonNull).map(ICitizenData::getEntity).collect(Collectors.toList());
    }

    @Override
    public final boolean hasAssignedCitizen()
    {
        return !assignedCitizen.isEmpty();
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(assignedCitizen.size());
        for (@NotNull final ICitizenData citizen : assignedCitizen)
        {
            buf.writeInt(citizen.getId());
        }
    }
}
