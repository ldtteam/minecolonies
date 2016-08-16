package com.schematica.client.renderer.chunk.overlay;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface ISchematicRenderChunkFactory extends IRenderChunkFactory
{
    RenderOverlay makeRenderOverlay(World world, RenderGlobal renderGlobal, BlockPos pos, int index);
}
