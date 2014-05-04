package com.github.lunatrius.schematica.client;

import com.github.lunatrius.schematica.CommonProxy;
import com.github.lunatrius.schematica.client.events.ChatEventHandler;
import com.github.lunatrius.schematica.client.events.KeyInputHandler;
import com.github.lunatrius.schematica.client.events.TickHandler;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

public class ClientProxy extends CommonProxy
{
    private RendererSchematicGlobal rendererSchematicGlobal = null;
    private SchematicWorld          schematicWorld          = null;

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
    public File getDataDirectory()
    {
        return Minecraft.getMinecraft().mcDataDir;
    }

    @Override
    public void setActiveSchematic(SchematicWorld world)
    {
        this.schematicWorld = world;
    }

    @Override
    public void setActiveSchematic(SchematicWorld world, EntityPlayer player)
    {
        setActiveSchematic(world);
    }

    @Override
    public SchematicWorld getActiveSchematic()
    {
        return this.schematicWorld;
	}

	@Override
	public SchematicWorld getActiveSchematic(EntityPlayer player) {
		return getActiveSchematic();
	}
}
