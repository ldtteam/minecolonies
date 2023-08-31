package com.minecolonies.coremod.client.render.worldevent;

import com.minecolonies.coremod.client.render.worldevent.highlightmanager.IHighlightRenderData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighlightManager
{
    /**
     * A position to highlight with a unique id.
     */
    private static final Map<String, HighlightRenderDataContainer> HIGHLIGHT_ITEMS = new HashMap<>();

    /**
     * Highlights positions
     *
     * @param context rendering context
     */
    static void render(final WorldEventContext context)
    {
        if (HIGHLIGHT_ITEMS.isEmpty())
        {
            return;
        }

        final long worldTime = context.clientLevel.getGameTime();

        List<HighlightRenderDataContainer> itemsToRemove = new ArrayList<>();
        for (final HighlightRenderDataContainer renderDataContainer : HIGHLIGHT_ITEMS.values())
        {
            renderDataContainer.attemptStart(context);
            IHighlightRenderData renderData = renderDataContainer.data;

            if (renderDataContainer.isExpired(worldTime))
            {
                renderData.stopRender(context);
                itemsToRemove.add(renderDataContainer);
            }
            else
            {
                renderData.render(context);
            }
        }

        for (HighlightRenderDataContainer container : itemsToRemove)
        {
            HIGHLIGHT_ITEMS.remove(container.key);
        }
    }

    /**
     * Clears all highlight items for the given key.
     *
     * @param key the key to remove the render data for.
     */
    public static void clearHighlightsForKey(final String key)
    {
        HIGHLIGHT_ITEMS.remove(key);
    }

    /**
     * Adds a highlight item for the given key.
     *
     * @param key  the key of the item to render.
     * @param data the highlight render data.
     */
    public static void addHighlight(final String key, final IHighlightRenderData data)
    {
        HIGHLIGHT_ITEMS.put(key, new HighlightRenderDataContainer(key, data));
    }

    /**
     * Internal container for managing highlight renderer data.
     */
    private static class HighlightRenderDataContainer
    {
        /**
         * The key for this renderer.
         */
        private final String key;

        /**
         * The data for this renderer.
         */
        private final IHighlightRenderData data;

        /**
         * The time at which the highlighter was started.
         */
        private long startTime = 0;

        /**
         * Default constructor.
         */
        private HighlightRenderDataContainer(String key, IHighlightRenderData data)
        {
            this.key = key;
            this.data = data;
        }

        /**
         * Check if the highlight has expired.
         *
         * @return true if expired.
         */
        private boolean isExpired(final long worldTime)
        {
            Duration duration = data.getDuration();
            if (duration != null)
            {
                return (startTime + (duration.getSeconds() * 20)) < worldTime;
            }
            return false;
        }

        /**
         * Attempt to start the rendering of the highlight data.
         *
         * @param context the world event context.
         */
        private void attemptStart(final WorldEventContext context)
        {
            if (startTime == 0)
            {
                startTime = context.clientLevel.getGameTime();
                data.startRender(context);
            }
        }
    }
}
