package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract class to list all building extensions (assigned) to a building.
 */
public abstract class BuildingExtensionsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingModule
{
    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * A map of building extensions, along with their unix timestamp of when they can next be checked again.
     */
    private final Map<BlockPos, Integer> checkedExtensions = new HashMap<>();

    /**
     * The building extension the citizen is currently working on.
     */
    @Nullable
    private IBuildingExtension currentExtension;

    /**
     * Building extensions should be assigned manually to the citizen.
     */
    private boolean shouldAssignManually = false;

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);
        final ListTag listTag = compound.getList(TAG_LIST, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); ++i)
        {
            final CompoundTag tag = listTag.getCompound(i);
            checkedExtensions.put(BlockPosUtil.read(tag, TAG_POS), compound.getInt(TAG_DAY));
        }
    }

    @Override
    public void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        final ListTag listTag = new ListTag();
        for (final Map.Entry<BlockPos, Integer> entry : checkedExtensions.entrySet())
        {
            final CompoundTag listEntry = new CompoundTag();
            BlockPosUtil.write(compound, TAG_POS, entry.getKey());
            listEntry.putLong(TAG_DAY, entry.getValue());
            listTag.add(listEntry);
        }
        compound.put(TAG_LIST, listTag);
    }

    @Override
    public void serializeToView(@NotNull final RegistryFriendlyByteBuf buf)
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
     * Get the class type which is expected for the building extensions to have.
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
        return currentExtension;
    }

    /**
     * Retrieves the building extension to work on for the citizen, as long as the current building extension has work, it will keep returning that building extension.
     * Else it will retrieve a random building extension to work on for the citizen.
     * This method will also automatically claim any building extensions that are not in use if the building is on automatic assignment mode.
     *
     * @return a building extension to work on.
     */
    @Nullable
    public IBuildingExtension getBuildingExtensionToWorkOn()
    {
        if (currentExtension != null)
        {
            return currentExtension;
        }

        IBuildingExtension lastUsedExtension = null;
        int lastUsedExtensionDay = 0;

        for (final IBuildingExtension extension : getOwnedExtensions())
        {
            if (!checkedExtensions.containsKey(extension.getPosition()))
            {
                currentExtension = extension;
                return extension;
            }

            final int lastDay = checkedExtensions.get(extension.getPosition());
            if (lastUsedExtension == null)
            {
                if (lastDay < building.getColony().getDay())
                {
                    lastUsedExtension = extension;
                    lastUsedExtensionDay = lastDay;
                }
            }
            else if (lastUsedExtensionDay < lastDay)
            {
                lastUsedExtension = extension;
                lastUsedExtensionDay = lastDay;
            }
        }
        currentExtension = lastUsedExtension;
        return lastUsedExtension;
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
                assignExtension(extension);
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
    public void assignExtension(final IBuildingExtension extension)
    {
        if (canAssignExtension(extension))
        {
            extension.setBuilding(building.getID());
            markDirty();
        }
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
    protected abstract boolean canAssignExtensionOverride(final IBuildingExtension extension);

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

        if (Objects.equals(currentExtension, extension))
        {
            resetCurrentExtension();
        }
    }

    /**
     * Resets the current building extension if the worker indicates this building extension should no longer be worked on.
     */
    public void resetCurrentExtension()
    {
        if (currentExtension != null)
        {
            checkedExtensions.put(currentExtension.getPosition(), building.getColony().getDay());
        }
        currentExtension = null;
    }
}
