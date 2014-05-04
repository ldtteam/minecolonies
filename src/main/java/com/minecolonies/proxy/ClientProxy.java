package com.minecolonies.proxy;

import com.minecolonies.client.RenderBipedCitizen;
import com.minecolonies.entity.EntityCitizen;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.model.ModelBiped;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new RenderBipedCitizen(new ModelBiped(), 1f));
    }
}
