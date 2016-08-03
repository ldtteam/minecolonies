package com.schematica.client.renderer.chunk.overlay;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class RenderOverlayList extends RenderOverlay {
    private final int displayList = GLAllocation.generateDisplayLists(1);

    public RenderOverlayList(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
        super(world, renderGlobal, pos, index);
    }

    public int getDisplayList(final EnumWorldBlockLayer layer, final CompiledChunk compiledChunk) {
        return !compiledChunk.isLayerEmpty(layer) ? this.displayList : -1;
    }

    @Override
    public void deleteGlResources() {
        super.deleteGlResources();
        GLAllocation.deleteDisplayLists(this.displayList);
    }
}
