package com.minecolonies.api.colony.raids.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.raids.RaidType;
import net.minecraftforge.registries.IForgeRegistry;

public interface IRaidTypeRegistry
{
    static IForgeRegistry<RaidType> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getRaidTypeRegistry();
    }
}
