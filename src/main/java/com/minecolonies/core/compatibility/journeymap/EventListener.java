package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ClientChunkUpdatedEvent;
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

        if (event.getLevel() instanceof Level)
        {
            final ResourceKey<Level> dimension = ((Level) event.getLevel()).dimension();

            ColonyDeathpoints.updateChunk(this.jmap, dimension, event.getChunk());
        }
    }

    @SubscribeEvent
    public void onColonyChunkDataUpdated(@NotNull final ClientChunkUpdatedEvent event)
    {
        final ResourceKey<Level> dimension = event.getChunk().getLevel().dimension();

        ColonyBorderMapping.updateChunk(this.jmap, dimension, event.getChunk());
    }

    @SubscribeEvent
    public void onColonyViewUpdated(@NotNull final ColonyViewUpdatedEvent event)
    {
        final IColonyView colony = event.getColony();
        final Set<BlockPos> graves = colony.getGraveManager().getGraves().keySet();

        ColonyDeathpoints.updateGraves(this.jmap, colony, graves);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(@NotNull final ClientTickEvent.Pre event)
    {
        final Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
