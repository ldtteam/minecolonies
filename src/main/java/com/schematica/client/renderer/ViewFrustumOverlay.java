package com.schematica.client.renderer;

import com.schematica.client.renderer.chunk.overlay.ISchematicRenderChunkFactory;
import com.schematica.client.renderer.chunk.overlay.RenderOverlay;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ViewFrustumOverlay extends ViewFrustum {
    public RenderOverlay[] renderOverlays;

    public ViewFrustumOverlay(final World world, final int renderDistanceChunks, final RenderGlobal renderGlobal, final ISchematicRenderChunkFactory renderChunkFactory) {
        super(world, renderDistanceChunks, renderGlobal, renderChunkFactory);
        createRenderOverlays(renderChunkFactory);
    }

    protected void createRenderOverlays(final ISchematicRenderChunkFactory renderChunkFactory) {
        final int amount = this.countChunksX * this.countChunksY * this.countChunksZ;
        this.renderOverlays = new RenderOverlay[amount];
        int count = 0;

        for (int x = 0; x < this.countChunksX; x++) {
            for (int y = 0; y < this.countChunksY; y++) {
                for (int z = 0; z < this.countChunksZ; z++) {
                    final int index = (z * this.countChunksY + y) * this.countChunksX + x;
                    final BlockPos pos = new BlockPos(x * 16, y * 16, z * 16);
                    this.renderOverlays[index] = renderChunkFactory.makeRenderOverlay(this.world, this.renderGlobal, pos, count++);
                }
            }
        }
    }

    @Override
    public void deleteGlResources() {
        super.deleteGlResources();

        for (final RenderOverlay renderOverlay : this.renderOverlays) {
            renderOverlay.deleteGlResources();
        }
    }

    @Override
    public void updateChunkPositions(final double viewEntityX, final double viewEntityZ) {
        super.updateChunkPositions(viewEntityX, viewEntityZ);

        final int xx = MathHelper.floor_double(viewEntityX) - 8;
        final int zz = MathHelper.floor_double(viewEntityZ) - 8;
        final int yy = this.countChunksX * 16;

        for (int chunkX = 0; chunkX < this.countChunksX; chunkX++) {
            final int x = getPosition(xx, yy, chunkX);

            for (int chunkZ = 0; chunkZ < this.countChunksZ; chunkZ++) {
                final int z = getPosition(zz, yy, chunkZ);

                for (int chunkY = 0; chunkY < this.countChunksY; chunkY++) {
                    final int y = chunkY * 16;
                    final RenderOverlay renderOverlay = this.renderOverlays[(chunkZ * this.countChunksY + chunkY) * this.countChunksX + chunkX];
                    final BlockPos blockpos = new BlockPos(x, y, z);

                    if (!blockpos.equals(renderOverlay.getPosition())) {
                        renderOverlay.setPosition(blockpos);
                    }
                }
            }
        }
    }

    private int getPosition(final int xz, final int y, final int chunk) {
        final int chunks = chunk * 16;
        int i = chunks - xz + y / 2;

        if (i < 0) {
            i -= y - 1;
        }

        return chunks - i / y * y;
    }

    @Override
    public void markBlocksForUpdate(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ) {
        super.markBlocksForUpdate(fromX, fromY, fromZ, toX, toY, toZ);

        final int x0 = MathHelper.bucketInt(fromX, 16);
        final int y0 = MathHelper.bucketInt(fromY, 16);
        final int z0 = MathHelper.bucketInt(fromZ, 16);
        final int x1 = MathHelper.bucketInt(toX, 16);
        final int y1 = MathHelper.bucketInt(toY, 16);
        final int z1 = MathHelper.bucketInt(toZ, 16);

        for (int xi = x0; xi <= x1; ++xi) {
            int x = xi % this.countChunksX;

            if (x < 0) {
                x += this.countChunksX;
            }

            for (int yi = y0; yi <= y1; ++yi) {
                int y = yi % this.countChunksY;

                if (y < 0) {
                    y += this.countChunksY;
                }

                for (int zi = z0; zi <= z1; ++zi) {
                    int z = zi % this.countChunksZ;

                    if (z < 0) {
                        z += this.countChunksZ;
                    }

                    final int index = (z * this.countChunksY + y) * this.countChunksX + x;
                    final RenderOverlay renderOverlay = this.renderOverlays[index];
                    renderOverlay.setNeedsUpdate(true);
                }
            }
        }
    }

    public RenderOverlay getRenderOverlay(final BlockPos pos) {
        int x = MathHelper.bucketInt(pos.getX(), 16);
        final int y = MathHelper.bucketInt(pos.getY(), 16);
        int z = MathHelper.bucketInt(pos.getZ(), 16);

        if (y >= 0 && y < this.countChunksY) {
            x %= this.countChunksX;

            if (x < 0) {
                x += this.countChunksX;
            }

            z %= this.countChunksZ;

            if (z < 0) {
                z += this.countChunksZ;
            }

            final int index = (z * this.countChunksY + y) * this.countChunksX + x;
            return this.renderOverlays[index];
        } else {
            return null;
        }
    }
}
