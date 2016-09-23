package com.schematica.client.renderer.chunk;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

public class OverlayRenderDispatcher extends ChunkRenderDispatcher
{
    public OverlayRenderDispatcher()
    {
        super();
    }

    public OverlayRenderDispatcher(int countRenderBuilders)
    {
        super(countRenderBuilders);
    }

    @Override
    public ListenableFuture<Object> uploadChunk(
                                                 final BlockRenderLayer layer,
                                                 final VertexBuffer worldRenderer,
                                                 final RenderChunk renderChunk,
                                                 final CompiledChunk compiledChunk,
                                                 final double par5)
    {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() || OpenGlHelper.useVbo())
        {
            return super.uploadChunk(layer, worldRenderer, renderChunk, compiledChunk, par5);
        }

        uploadDisplayList(worldRenderer, ((RenderOverlayList) renderChunk).getDisplayList(layer, compiledChunk), renderChunk);

        worldRenderer.setTranslation(0.0, 0.0, 0.0);
        return Futures.immediateFuture(null);
    }
}
