package com.minecolonies.proxy;

import com.minecolonies.client.EntityCitizenRenderer;
import com.minecolonies.entity.EntityCitizen;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new EntityCitizenRenderer());
    }
}
