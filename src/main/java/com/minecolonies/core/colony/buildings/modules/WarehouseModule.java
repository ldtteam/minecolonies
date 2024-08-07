package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for the warehouse module.
 */
public class WarehouseModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * The storage tag for the storage capacity.
     */
    private static final String TAG_STORAGE = "tagStorage";

    /**
     * Storage upgrade level.
     */
    private int storageUpgrade = 0;

    /**
     * Construct a new grouped itemlist module with the unique list identifier.
     */
    public WarehouseModule()
    {
        super();
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        storageUpgrade = compound.getInt(TAG_STORAGE);
    }

    @Override
    public void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        compound.putInt(TAG_STORAGE, storageUpgrade);
    }

    @Override
    public void serializeToView(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(storageUpgrade);
    }

    /**
     * Get the upgrade level.
     * @return the level.
     */
    public int getStorageUpgrade()
    {
        return storageUpgrade;
    }

    /**
     * Increment the storage upgrade level.
     */
    public void incrementStorageUpgrade()
    {
        this.storageUpgrade++;
    }
}
