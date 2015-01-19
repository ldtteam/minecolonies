package com.minecolonies.proxy;

import com.minecolonies.MineColonies;
import com.minecolonies.client.gui.WindowCitizen;
import com.minecolonies.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    //private RendererSchematicGlobal rendererSchematicGlobal;

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void registerKeybindings()
    {
//        for(KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS)
//        {
//            ClientRegistry.registerKeyBinding(keyBinding);
//        }
    }

    @Override
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
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, new RenderBipedCitizen());
    }

    @Override
    public void registerTileEntityRendering()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityColonyBuilding.class, new EmptyTileEntitySpecialRenderer());
    }

    @Override
    public void showCitizenWindow(CitizenData.View citizen)
    {
        if (Configurations.enableInDevelopmentFeatures)
        {
            GuiHandler.showGuiWindow(new WindowCitizen(citizen));
        }
        else
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
