package com.minecolonies.core.compatibility.journeymap.v6;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.eventbus.events.colony.ColonyViewUpdatedModEvent;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import com.minecolonies.core.event.ClientChunkUpdatedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_JOB;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JOURNEY_MAP_INFO;

public class EventListener
{
    public static final Style JOB_TOOLTIP = Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true);

    @NotNull
    private final Journeymap jmap;

    public EventListener(@NotNull final Journeymap jmap)
    {
        this.jmap = jmap;

        MinecraftForge.EVENT_BUS.register(this);
        IMinecoloniesAPI.getInstance().getEventBus().subscribe(ColonyViewUpdatedModEvent.class, this::onColonyViewUpdated);
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

    public void onColonyViewUpdated(@NotNull final ColonyViewUpdatedModEvent event)
    {
        final IColonyView colony = event.getColony();
        final Set<BlockPos> graves = colony.getGraveManager().getGraves().keySet();

        ColonyDeathpoints.updateGraves(this.jmap, colony, graves);
        ColonyBorderMapping.updateChunksAroundPlayer(this.jmap, colony.getDimension());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        final Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
