package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ASSIGNED;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_HIRING_MODE;

/**
 * Abstract assignment module.
 */
public abstract class AbstractAssignedCitizenModule extends AbstractBuildingModule implements IAssignsCitizen, IPersistentModule
{
    /**
     * List of worker assosiated to the building.
     */
    protected final List<ICitizenData> assignedCitizen = Lists.newArrayList();

    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode hiringMode = HiringMode.DEFAULT;

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
        onRemoval(citizen);
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
        return new ArrayList<>(assignedCitizen);
    }

    /**
     * Get the first citizen assigned to this module.
     * @return this citizen or null.
     */
    @Nullable
    public ICitizenData getFirstCitizen()
    {
        return assignedCitizen.isEmpty() ? null : assignedCitizen.get(0);
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
    public void serializeNBT(final CompoundTag compound)
    {
        final CompoundTag assignedCompound = new CompoundTag();
        assignedCompound.putInt(TAG_HIRING_MODE, this.hiringMode.ordinal());
        compound.put(getModuleSerializationIdentifier(), assignedCompound);
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        if (compound.contains(TAG_ASSIGNED))
        {
            this.hiringMode = HiringMode.values()[compound.getCompound(TAG_ASSIGNED).getInt(TAG_HIRING_MODE)];
        }
        else if (compound.contains(getModuleSerializationIdentifier()))
        {
            this.hiringMode = HiringMode.values()[compound.getCompound(getModuleSerializationIdentifier()).getInt(TAG_HIRING_MODE)];
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(assignedCitizen.size());
        for (@NotNull final ICitizenData citizen : assignedCitizen)
        {
            buf.writeInt(citizen.getId());
        }
        buf.writeInt(hiringMode.ordinal());
        buf.writeInt(getModuleMax());
    }

    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        this.markDirty();
    }

    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    /**
     * Get the identifier for unique serialization.
     * Use for deserialize/serialize only!
     * @return a string identifier.
     */
    protected abstract String getModuleSerializationIdentifier();
}
