package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.buildings.modules.ITemplateModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.util.IHasDirty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Interface for managing building module templates.
 */
public interface IBuildingModuleTemplateManager extends IHasDirty
{
    /**
     * A descriptor class for the different module templates.
     *
     * @param name               the unique display name of the module template.
     * @param appliedToBuildings how many buildings this template is applied to.
     */
    record ModuleTemplateDescriptor(
        @NotNull String name,
        int appliedToBuildings)
    {}

    /**
     * Get the list data for a given module template.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @return the compound data, or null.
     */
    @NotNull
    List<ModuleTemplateDescriptor> getTemplates(final BuildingEntry buildingEntry, final ResourceLocation key);

    /**
     * Get the compound data for a given module template.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @param name          the unique name of the module template.
     * @return the compound data, or null.
     */
    @Nullable
    CompoundTag getTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name);

    /**
     * Apply a template to a given building.
     *
     * @param provider the registry lookup provider.
     * @param module   the module instance.
     * @param name     the unique name of the module template.
     */
    void applyTemplate(@NotNull final HolderLookup.Provider provider, final ITemplateModule module, final String name);

    /**
     * Check if a given template is applied to a building.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @param name          the unique name of the module template.
     * @param buildingPos   the position of the building to check.
     * @return the compound data, or null.
     */
    boolean isApplied(final BuildingEntry buildingEntry, final ResourceLocation key, final String name, final BlockPos buildingPos);

    /**
     * Ignore using a template on a given building.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @param buildingPos   the position of the building to check.
     */
    void ignoreTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final BlockPos buildingPos);

    /**
     * Ignore using a template on a given building.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @param name          the unique name of the module template.
     * @param buildingPos   the position of the building to check.
     */
    void ignoreTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name, final BlockPos buildingPos);

    /**
     * Updates a module template and synchronize it to all applied buildings.
     *
     * @param provider the registry lookup provider.
     * @param module   the module instance.
     * @param name     the unique name of the module template.
     */
    void updateTemplate(@NotNull final HolderLookup.Provider provider, final ITemplateModule module, final String name);

    /**
     * Remove a template.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @param name          the unique name of the module template.
     */
    void removeTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name);

    /**
     * Read the templates from NBT.
     *
     * @param provider the registry lookup provider.
     * @param compound the compound.
     */
    void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound);

    /**
     * Write the templates to NBT.
     *
     * @param provider the registry lookup provider.
     * @return the compound.
     */
    CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider);

    /**
     * Sends packages to update the client side manager.
     *
     * @param closeSubscribers the existing subscribers.
     * @param newSubscribers   new subscribers
     */
    void sendPackets(@NotNull final Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers);

    /**
     * Overwrite the data on the client from the server.
     *
     * @param provider the registry lookup provider.
     * @param compound the compound.
     */
    void overWriteData(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound);
}
