package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for all building modules that store additional data.
 */
public interface IPersistentModule extends IBuildingModule
{
    /**
     * Deserialize the module.
     * @param compound the nbt compound.
     */
    default void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound) { }

    /**
     * Serialize the module from a compound.
     * @param compound the compound.
     */
    default void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound) { }
}
