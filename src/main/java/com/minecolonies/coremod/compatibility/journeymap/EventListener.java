package com.minecolonies.coremod.compatibility.journeymap;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ClientChunkUpdatedEvent;
import com.minecolonies.api.colony.event.ColonyViewUpdatedEvent;
import journeymap.client.api.IClientAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class EventListener
{
    @NotNull
    private final IClientAPI jmap;

    public EventListener(@NotNull final IClientAPI jmap)
    {
        this.jmap = jmap;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        ColonyBorderMapping.clear();
        ColonyDeathpoints.clear();
        this.jmap.removeAll(MOD_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChunkLoaded(@NotNull final ChunkEvent.Load event)
    {
        if (!event.getWorld().isClientSide()) return;

        if (event.getWorld() instanceof Level)
        {
            final ResourceKey<Level> dimension = ((Level) event.getWorld()).dimension();

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
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        final Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
