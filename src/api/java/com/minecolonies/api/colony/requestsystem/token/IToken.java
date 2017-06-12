package com.minecolonies.api.colony.requestsystem.token;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface used to represent a request outside of the request management system.
 * <p>
 * Allows for simple storage of all open requests of a building, a worker etc, without having to
 * store the whole request twice.
 * <p>
 * Also extends INBTSerializable to make writing the data to disk a lot easier.
 */
public interface IToken<T, D extends NBTBase> extends INBTSerializable<D>
{

    /**
     * The identifier used to represent a request.
     *
     * @return The identifier of the request that this token represents.
     */
    T getIdentifier();
}
