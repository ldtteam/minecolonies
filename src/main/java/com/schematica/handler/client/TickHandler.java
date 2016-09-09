package com.schematica.handler.client;

import com.schematica.Settings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Settings TickHandler for schematic rendering.
 */
public final class TickHandler
{
    public static final TickHandler INSTANCE = new TickHandler();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private TickHandler()
    {
    }

    /**
     * Mark dirty when a client disconnects.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onClientDisconnect(final FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Settings.instance.markDirty();
    }

    /**
     * Call {@link Settings#reset()} if dirty.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (this.minecraft.isGamePaused() || event.phase != TickEvent.Phase.END)
        {
            return;
        }

        this.minecraft.mcProfiler.startSection("schematica");

        if (Settings.instance.isDirty())
        {
            Settings.instance.reset();
        }

        this.minecraft.mcProfiler.endSection();
    }
}
