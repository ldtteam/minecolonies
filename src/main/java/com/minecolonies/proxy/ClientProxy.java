package com.minecolonies.proxy;

import com.minecolonies.client.RenderBipedCitizenMulti;
import com.minecolonies.client.model.ModelEntityCitizenFemaleAristocrat;
import com.minecolonies.client.model.ModelEntityCitizenFemaleCitizen;
import com.minecolonies.client.model.ModelEntityCitizenFemaleNoble;
import com.minecolonies.entity.EntityCitizen;
import com.github.lunatrius.schematica.client.events.ChatEventHandler;
import com.github.lunatrius.schematica.client.events.KeyInputHandler;
import com.github.lunatrius.schematica.client.events.TickHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.model.ModelBiped;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new RenderBipedCitizenMulti(new ModelBiped(), new ModelEntityCitizenFemaleCitizen(), new ModelEntityCitizenFemaleNoble(), new ModelEntityCitizenFemaleAristocrat(), 1f));
    
         MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
    }
}
 