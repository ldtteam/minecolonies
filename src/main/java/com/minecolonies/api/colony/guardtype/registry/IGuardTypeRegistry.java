package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.guardtype.GuardType;
import net.neoforged.neoforge.registries.IForgeRegistry;

public interface IGuardTypeRegistry
{

    static IForgeRegistry<GuardType> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeRegistry();
    }
}
