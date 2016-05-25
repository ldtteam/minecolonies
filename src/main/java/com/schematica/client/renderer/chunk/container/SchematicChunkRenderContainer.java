package com.schematica.client.renderer.chunk.container;

import com.google.common.collect.Lists;
import com.schematica.client.renderer.chunk.overlay.RenderOverlay;
import net.minecraft.client.renderer.ChunkRenderContainer;

import java.util.List;

public abstract class SchematicChunkRenderContainer extends ChunkRenderContainer {
    final List<RenderOverlay> renderOverlays = Lists.newArrayListWithCapacity(16 * 33 * 33);

    @Override
    public void initialize(final double viewEntityX, final double viewEntityY, final double viewEntityZ) {
        super.initialize(viewEntityX, viewEntityY, viewEntityZ);
        this.renderOverlays.clear();
    }

    public void addRenderOverlay(final RenderOverlay renderOverlay) {
        this.renderOverlays.add(renderOverlay);
    }

    public abstract void renderOverlay();
}
