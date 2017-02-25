package com.minecolonies.coremod.colony.management.requestsystem.api.location;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestManager;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.NotNull;

/**
 * Interface describing classes that serialize and deserialize
 * an ILocation to and from NBT.
 * @param <T> The type of location this factory can serialize
 * @param <D> The type of NBTTag this factory reads from and write to.
 */
public interface ILocationFactory<T , D extends NBTBase> {

    /**
     * Method to get the location type this factory can produce.
     * @return The type of location this factory can produce.
     */
    @NotNull
    Class<? extends T> getFactoryProductionType();
    
    /**
     * Method to serialize a given Request.
     * @param location The location to serialize.
     * @param manager The manager that requested the serialization.
     * @return The serialized data of the given location.
     */
    @NotNull
    D serializeLocation(@NotNull IRequestManager manager, @NotNull T location);

    /**
     * Method to deserialize a given Request.
     * @param nbt The data of the location that should be deserialized.
     * @param manager The manager requesting
     * @return The location that corresponds with the given data in the nbt
     */
    @NotNull
    T deserializeLocation(@NotNull IRequestManager manager, @NotNull D nbt);
}
