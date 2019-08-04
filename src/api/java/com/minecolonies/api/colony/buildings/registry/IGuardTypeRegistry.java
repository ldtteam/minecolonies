package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.IGuardType;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

/**
 * Registry to register guard types to.
 */
public interface IGuardTypeRegistry
{

    static IGuardTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeRegistry();
    }

    /**
     * Method used to register new guard type.
     *
     * @param type The new type.
     * @return The registry.
     */
    @NotNull
    IGuardTypeRegistry registerGuardType(final IGuardType type);

    /**
     * Method to get the guard type from a name, takes registration into account.
     * If a not registered name is passed in, then null is returned.
     *
     * @param location The name of the type that is being looked up.
     * @return The guard type or null.
     */
    @Nullable
    default IGuardType getFromName(final ResourceLocation location)
    {
        if (location == null)
        {
            return null;
        }

        if (!getRegisteredTypes().containsKey(location))
        {
            return null;
        }

        return getRegisteredTypes().get(location);
    }

    /**
     * Method used to get the registered guard types by their name.
     *
     * @return A map containing a mapping between guard type and its name.
     */
    @NotNull
    LinkedHashMap<ResourceLocation, IGuardType> getRegisteredTypes();
}
