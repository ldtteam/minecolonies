package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.display.Context;
import journeymap.api.v2.client.entity.WrappedEntity;
import journeymap.api.v2.client.event.EntityRadarUpdateEvent;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.client.event.RegistryEvent;
import journeymap.api.v2.common.event.ClientEventRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_JOB;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JOURNEY_MAP_INFO;

/**
 * Plugin entrypoint for JourneyMap
 */
@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public class JourneymapPlugin implements IClientPlugin
{
    private static final Style JOB_TOOLTIP = Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true);

    private Journeymap jmap;
    @SuppressWarnings("unused")
    private EventListener listener;

    @Override
    public void initialize(@NotNull final IClientAPI api)
    {
        this.jmap = new Journeymap(api);
        this.listener = new EventListener(this.jmap);

        ClientEventRegistry.MAPPING_EVENT.subscribe(MOD_ID, this::onMappingEvent);
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT_EVENT.subscribe(MOD_ID, this::onOptionsRegistryEvent);
        ClientEventRegistry.INFO_SLOT_REGISTRY_EVENT_EVENT.subscribe(MOD_ID, this::onInfoRegistryEvent);
        ClientEventRegistry.ENTITY_RADAR_UPDATE_EVENT.subscribe(MOD_ID, this::onEntityRadarUpdateEvent);
    }

    @Override
    public String getModId()
    {
        return MOD_ID;
    }

    private void onMappingEvent(final MappingEvent event)
    {
        switch (event.getStage())
        {
            case MAPPING_STARTED:
                ColonyBorderMapping.load(this.jmap, event.dimension);
                break;

            case MAPPING_STOPPED:
                ColonyBorderMapping.unload(this.jmap, event.dimension);
                ColonyDeathpoints.unload(this.jmap, event.dimension);
                break;
        }
    }

    private void onOptionsRegistryEvent(final RegistryEvent.OptionsRegistryEvent event)
    {
        this.jmap.setOptions(new JourneymapOptions());
    }

    private void onInfoRegistryEvent(final RegistryEvent.InfoSlotRegistryEvent event)
    {
        event.register(MOD_ID, "com.minecolonies.coremod.journeymap.currentcolony", 2500, ColonyBorderMapping::getCurrentColony);
    }

    private void onEntityRadarUpdateEvent(final EntityRadarUpdateEvent event)
    {
        final WrappedEntity wrapper = event.getWrappedEntity();
        final Entity entity = wrapper.getEntityRef().get();

        if (entity instanceof AbstractEntityCitizen)
        {
            final boolean isVisitor = entity instanceof VisitorCitizen;
            MutableComponent jobName;

            if (isVisitor)
            {
                if (!JourneymapOptions.getShowVisitors(this.jmap.getOptions()))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = Component.translatableEscape(PARTIAL_JOURNEY_MAP_INFO + "visitor");
            }
            else
            {
                final String jobId = entity.getEntityData().get(DATA_JOB);
                final JobEntry jobEntry = IJobRegistry.getInstance().get(ResourceLocation.parse(jobId));
                final IJob<?> job = jobEntry == null ? null : jobEntry.produceJob(null);

                if (job instanceof AbstractJobGuard
                        ? !JourneymapOptions.getShowGuards(this.jmap.getOptions())
                        : !JourneymapOptions.getShowCitizens(this.jmap.getOptions()))
                {
                    wrapper.setDisable(true);
                    return;
                }

                jobName = Component.translatableEscape(jobEntry == null
                        ? PARTIAL_JOURNEY_MAP_INFO + "unemployed"
                        : jobEntry.getTranslationKey());
            }

            if (JourneymapOptions.getShowColonistTooltip(this.jmap.getOptions()))
            {
                Component name = entity.getCustomName();
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
        else if (entity instanceof AbstractEntityRaiderMob)
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
}
