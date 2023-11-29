package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface for all building modules that store additional data.
 */
public interface IPersistentModule extends IBuildingModule
{
    /**
     * Deserialize the module.
     * @param compound the nbt compound.
     */
    default void deserializeNBT(CompoundTag compound) { }

    /**
     * Serialize the module from a compound.
     * @param compound the compound.
     */
    default void serializeNBT(final CompoundTag compound) { }
}
