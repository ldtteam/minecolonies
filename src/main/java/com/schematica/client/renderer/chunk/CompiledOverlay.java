package com.schematica.client.renderer.chunk;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockRenderLayer;

public class CompiledOverlay extends CompiledChunk
{
    @Override
    public void setLayerUsed(final BlockRenderLayer layer)
    {
        if (layer == BlockRenderLayer.TRANSLUCENT)
        {
            super.setLayerUsed(layer);
        }
    }

    @Override
    public boolean isLayerEmpty(final BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.TRANSLUCENT && super.isLayerEmpty(layer);
    }

    @Override
    public void setLayerStarted(final BlockRenderLayer layer)
    {
        if (layer == BlockRenderLayer.TRANSLUCENT)
        {
            super.setLayerStarted(layer);
        }
    }

    @Override
    public boolean isLayerStarted(final BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.TRANSLUCENT && super.isLayerStarted(layer);
    }
}
