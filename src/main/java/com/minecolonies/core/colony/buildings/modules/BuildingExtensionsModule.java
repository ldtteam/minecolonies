package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract class to list all extensions (assigned) to a building.
 */
public abstract class BuildingExtensionsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingModule, ITickingModule
{
    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";
    private static final String TAG_CURRENT_EXTENSION = "currex";

    /**
     * A map of building extensions, along with their unix timestamp of when they can next be checked again.
     */
    private final Map<IBuildingExtension.ExtensionId, Integer> checkedExtensions = new Object2IntOpenHashMap<>();

    /**
     * The building extension the citizen is currently working on.
     */
    @Nullable
    private IBuildingExtension.ExtensionId currentExtensionId;

    /**
     * Building extensions should be assigned manually to the citizen.
     */
    private boolean shouldAssignManually = false;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        claimExtensions();
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);
        final ListTag listTag = compound.getList(TAG_BUILDING_EXTENSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); ++i)
        {
            final CompoundTag tag = listTag.getCompound(i);
            checkedExtensions.put(IBuildingExtension.ExtensionId.deserializeNBT(tag.getCompound(TAG_ID)), compound.getInt(TAG_DAY));
        }
        if (compound.contains(TAG_CURRENT_EXTENSION))
        {
            currentExtensionId = IBuildingExtension.ExtensionId.deserializeNBT(compound.getCompound(TAG_CURRENT_EXTENSION));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        final ListTag listTag = new ListTag();
        for (final Map.Entry<IBuildingExtension.ExtensionId, Integer> entry : checkedExtensions.entrySet())
        {
            final CompoundTag listEntry = new CompoundTag();
            compound.put(TAG_ID, entry.getKey().serializeNBT());
            listEntry.putLong(TAG_DAY, entry.getValue());
            listTag.add(listEntry);
        }
        compound.put(TAG_LIST, listTag);
        if (currentExtensionId != null)
        {
            compound.put(TAG_CURRENT_EXTENSION, currentExtensionId.serializeNBT());
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(shouldAssignManually);
        buf.writeInt(getMaxExtensionCount());
    }

    /**
     * Getter to obtain the maximum building extension count.
     *
     * @return an integer stating the maximum building extension count.
     */
    protected abstract int getMaxExtensionCount();

    /**
     * Get the class type which is expected for the building extension to have.
     *
     * @return the class type.
     */
    public abstract Class<?> getExpectedExtensionType();

    /**
     * Getter of the current building extension.
     *
     * @return a building extension object.
     */
    @Nullable
    public IBuildingExtension getCurrentExtension()
    {
        if (currentExtensionId == null)
        {
            return null;
        }
        return building.getColony().getBuildingManager().getMatchingBuildingExtension(currentExtensionId);
    }

    /**
     * Retrieves the building extension to work on for the citizen, as long as the current building extension has work, it will keep returning that building extension.
     * Else it will retrieve a random building extension to work on for the citizen.
     * This method will also automatically claim any building extensions that are not in use if the building is on automatic assignment mode.
     *
     * @return a building extension to work on.
     */
    @Nullable
    public IBuildingExtension getExtensionToWorkOn()
    {
        final IBuildingExtension currentExtension = getCurrentExtension();
        if (currentExtension != null)
        {
            return currentExtension;
        }

        IBuildingExtension.ExtensionId lastUsedExtension = null;
        int lastUsedExtensionDay = building.getColony().getDay();

        for (final IBuildingExtension extension : getOwnedExtensions())
        {
            if (!checkedExtensions.containsKey(extension.getId()))
            {
                currentExtensionId = extension.getId();
                return extension;
            }

            final int lastDay = checkedExtensions.get(extension.getId());
            if (lastDay < lastUsedExtensionDay)
            {
                lastUsedExtension = extension.getId();
                lastUsedExtensionDay = lastDay;
            }
        }
        currentExtensionId = lastUsedExtension;
        return getCurrentExtension();
    }

    /**
     * Returns list of owned building extensions.
     *
     * @return a list of building extension objects.
     */
    @NotNull
    public final List<IBuildingExtension> getOwnedExtensions()
    {
        return getMatchingExtension(f -> building.getID().equals(f.getBuildingId()));
    }

    /**
     * Returns list of building extensions.
     *
     * @return a list of building extension objects.
     */
    @NotNull
    public abstract List<IBuildingExtension> getMatchingExtension(final Predicate<IBuildingExtension>  predicateToMatch);

    /**
     * Attempt to automatically claim free building extensions, if possible and if any building extensions are available.
     */
    public void claimExtensions()
    {
        if (!shouldAssignManually)
        {
            for (final IBuildingExtension extension : getFreeExtensions())
            {
                if (assignExtension(extension))
                {
                    break;
                }
            }
        }
    }

    /**
     * Returns list of free building extensions.
     *
     * @return a list of building extension objects.
     */
    public final List<IBuildingExtension> getFreeExtensions()
    {
        return getMatchingExtension(extension -> !extension.isTaken());
    }

    /**
     * Method called to assign a building extension to the building.
     *
     * @param extension the building extension to add.
     */
    public boolean assignExtension(final IBuildingExtension extension)
    {
        if (canAssignExtension(extension))
        {
            extension.setBuilding(building.getID());
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Check to see if a new building extension can be assigned to the worker.
     *
     * @param extension the building extension which is being added.
     * @return true if so.
     */
    public final boolean canAssignExtension(final IBuildingExtension extension)
    {
        return getOwnedExtensions().size() < getMaxExtensionCount() && canAssignExtensionOverride(extension);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        building.getColony().getBuildingManager().markBuildingExtensionsDirty();
    }

    /**
     * Additional checks to see if this building extension can be assigned to the building.
     *
     * @param extension the building extension which is being added.
     * @return true if so.
     */
    protected abstract boolean canAssignExtensionOverride(IBuildingExtension extension);

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public final boolean assignManually()
    {
        return shouldAssignManually;
    }

    /**
     * Checks if the building has any building extensions.
     *
     * @return true if he has none.
     */
    public final boolean hasNoExtensions()
    {
        return getOwnedExtensions().isEmpty();
    }

    /**
     * Switches the assign manually of the building.
     *
     * @param assignManually true if assignment should be manual.
     */
    public final void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Method called to free a building extension.
     *
     * @param extension the building extension to be freed.
     */
    public void freeExtension(final IBuildingExtension extension)
    {
        extension.resetOwningBuilding();
        markDirty();

        if (currentExtensionId == extension.getId())
        {
            resetCurrentExtension();
        }
    }

    /**
     * Resets the current building extension if the worker indicates this building extension should no longer be worked on.
     */
    public void resetCurrentExtension()
    {
        if (currentExtensionId != null)
        {
            checkedExtensions.put(currentExtensionId, building.getColony().getDay());
        }
        currentExtensionId = null;
    }
}
