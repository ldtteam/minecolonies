package com.schematica.client.renderer.chunk.proxy;

import com.schematica.client.renderer.SchematicRenderCache;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SchematicRenderChunkList extends ListedRenderChunk
{
    public SchematicRenderChunkList(final World world, final RenderGlobal renderGlobal, final int index)
    {
        super(world, renderGlobal, index);
    }

    @Override
    public void rebuildChunk(final float x, final float y, final float z, final ChunkCompileTaskGenerator generator)
    {
        generator.getLock().lock();

        try
        {
            if (generator.getStatus() == ChunkCompileTaskGenerator.Status.COMPILING)
            {
                final BlockPos from = getPosition();
                final SchematicWorld schematic = (SchematicWorld) this.getWorld();

                if (from.getX() < 0 || from.getZ() < 0 || from.getX() >= schematic.getWidth() || from.getZ() >= schematic.getLength())
                {
                    final SetVisibility visibility = new SetVisibility();
                    visibility.setAllVisible(true);

                    final CompiledChunk dummy = new CompiledChunk();
                    dummy.setVisibility(visibility);

                    generator.setCompiledChunk(dummy);
                    return;
                }
            }
        }
        finally
        {
            generator.getLock().unlock();
        }

        super.rebuildChunk(x, y, z, generator);
    }

    @Override
    protected ChunkCache createRegionRenderCache(final World world, final BlockPos from, final BlockPos to, final int subtract)
    {
        return (ChunkCache) new SchematicRenderCache(world, from, to, subtract);
    }
}
