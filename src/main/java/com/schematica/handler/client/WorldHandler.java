package com.schematica.handler.client;

import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * WorldHandler for {@link SchematicWorld}'s.
 */
public class WorldHandler
{
    /**
     * Called when a world is loaded. Adds the {@link RenderSchematic} {@link IWorldEventListener} to the {@link SchematicWorld}.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onLoad(@NotNull final WorldEvent.Load event)
    {
        if (event.getWorld().isRemote && !(event.getWorld() instanceof SchematicWorld))
        {
            RenderSchematic.INSTANCE.setWorldAndLoadRenderers(Settings.instance.getSchematicWorld());
            addWorldAccess(event.getWorld(), RenderSchematic.INSTANCE);
        }
    }

    private static void addWorldAccess(@Nullable final World world, @Nullable final IWorldEventListener schematic)
    {
        if (world != null && schematic != null)
        {
            world.addEventListener(schematic);
        }
    }

    /**
     * Called when a world is unloaded. Removes the {@link RenderSchematic} {@link IWorldEventListener} from the {@link SchematicWorld}.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onUnload(@NotNull final WorldEvent.Unload event)
    {
        if (event.getWorld().isRemote)
        {
            removeWorldAccess(event.getWorld(), RenderSchematic.INSTANCE);
        }
    }

    private static void removeWorldAccess(@Nullable final World world, @Nullable final IWorldEventListener schematic)
    {
        if (world != null && schematic != null)
        {
            world.removeEventListener(schematic);
        }
    }
}
