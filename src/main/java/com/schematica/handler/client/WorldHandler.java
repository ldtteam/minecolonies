package com.schematica.handler.client;

import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * WorldHandler for {@link SchematicWorld}'s.
 */
public class WorldHandler
{
    /**
     * Called when a world is loaded. Adds the {@link RenderSchematic} {@link IWorldAccess} to the {@link SchematicWorld}.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onLoad(final WorldEvent.Load event)
    {
        if (event.world.isRemote && !(event.world instanceof SchematicWorld))
        {
            RenderSchematic.INSTANCE.setWorldAndLoadRenderers(Settings.instance.getSchematicWorld());
            addWorldAccess(event.world, RenderSchematic.INSTANCE);
        }
    }

    /**
     * Called when a world is unloaded. Removes the {@link RenderSchematic} {@link IWorldAccess} from the {@link SchematicWorld}.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event)
    {
        if (event.world.isRemote)
        {
            removeWorldAccess(event.world, RenderSchematic.INSTANCE);
        }
    }

    private static void addWorldAccess(final World world, final IWorldAccess schematic)
    {
        if (world != null && schematic != null)
        {
            world.addWorldAccess(schematic);
        }
    }

    private static void removeWorldAccess(final World world, final IWorldAccess schematic)
    {
        if (world != null && schematic != null)
        {
            world.removeWorldAccess(schematic);
        }
    }
}
