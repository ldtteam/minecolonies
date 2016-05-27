package com.schematica.handler.client;

import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldHandler {
    @SubscribeEvent
    public void onLoad(final WorldEvent.Load event) {
        if (event.world.isRemote && !(event.world instanceof SchematicWorld)) {
            RenderSchematic.INSTANCE.setWorldAndLoadRenderers(Settings.instance.schematic);
            addWorldAccess(event.world, RenderSchematic.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        if (event.world.isRemote) {
            removeWorldAccess(event.world, RenderSchematic.INSTANCE);
        }
    }

    private static void addWorldAccess(final World world, final IWorldAccess schematic) {
        if (world != null && schematic != null) {
            world.addWorldAccess(schematic);
        }
    }

    private static void removeWorldAccess(final World world, final IWorldAccess schematic) {
        if (world != null && schematic != null) {
            world.removeWorldAccess(schematic);
        }
    }
}
