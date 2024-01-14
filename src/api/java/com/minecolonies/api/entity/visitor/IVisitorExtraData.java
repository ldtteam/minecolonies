package com.minecolonies.api.entity.visitor;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface for extra visitor data.
 */
public interface IVisitorExtraData<S> extends INBTSerializable<CompoundTag>
{
    /**
     * The unique key which the data gets stored under.
     *
     * @return the key.
     */
    String getKey();

    /**
     * Get the visitor data value.
     *
     * @return the value.
     */
    S getValue();

    /**
     * Get the default value in case no value was explicitly set yet.
     *
     * @return the value.
     */
    S getDefaultValue();
}