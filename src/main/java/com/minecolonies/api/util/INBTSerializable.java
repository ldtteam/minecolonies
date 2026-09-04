package com.minecolonies.api.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Contract for MineColonies objects persisted as a self-contained compound tag.
 *
 * @param <T> the serialized representation.
 */
public interface INBTSerializable<T extends Tag>
{
    /**
     * Serialize this object.
     *
     * @param provider registry lookup context.
     * @return the serialized value.
     */
    T serializeNBT(@NotNull HolderLookup.Provider provider);

    /**
     * Restore this object from previously serialized data.
     *
     * @param provider registry lookup context.
     * @param nbt      previously serialized value.
     */
    void deserializeNBT(@NotNull HolderLookup.Provider provider, T nbt);
}
