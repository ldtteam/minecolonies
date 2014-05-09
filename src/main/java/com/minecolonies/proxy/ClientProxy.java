package com.minecolonies.proxy;

import com.github.lunatrius.schematica.client.events.ChatEventHandler;
import com.github.lunatrius.schematica.client.events.KeyInputHandler;
import com.github.lunatrius.schematica.client.events.TickHandler;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.minecolonies.client.RenderBipedCitizenMulti;
import com.minecolonies.client.model.ModelEntityCitizenFemaleAristocrat;
import com.minecolonies.client.model.ModelEntityCitizenFemaleCitizen;
import com.minecolonies.client.model.ModelEntityCitizenFemaleNoble;
import com.minecolonies.entity.EntityCitizen;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{
    private RendererSchematicGlobal rendererSchematicGlobal;

    @Override
    public void registerKeybindings()
    {
        for(KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS)
        {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Override
    public void registerEvents()
    {
        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
        FMLCommonHandler.instance().bus().register(new TickHandler());

        this.rendererSchematicGlobal = new RendererSchematicGlobal();
        MinecraftForge.EVENT_BUS.register(this.rendererSchematicGlobal);
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
    }

    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new RenderBipedCitizenMulti(new ModelBiped(), new ModelEntityCitizenFemaleCitizen(), new ModelEntityCitizenFemaleNoble(), new ModelEntityCitizenFemaleAristocrat(), 1f));

        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
    }
}
