package com.minecolonies.coremod.compatibility.journeymap;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ClientChunkUpdatedEvent;
import com.minecolonies.api.colony.event.ColonyViewUpdatedEvent;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import journeymap.client.api.display.Context;
import journeymap.client.api.event.forge.EntityRadarUpdateEvent;
import journeymap.client.api.model.WrappedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_JOB;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_JMAP_PREFIX;

public class EventListener
{
    private static final Style JOB_TOOLTIP = Style.EMPTY.withColor(TextFormatting.YELLOW).withItalic(true);

    @NotNull
    private final Journeymap jmap;

    public EventListener(@NotNull final Journeymap jmap)
    {
        this.jmap = jmap;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        ColonyDeathpoints.clear();
        this.jmap.getApi().removeAll(MOD_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChunkLoaded(@NotNull final ChunkEvent.Load event)
    {
        if (!event.getWorld().isClientSide()) return;

        if (event.getWorld() instanceof World)
        {
            final RegistryKey<World> dimension = ((World) event.getWorld()).dimension();

            ColonyDeathpoints.updateChunk(this.jmap, dimension, event.getChunk());
        }
    }

    @SubscribeEvent
    public void onColonyChunkDataUpdated(@NotNull final ClientChunkUpdatedEvent event)
    {
        final RegistryKey<World> dimension = event.getChunk().getLevel().dimension();

        ColonyBorderMapping.updateChunk(this.jmap, dimension, event.getChunk());
    }

    @SubscribeEvent
    public void onColonyViewUpdated(@NotNull final ColonyViewUpdatedEvent event)
    {
        final IColonyView colony = event.getColony();
        final Set<BlockPos> graves = colony.getGraveManager().getGraves().keySet();

        ColonyDeathpoints.updateGraves(this.jmap, colony, graves);
    }

    @SubscribeEvent
    public void onUpdateEntityRadar(@NotNull final EntityRadarUpdateEvent event)
    {
        final WrappedEntity wrapper = event.getWrappedEntity();
        final LivingEntity entity = wrapper.getEntityLivingRef().get();

        if (entity instanceof AbstractEntityCitizen)
        {
            final boolean isVisitor = entity instanceof VisitorCitizen;
            TextComponent jobName;

            if (isVisitor)
            {
                if (!JourneymapOptions.getShowVisitors(this.jmap.getOptions()))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = new TranslationTextComponent(COM_MINECOLONIES_JMAP_PREFIX + "visitor");
            }
            else
            {
                final String jobId = entity.getEntityData().get(DATA_JOB);
                final JobEntry jobEntry = IJobRegistry.getInstance().getValue(new ResourceLocation(jobId));
                final IJob<?> job = jobEntry == null ? null : jobEntry.produceJob(null);

                if (job instanceof AbstractJobGuard
                        ? !JourneymapOptions.getShowGuards(this.jmap.getOptions())
                        : !JourneymapOptions.getShowCitizens(this.jmap.getOptions()))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = new TranslationTextComponent(jobEntry == null
                        ? COM_MINECOLONIES_JMAP_PREFIX + "unemployed"
                        : jobEntry.getTranslationKey());
            }

            if (JourneymapOptions.getShowColonistTooltip(this.jmap.getOptions()))
            {
                ITextComponent name = entity.getCustomName();
                if (name != null)
                {
                    wrapper.setEntityToolTips(Arrays.asList(name, jobName.setStyle(JOB_TOOLTIP)));
                }
            }

            final boolean showName = event.getActiveUiState().ui.equals(Context.UI.Minimap)
                    ? JourneymapOptions.getShowColonistNameMinimap(this.jmap.getOptions())
                    : JourneymapOptions.getShowColonistNameFullscreen(this.jmap.getOptions());

            if (!showName)
            {
                wrapper.setCustomName("");
            }

            if (!isVisitor && JourneymapOptions.getShowColonistTeamColour(this.jmap.getOptions()))
            {
                wrapper.setColor(entity.getTeamColor());
            }
        }
        else if (entity instanceof AbstractEntityMinecoloniesMob)
        {
            final JourneymapOptions.RaiderColor color = JourneymapOptions.getRaiderColor(this.jmap.getOptions());

            if (JourneymapOptions.RaiderColor.NONE.equals(color))
            {
                wrapper.setDisable(true);
            }
            else if (!JourneymapOptions.RaiderColor.HOSTILE.equals(color))
            {
                wrapper.setColor(color.getColor().getValue());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        final World world = Minecraft.getInstance().level;

        if (world != null)
        {
            ColonyBorderMapping.updatePending(this.jmap, world.dimension());
        }
    }
}
