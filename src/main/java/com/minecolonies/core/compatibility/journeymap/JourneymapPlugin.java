package com.minecolonies.core.compatibility.journeymap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.client.event.RegistryEvent;
import journeymap.api.v2.common.event.ClientEventRegistry;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Plugin entrypoint for JourneyMap
 */
@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public class JourneymapPlugin implements IClientPlugin
{
    private Journeymap jmap;
    @SuppressWarnings("unused")
    private EventListener listener;

    @Override
    public void initialize(@NotNull final IClientAPI api)
    {
        this.jmap = new Journeymap(api);
        this.listener = new EventListener(this.jmap);

        ClientEventRegistry.MAPPING_EVENT.subscribe(MOD_ID, this::onMappingEvent);
        ClientEventRegistry.INFO_SLOT_REGISTRY_EVENT_EVENT.subscribe(MOD_ID, this::onRegistryEvent);
        
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

    private void onRegistryEvent(final RegistryEvent event)
    {
        if (RegistryEvent.RegistryType.OPTIONS.equals(event.getRegistryType()))
        {
            this.jmap.setOptions(new JourneymapOptions());
        }
        else if (RegistryEvent.RegistryType.INFO_SLOT.equals(event.getRegistryType()))
        {
            final RegistryEvent.InfoSlotRegistryEvent infoSlotRegistry = (RegistryEvent.InfoSlotRegistryEvent) event;
            infoSlotRegistry.register(MOD_ID, "com.minecolonies.coremod.journeymap.currentcolony", 2500, ColonyBorderMapping::getCurrentColony);
        }
    }
}
