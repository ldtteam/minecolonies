package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Interface for all building modules that store additional data.
 */
public interface IPersistentModule extends IBuildingModule
{
    /**
     * Deserialize the module.
     * @param compound the nbt compound.
     */
    default void deserializeNBT(CompoundNBT compound) { }

    /**
     * Serialize the module from a compound.
     * @param compound the compound.
     */
    default void serializeNBT(final CompoundNBT compound) { }

    /**
     * Serialization method to send the module data to the client side.
     * @param buf the buffer to write it to.
     */
    default void serializeToView(PacketBuffer buf) { }
}
