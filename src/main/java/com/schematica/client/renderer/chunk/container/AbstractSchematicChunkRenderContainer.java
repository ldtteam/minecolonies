package com.schematica.client.renderer.chunk.container;

import com.google.common.collect.Lists;
import com.schematica.client.renderer.chunk.overlay.RenderOverlay;
import net.minecraft.client.renderer.ChunkRenderContainer;

import java.util.List;

/**
 * Holds Overlays to render.
 */
public abstract class AbstractSchematicChunkRenderContainer extends ChunkRenderContainer
{
    protected final List<RenderOverlay> renderOverlays = Lists.newArrayListWithCapacity(16 * 33 * 33);

    @Override
    public void initialize(final double viewEntityX, final double viewEntityY, final double viewEntityZ)
    {
        super.initialize(viewEntityX, viewEntityY, viewEntityZ);
        this.renderOverlays.clear();
    }

    /**
     * Add an overlay to the rendered schematic.
     *
     * @param renderOverlay the overlay to render.
     */
    public void addRenderOverlay(final RenderOverlay renderOverlay)
    {
        this.renderOverlays.add(renderOverlay);
    }

    /**
     * Render the schematic overlay.
     */
    public abstract void renderOverlay();
}
