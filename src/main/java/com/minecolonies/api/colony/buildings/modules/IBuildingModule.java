package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.util.IHasDirty;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Default interface for all building modules.
 */
public interface IBuildingModule extends IHasDirty
{
    /**
     * Get the building of the module.
     * @return the building.
     */
    IBuilding getBuilding();

    /**
     * Set the building of the module.
     * @param building the building to set.
     * @return the module itself.
     */
    IBuildingModule setBuilding(final IBuilding building);

    /**
     * Set the producer of this module
     * @param moduleSet
     * @return
     */
    IBuildingModule setProducer(BuildingEntry.ModuleProducer moduleSet);

    /**
     * Get the producer of this module
     *
     * @return
     */
    BuildingEntry.ModuleProducer getProducer();

    /**
     * Serialization method to send the module data to the client side.
     *
     * @param buf      the buffer to write it to.
     * @param fullSync whether we need to sync the full data
     */
    default void serializeToView(FriendlyByteBuf buf, final boolean fullSync)
    {
        serializeToView(buf);
    }

    /**
     * Serialization method to send the module data to the client side.
     *
     * @param buf the buffer to write it to.
     */
    default void serializeToView(FriendlyByteBuf buf) {}
}
