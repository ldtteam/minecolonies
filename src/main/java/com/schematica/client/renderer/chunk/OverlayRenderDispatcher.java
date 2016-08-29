package com.schematica.client.renderer.chunk;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumWorldBlockLayer;

public class OverlayRenderDispatcher extends ChunkRenderDispatcher
{
    @Override
    public ListenableFuture<Object> uploadChunk(
            final EnumWorldBlockLayer layer,
            final WorldRenderer worldRenderer,
            final RenderChunk renderChunk,
            final CompiledChunk compiledChunk)
    {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() || OpenGlHelper.useVbo())
        {
            return super.uploadChunk(layer, worldRenderer, renderChunk, compiledChunk);
        }

        uploadDisplayList(worldRenderer, ((RenderOverlayList) renderChunk).getDisplayList(layer, compiledChunk), renderChunk);

        worldRenderer.setTranslation(0.0, 0.0, 0.0);
        return Futures.immediateFuture(null);
    }
}
