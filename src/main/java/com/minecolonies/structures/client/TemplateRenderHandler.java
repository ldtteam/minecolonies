package com.minecolonies.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Lists;
import com.minecolonies.blockout.Log;
import com.minecolonies.structures.lib.TemplateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public final class TemplateRenderHandler
{
    private static final TemplateRenderHandler                ourInstance                = new TemplateRenderHandler();
    private final  Cache<Template, TemplateTessellator> templateBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Template, TemplateTessellator>) notification -> notification.getValue().getBuffer().deleteGlBuffers())
        .build();
    private BlockRendererDispatcher              rendererDispatcher;

    private TemplateRenderHandler()
    {
    }

    public static TemplateRenderHandler getInstance()
    {
        return ourInstance;
    }

    public void draw(final Template template, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset)
    {
        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
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
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error(e);
        }
    }
}
