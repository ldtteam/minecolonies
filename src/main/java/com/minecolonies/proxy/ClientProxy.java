package com.minecolonies.proxy;

import com.minecolonies.client.model.*;
import com.minecolonies.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.client.render.RenderBipedCitizenMulti;
import com.minecolonies.client.render.RenderBipedWorker;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.tileentities.TileEntityHut;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.model.ModelBiped;

public class ClientProxy extends CommonProxy
{
    //private RendererSchematicGlobal rendererSchematicGlobal;

    public void registerKeybindings()
    {
//        for(KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS)
//        {
//            ClientRegistry.registerKeyBinding(keyBinding);
//        }
    }

    public void registerEvents()
    {
        super.registerEvents();
//        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
//        FMLCommonHandler.instance().bus().register(new TickHandler());
//
//        this.rendererSchematicGlobal = new RendererSchematicGlobal();
//        MinecraftForge.EVENT_BUS.register(this.rendererSchematicGlobal);
    }

    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new RenderBipedCitizenMulti(new ModelBiped(), new ModelEntityCitizenFemaleCitizen(), new ModelEntityCitizenFemaleNoble(), new ModelEntityCitizenFemaleAristocrat()));
        RenderingRegistry.registerEntityRenderingHandler(EntityBuilder.class, new RenderBipedWorker(new ModelBiped(), new ModelEntityBuilderFemale()));
        RenderingRegistry.registerEntityRenderingHandler(EntityDeliveryman.class, new RenderBipedWorker(new ModelEntityDeliveryman(), new ModelEntityDeliveryman()));//TODO female model
    }

    @Override
    public void registerTileEntityRendering()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHut.class, new EmptyTileEntitySpecialRenderer());
    }
}
