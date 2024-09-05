package com.minecolonies.api.tools.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public class IToolTypeRegistry
{
    public static IForgeRegistry<ToolTypeEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getToolTypeRegistry();
    }
}
