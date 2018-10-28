package com.minecolonies.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.minecolonies.blockout.Log;
import com.minecolonies.structures.lib.TemplateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * The wayPointTemplate render handler on the client side.
 */
public final class TemplateRenderHandler
{
    /**
     * A static instance on the client.
     */
    private static final TemplateRenderHandler ourInstance = new TemplateRenderHandler();

    /**
     * The builder cache.
     */
    private final Cache<Template, TemplateTessellator> templateBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Template, TemplateTessellator>) notification -> notification.getValue().getBuffer().deleteGlBuffers())
        .build();

    /**
     * The dispatcher.
     */
    private BlockRendererDispatcher rendererDispatcher;

    /**
     * Cached entity renderer.
     */
    private RenderManager entityRenderer;

    /**
     * Private constructor to hide public one.
     */
    private TemplateRenderHandler()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get the static instance.
     *
     * @return a static instance of this class.
     */
    public static TemplateRenderHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a wayPointTemplate with a rotation, mirror and offset.
     *
     * @param template      the wayPointTemplate to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Template template, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final float partialTicks, final BlockPos pos)
    {
        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }
        if (entityRenderer == null)
        {
            entityRenderer = Minecraft.getMinecraft().getRenderManager();
        }

        final TemplateBlockAccess blockAccess = new TemplateBlockAccess(template);

        try
        {
            templateBufferBuilderCache.get(template, () -> {
                final TemplateTessellator tessellator = new TemplateTessellator();
                tessellator.getBuilder().begin(GL_QUADS, DefaultVertexFormats.BLOCK);

                template.blocks.stream()
                  .map(b -> TemplateBlockAccessTransformHandler.getInstance().Transform(b))
                  .forEach(b -> rendererDispatcher.renderBlock(b.blockState, b.pos, blockAccess, tessellator.getBuilder()));

                return tessellator;
            }).draw(rotation, mirror, drawingOffset, TemplateUtils.getPrimaryBlockOffset(template));

            template.blocks.stream().filter(blockInfo -> blockInfo.tileentityData != null).map(b -> constructTileEntities(b, pos.subtract(TemplateUtils.getPrimaryBlockOffset(template)))).filter(Objects::nonNull).forEach(tileEntity -> TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, 0));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error(e);
        }
    }

    @Nullable
    private TileEntity constructTileEntities(final Template.BlockInfo info, final BlockPos pos)
    {
        final TileEntity entity = TileEntity.create(null, info.tileentityData);
        if (!(entity instanceof TileEntityBanner))
        {
            return null;
        }

        entity.setPos(pos.add(info.pos));
        return entity;
    }

    /**
     * Render a template at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param template the template.
     */
    public void drawTemplateAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Template template)
    {
        if (points.isEmpty())
        {
            return;
        }
        final EntityPlayer perspectiveEntity = Minecraft.getMinecraft().player;
        final double interpolatedEntityPosX = perspectiveEntity.lastTickPosX + (perspectiveEntity.posX - perspectiveEntity.lastTickPosX) * partialTicks;
        final double interpolatedEntityPosY = perspectiveEntity.lastTickPosY + (perspectiveEntity.posY - perspectiveEntity.lastTickPosY) * partialTicks;
        final double interpolatedEntityPosZ = perspectiveEntity.lastTickPosZ + (perspectiveEntity.posZ - perspectiveEntity.lastTickPosZ) * partialTicks;

        for (final BlockPos coord : points)
        {
            final BlockPos pos = coord.down();
            final double renderOffsetX = pos.getX() - interpolatedEntityPosX;
            final double renderOffsetY = pos.getY() - interpolatedEntityPosY;
            final double renderOffsetZ = pos.getZ() - interpolatedEntityPosZ;
            final Vector3d renderOffset = new Vector3d();
            renderOffset.x = renderOffsetX;
            renderOffset.y = renderOffsetY;
            renderOffset.z = renderOffsetZ;

            draw(template, Rotation.NONE, Mirror.NONE, renderOffset, partialTicks, coord);
        }
    }
}
