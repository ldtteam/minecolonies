package com.minecolonies.coremod.event;

import com.ldtteam.structurize.util.RenderUtils;
import com.minecolonies.api.util.Tuple;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class HighlightManager
{
    /**
     * A position to highlight with a unique id.
     */
    @Nullable
    public static final Map<String, Tuple<BlockPos, Long>> HIGHLIGHT_MAP = new HashMap<>();

    /**
     * Render buffers.
     */
    public static final  RenderBuffers        renderBuffers            = new RenderBuffers();
    private static final MultiBufferSource.BufferSource   renderBuffer             = renderBuffers.bufferSource();
    private static final Supplier<VertexConsumer> linesWithoutCullAndDepth = () -> renderBuffer.getBuffer(RenderUtils.LINES_GLINT);

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        if (!HIGHLIGHT_MAP.isEmpty())
        {
            final long worldTime = Minecraft.getInstance().level.getGameTime();
            for (final Map.Entry<String, Tuple<BlockPos, Long>> entry : new ArrayList<>(HIGHLIGHT_MAP.entrySet()))
            {
                if (entry.getValue().getB() <= worldTime)
                {
                    HIGHLIGHT_MAP.remove(entry.getKey());
                }
                else
                {
                    RenderUtils.renderBox(entry.getValue().getA(), entry.getValue().getA(), 0, 1, 0, 1.0F, 0.002D, event.getMatrixStack(), linesWithoutCullAndDepth.get());
                }
            }
        }
        renderBuffer.endBatch();
    }
}
