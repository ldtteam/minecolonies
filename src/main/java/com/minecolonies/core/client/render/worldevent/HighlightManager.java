package com.minecolonies.core.client.render.worldevent;

import com.minecolonies.core.client.render.worldevent.highlightmanager.IHighlightRenderData;

import java.time.Duration;
import java.util.*;

public class HighlightManager
{
    /**
     * A position to highlight with a group and key.
     */
    private static final Map<String, Map<String, HighlightRenderDataContainer>> HIGHLIGHT_ITEMS = new HashMap<>();

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

        for (final Iterator<Map<String, HighlightRenderDataContainer>> groups = HIGHLIGHT_ITEMS.values().iterator(); groups.hasNext(); )
        {
            final Map<String, HighlightRenderDataContainer> group = groups.next();

            for (final Iterator<HighlightRenderDataContainer> containers = group.values().iterator(); containers.hasNext(); )
            {
                final HighlightRenderDataContainer renderDataContainer = containers.next();

                renderDataContainer.attemptStart(context);
                IHighlightRenderData renderData = renderDataContainer.data;

                if (renderDataContainer.isExpired(worldTime))
                {
                    renderData.stopRender(context);
                    containers.remove();
                }
                else
                {
                    renderData.render(context);
                }
            }

            if (group.isEmpty())
            {
                groups.remove();
            }
        }
    }

    /**
     * Clears all highlight items for the given group key.
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
     * @param key    the group key of the item to render.
     * @param subKey the subkey of the item within the group (if non-unique, an existing item will be replaced).
     * @param data   the highlight render data.
     */
    public static void addHighlight(final String key, final String subKey, final IHighlightRenderData data)
    {
        HIGHLIGHT_ITEMS.computeIfAbsent(key, k -> new HashMap<>()).put(subKey, new HighlightRenderDataContainer(data));
    }

    /**
     * Internal container for managing highlight renderer data.
     */
    private static class HighlightRenderDataContainer
    {
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
        private HighlightRenderDataContainer(IHighlightRenderData data)
        {
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
