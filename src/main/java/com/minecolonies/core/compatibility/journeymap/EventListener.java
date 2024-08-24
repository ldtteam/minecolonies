package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ColonyViewUpdatedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class EventListener
{
    @NotNull
    private final Journeymap jmap;
    private boolean viewsUpdated;

    public EventListener(@NotNull final Journeymap jmap)
    {
        this.jmap = jmap;

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyDeathpoints.clear();
        this.jmap.getApi().removeAll(MOD_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChunkLoaded(@NotNull final ChunkEvent.Load event)
    {
        if (!event.getLevel().isClientSide()) return;

        if (event.getLevel() instanceof final Level level)
        {
            final ResourceKey<Level> dimension = level.dimension();

            ColonyBorderMapping.updateChunk(this.jmap, dimension, event.getChunk());
            ColonyDeathpoints.updateChunk(this.jmap, dimension, event.getChunk());
        }
    }

    @SubscribeEvent
    public void onColonyViewUpdated(@NotNull final ColonyViewUpdatedEvent event)
    {
        final IColonyView colony = event.getColony();
        final Set<BlockPos> graves = colony.getGraveManager().getGraves().keySet();

        viewsUpdated = true;
        ColonyDeathpoints.updateGraves(this.jmap, colony, graves);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(@NotNull final ClientTickEvent.Pre event)
    {
        final Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            if (viewsUpdated)
            {
                viewsUpdated = false;
                ColonyBorderMapping.queueChunks(this.jmap, world.dimension());
            }

            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
