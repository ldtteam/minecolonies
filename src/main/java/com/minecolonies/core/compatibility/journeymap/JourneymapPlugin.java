package com.minecolonies.core.compatibility.journeymap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Plugin entrypoint for JourneyMap
 */
@ClientPlugin
public class JourneymapPlugin implements IClientPlugin
{
    private Journeymap jmap;
    private EventListener listener;

    @Override
    public void initialize(@NotNull final IClientAPI api)
    {
        this.jmap = new Journeymap(api);
        this.listener = new EventListener(this.jmap);

        api.subscribe(MOD_ID, EnumSet.of(
                ClientEvent.Type.MAPPING_STARTED,
                ClientEvent.Type.MAPPING_STOPPED,
                ClientEvent.Type.REGISTRY));
    }

    @Override
    public String getModId()
    {
        return MOD_ID;
    }

    @Override
    public void onEvent(@NotNull final ClientEvent event)
    {
        switch (event.type)
        {
            case MAPPING_STARTED:
                ColonyBorderMapping.load(this.jmap, event.dimension);
                break;

            case MAPPING_STOPPED:
                ColonyBorderMapping.unload(this.jmap, event.dimension);
                ColonyDeathpoints.unload(this.jmap, event.dimension);
                break;

            case REGISTRY:
                final RegistryEvent registryEvent = (RegistryEvent) event;
                if (RegistryEvent.RegistryType.OPTIONS.equals(registryEvent.getRegistryType()))
                {
                    this.jmap.setOptions(new JourneymapOptions());
                }
                else if (RegistryEvent.RegistryType.INFO_SLOT.equals(registryEvent.getRegistryType()))
                {
                    final RegistryEvent.InfoSlotRegistryEvent infoSlotRegistry = (RegistryEvent.InfoSlotRegistryEvent) registryEvent;
                    infoSlotRegistry.register(MOD_ID, "com.minecolonies.coremod.journeymap.currentcolony", 2500, ColonyBorderMapping::getCurrentColony);
                }
                break;
        }
    }
}
