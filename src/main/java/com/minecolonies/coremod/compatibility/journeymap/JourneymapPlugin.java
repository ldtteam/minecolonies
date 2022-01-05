package com.minecolonies.coremod.compatibility.journeymap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Plugin entrypoint for JourneyMap
 */
@ClientPlugin
public class JourneymapPlugin implements IClientPlugin
{
    /**
     * Access to JourneyMap api
     */
    private IClientAPI jmap;

    /**
     * Event handler
     */
    private EventListener listener;

    @Override
    public void initialize(@NotNull final IClientAPI api)
    {
        this.jmap = api;
        this.listener = new EventListener(this.jmap);

        this.jmap.subscribe(MOD_ID, EnumSet.of(ClientEvent.Type.MAPPING_STOPPED));
    }

    @Override
    public String getModId()
    {
        return MOD_ID;
    }

    @Override
    public void onEvent(@NotNull final ClientEvent event)
    {
        if (event.type.equals(ClientEvent.Type.MAPPING_STOPPED))
        {
            ColonyBorderMapping.unload(this.jmap, event.dimension);
            ColonyDeathpoints.unload(this.jmap, event.dimension);
        }
    }
}
