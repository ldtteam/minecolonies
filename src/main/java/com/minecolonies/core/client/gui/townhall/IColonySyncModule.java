package com.minecolonies.core.client.gui.townhall;

import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import net.minecraft.nbt.CompoundTag;

public interface IColonySyncModule extends IPersistentModule
{
    /**
     * Update the module from the default data.
     *
     * @param compound the compound.
     */
    default void updateFromDefaults(final CompoundTag compound)
    {
        deserializeNBT(compound);
    }

    /**
     * Update the defaults for this given module.
     *
     * @return the new compound data.
     */
    default CompoundTag setDefaults()
    {
        final CompoundTag compoundTag = new CompoundTag();
        serializeNBT(compoundTag);
        return compoundTag;
    }
}
