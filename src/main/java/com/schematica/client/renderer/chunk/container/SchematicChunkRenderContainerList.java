package com.schematica.client.renderer.chunk.container;

import com.schematica.client.renderer.chunk.overlay.RenderOverlay;
import com.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.opengl.GL11;

public class SchematicChunkRenderContainerList extends SchematicChunkRenderContainer {
    @Override
    public void renderChunkLayer(final EnumWorldBlockLayer layer) {
        if (this.initialized) {
            for (final RenderChunk renderchunk : this.renderChunks) {
                final ListedRenderChunk listedRenderChunk = (ListedRenderChunk) renderchunk;
                GlStateManager.pushMatrix();
                preRenderChunk(renderchunk);
                GL11.glCallList(listedRenderChunk.getDisplayList(layer, listedRenderChunk.getCompiledChunk()));
                GlStateManager.popMatrix();
            }

            GlStateManager.resetColor();
            this.renderChunks.clear();
        }
    }

    @Override
    public void renderOverlay() {
        if (this.initialized) {
            for (final RenderOverlay renderOverlay : this.renderOverlays) {
                final RenderOverlayList renderOverlayList = (RenderOverlayList) renderOverlay;
                GlStateManager.pushMatrix();
                preRenderChunk(renderOverlay);
                GL11.glCallList(renderOverlayList.getDisplayList(EnumWorldBlockLayer.TRANSLUCENT, renderOverlayList.getCompiledChunk()));
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.resetColor();
        this.renderOverlays.clear();
    }
}
