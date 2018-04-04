package com.minecolonies.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.minecolonies.blockout.Log;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class TemplateRenderHandler {
    private static TemplateRenderHandler ourInstance = new TemplateRenderHandler();

    public static TemplateRenderHandler getInstance() {
        return ourInstance;
    }

    private final Cache<Template, TemplateTessellator> templateBufferBuilderCache = CacheBuilder.newBuilder().maximumSize(50).removalListener(new RemovalListener<Template, TemplateTessellator>() {
        @Override
        public void onRemoval(RemovalNotification<Template, TemplateTessellator> notification) {
            notification.getValue().getBuffer().deleteGlBuffers();
        }
    }).build();
    private final BlockRendererDispatcher rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

    private TemplateRenderHandler() {
    }

    public void draw(Structure structure, double viewEntityX, double viewEntityY, double viewEntityZ) {
        PlacementSettings settings = structure.getSettings();
        Template template = structure.getTemplate();
        TemplateBlockAccess blockAccess = new TemplateBlockAccess(template);

        try {
            templateBufferBuilderCache.get(template, () -> {
                TemplateTessellator tessellator = new TemplateTessellator();
                tessellator.getBuilder().begin(7, DefaultVertexFormats.BLOCK);

                template.blocks.stream().forEach(b -> {
                    rendererDispatcher.renderBlock(b.blockState, b.pos, blockAccess, tessellator.getBuilder());
                });

                return tessellator;
            }).draw(viewEntityX, viewEntityY, viewEntityZ);
        } catch (ExecutionException e) {
            Log.getLogger().error(e);
        }


    }
}
