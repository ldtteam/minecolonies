package com.schematica.client.renderer.chunk;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.EnumWorldBlockLayer;

public class CompiledOverlay extends CompiledChunk
{
    @Override
    public void setLayerStarted(final EnumWorldBlockLayer layer)
    {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT)
        {
            super.setLayerStarted(layer);
        }
    }

    @Override
    public void setLayerUsed(final EnumWorldBlockLayer layer)
    {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT)
        {
            super.setLayerUsed(layer);
        }
    }

    @Override
    public boolean isLayerStarted(final EnumWorldBlockLayer layer)
    {
        return layer == EnumWorldBlockLayer.TRANSLUCENT && super.isLayerStarted(layer);
    }

    @Override
    public boolean isLayerEmpty(final EnumWorldBlockLayer layer)
    {
        return layer == EnumWorldBlockLayer.TRANSLUCENT && super.isLayerEmpty(layer);
    }
}
