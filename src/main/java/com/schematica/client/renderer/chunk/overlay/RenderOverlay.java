package com.schematica.client.renderer.chunk.overlay;

import com.schematica.client.renderer.chunk.CompiledOverlay;
import com.schematica.client.world.SchematicWorld;
import com.schematica.core.client.renderer.GeometryMasks;
import com.schematica.core.client.renderer.GeometryTessellator;
import com.schematica.handler.ConfigurationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderOverlay extends RenderChunk
{
    private final VertexBuffer vertexBuffer;

    public RenderOverlay(final World world, final RenderGlobal renderGlobal, final int index)
    {
        super(world, renderGlobal, index);
        this.vertexBuffer = OpenGlHelper.useVbo() ? new VertexBuffer(DefaultVertexFormats.POSITION_COLOR) : null;
    }

    @Override
    public VertexBuffer getVertexBufferByLayer(final int layer)
    {
        return this.vertexBuffer;
    }

    @Override
    public void rebuildChunk(final float x, final float y, final float z, final ChunkCompileTaskGenerator generator)
    {
        final CompiledOverlay compiledOverlay = new CompiledOverlay();
        final BlockPos from = getPosition();
        final BlockPos to = from.add(15, 15, 15);
        generator.getLock().lock();
        ChunkCache ChunkCache;
        final SchematicWorld schematic = (SchematicWorld) this.getWorld();

        try
        {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING)
            {
                return;
            }

            if (from.getX() < 0 || from.getZ() < 0 || from.getX() >= schematic.getWidth() || from.getZ() >= schematic.getLength())
            {
                generator.setCompiledChunk(CompiledChunk.DUMMY);
                return;
            }

            ChunkCache = new ChunkCache(this.getWorld(), from.add(-1, -1, -1), to.add(1, 1, 1), 1);
            generator.setCompiledChunk(compiledOverlay);
        }
        finally
        {
            generator.getLock().unlock();
        }

        final VisGraph visgraph = new VisGraph();

        if (!ChunkCache.extendedLevelsInChunkCache())
        {
            increamentRenderChunkedUpdated();

            final World mcWorld = Minecraft.getMinecraft().theWorld;

            final BlockRenderLayer layer = BlockRenderLayer.TRANSLUCENT;
            final net.minecraft.client.renderer.VertexBuffer worldRenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(layer);

            GeometryTessellator.setStaticDelta(ConfigurationHandler.blockDelta);

            for (final BlockPos pos : BlockPos.getAllInBox(from, to))
            {
                if ((schematic.isRenderingLayer && schematic.renderingLayer != pos.getY()) || !schematic.isInside(pos))
                {
                    continue;
                }

                boolean render = false;
                int sides = 0;
                int color = 0;

                final IBlockState schBlockState = schematic.getBlockState(pos);
                final Block schBlock = schBlockState.getBlock();

                if (schBlockState.isOpaqueCube())
                {
                    visgraph.setOpaqueCube(pos);
                }

                final BlockPos mcPos = pos.add(schematic.position);
                final IBlockState mcBlockState = mcWorld.getBlockState(mcPos);
                final Block mcBlock = mcBlockState.getBlock();

                final boolean isSchAirBlock = schematic.isAirBlock(pos);
                final boolean isMcAirBlock = mcWorld.isAirBlock(mcPos);

                if (!isMcAirBlock && isSchAirBlock && ConfigurationHandler.highlightAir)
                {
                    render = true;
                    color = 0xBF00BF;

                    sides = getSides(mcBlock, mcWorld, mcPos, sides);
                }

                if (!render)
                {
                    if (ConfigurationHandler.highlight)
                    {
                        if (!isMcAirBlock)
                        {
                            if (schBlock != mcBlock)
                            {
                                render = true;
                                color = 0xFF0000;
                            }
                            else if (schBlock.getMetaFromState(schBlockState) != mcBlock.getMetaFromState(mcBlockState))
                            {
                                render = true;
                                color = 0xBF5F00;
                            }
                        }
                        else if (!isSchAirBlock)
                        {
                            render = true;
                            color = 0x00BFFF;
                        }
                    }

                    if (render)
                    {
                        sides = getSides(schBlock, schematic, pos, sides);
                    }
                }

                if (render && sides != 0)
                {
                    if (!compiledOverlay.isLayerStarted(layer))
                    {
                        compiledOverlay.setLayerStarted(layer);
                        preRenderBlocks(worldRenderer, from);
                    }

                    GeometryTessellator.drawCuboid(worldRenderer, pos, sides, 0x3F000000 | color);
                    compiledOverlay.setLayerUsed(layer);
                }
            }

            if (compiledOverlay.isLayerStarted(layer))
            {
                postRenderBlocks(layer, x, y, z, worldRenderer, compiledOverlay);
            }
        }

        compiledOverlay.setVisibility(visgraph.computeVisibility());
    }

    private static void increamentRenderChunkedUpdated()
    {
        ++renderChunksUpdated;
    }

    private static int getSides(final Block block, final World world, final BlockPos pos, int s)
    {
        int sides = s;
        IBlockState blockState = world.getBlockState(pos);
        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.DOWN), EnumFacing.DOWN))
        {
            sides |= GeometryMasks.Quad.DOWN;
        }

        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.UP), EnumFacing.UP))
        {
            sides |= GeometryMasks.Quad.UP;
        }

        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.NORTH), EnumFacing.NORTH))
        {
            sides |= GeometryMasks.Quad.NORTH;
        }

        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.SOUTH), EnumFacing.SOUTH))
        {
            sides |= GeometryMasks.Quad.SOUTH;
        }

        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.WEST), EnumFacing.WEST))
        {
            sides |= GeometryMasks.Quad.WEST;
        }

        if (blockState.shouldSideBeRendered(world, pos.offset(EnumFacing.EAST), EnumFacing.EAST))
        {
            sides |= GeometryMasks.Quad.EAST;
        }

        return sides;
    }

    @Override
    public void preRenderBlocks(final net.minecraft.client.renderer.VertexBuffer worldRendererIn, final BlockPos pos)
    {
        worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRendererIn.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    @Override
    public void deleteGlResources()
    {
        super.deleteGlResources();

        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.deleteGlBuffers();
        }
    }
}
