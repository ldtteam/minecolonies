package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.util.Log;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;

import java.util.function.Consumer;

public class DynmapApiListener extends DynmapCommonAPIListener
{
    private final Consumer<DynmapIntegration> integrationConsumer;

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
        var markerApi = dynmapCommonAPI.getMarkerAPI();

        integrationConsumer.accept(new DynmapIntegration(markerApi));
    }
}