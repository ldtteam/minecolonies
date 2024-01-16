package com.minecolonies.core.client.render.worldevent.highlightmanager;

import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Interface for highlightable items.
 */
public interface IHighlightRenderData
{
    /**
     * Indicate the render data it should start rendering.
     */
    default void startRender(final WorldEventContext context) {}

    /**
     * Indicate the render data it should continue rendering.
     */
    void render(final WorldEventContext context);

    /**
     * Indicate the render data it should stop rendering.
     */
    default void stopRender(final WorldEventContext context) {}

    /**
     * Get the duration of the highlight.
     *
     * @return a duration instance, or null for an infinite highlight (should be manually cancelled).
     */
    @Nullable
    Duration getDuration();
}
