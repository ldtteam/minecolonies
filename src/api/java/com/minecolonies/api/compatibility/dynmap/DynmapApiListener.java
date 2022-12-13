package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.util.Log;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;

import java.util.function.Consumer;

/**
 * Dynmap integration class to connect Minecolonies to Dynmap it's API listening system.
 * This way Dynmap can invoke {@link DynmapApiListener#apiEnabled} in order to
 * start generating markers.
 */
public class DynmapApiListener extends DynmapCommonAPIListener
{
    private final Consumer<DynmapIntegration> integrationConsumer;

    /**
     * Constructor containing a consumer callback which is invoked whenever Dynmap is ready
     * to start creating markers.
     *
     * @param integrationConsumer The consumer instance.
     */
    public DynmapApiListener(Consumer<DynmapIntegration> integrationConsumer)
    {
        this.integrationConsumer = integrationConsumer;
    }

    public void registerListener()
    {
        DynmapCommonAPIListener.register(this);
    }

    @Override
    public void apiEnabled(final DynmapCommonAPI dynmapCommonAPI)
    {
        Log.getLogger().info("Dynmap API enabled, registering markers...");
        MarkerAPI markerApi = dynmapCommonAPI.getMarkerAPI();

        integrationConsumer.accept(new DynmapIntegration(markerApi));
    }
}