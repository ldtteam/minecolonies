package com.minecolonies.proxy;

import com.github.lunatrius.schematica.client.events.KeyInputHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;

public interface IProxy
{
    void registerTileEntities();

    void registerKeybindings();

    void registerEvents();
}
