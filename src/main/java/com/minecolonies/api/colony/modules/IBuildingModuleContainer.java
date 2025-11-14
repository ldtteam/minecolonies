package com.minecolonies.api.colony.modules;

import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Module container for buildings.
 */
public interface IBuildingModuleContainer extends IModuleContainer<IBuildingModule>
{
    /**
     * Check if the object has a particular module.
     *
     * @param producer the module producer for the module
     * @return true if so.
     */
    boolean hasModule(final BuildingEntry.ModuleProducer<?, ?> producer);

    /**
     * Get the first module with a particular class or interface.
     *
     * @return the module or empty if not existent.
     */
    <M extends IBuildingModule, V extends IBuildingModuleView> M getModule(final BuildingEntry.ModuleProducer<M, V> producer);

    /**
     * Get the first module with a particular class or interface.
     *
     * @return the module or empty if not existent.
     */
    IBuildingModule getModule(final int id);

    /**
     * Register a specific module to the object.
     *
     * @param module the module to register.
     */
    void registerModule(@NotNull final IBuildingModule module);
}
