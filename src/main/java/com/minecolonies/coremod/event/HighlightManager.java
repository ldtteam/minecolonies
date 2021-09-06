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

import java.util.*;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class HighlightManager
{
    /**
     * A position to highlight with a unique id.
     */
    @Nullable
    public static final Map<String, List<TimedBoxRenderData>> HIGHLIGHT_MAP = new HashMap<>();

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
            for (final Iterator<List<TimedBoxRenderData>> categoryIterator = HIGHLIGHT_MAP.values().iterator(); categoryIterator.hasNext(); )
            {
                final List<TimedBoxRenderData> boxes = categoryIterator.next();
                for (final Iterator<TimedBoxRenderData> boxListIterator = boxes.iterator(); boxListIterator.hasNext(); )
                {
                    final TimedBoxRenderData boxRenderData = boxListIterator.next();
                    if (boxRenderData.removalTimePoint <= worldTime)
                    {
                        boxListIterator.remove();
                    }
                    else
                    {
                        RenderUtils.renderBox(boxRenderData.pos,
                          boxRenderData.pos,
                          boxRenderData.getRed(),
                          boxRenderData.getGreen(),
                          boxRenderData.getBlue(),
                          1.0F,
                          0.002D,
                          event.getMatrixStack(),
                          linesWithoutCullAndDepth.get());

                        if (!boxRenderData.text.isEmpty())
                        {
                            IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
                            RenderUtils.renderDebugText(boxRenderData.pos, boxRenderData.text, event.getMatrixStack(), true, 3, buffer);
                            RenderSystem.disableDepthTest();
                            buffer.endBatch();
                            RenderSystem.enableDepthTest();
                        }
                    }
                }

                if (boxes.isEmpty())
                {
                    categoryIterator.remove();
                }
            }
        }
        renderBuffer.endBatch();
    }

    /**
     * Box data for rendering
     */
    public static class TimedBoxRenderData
    {
        /**
         * List of strings to display
         */
        private List<String> text = new ArrayList<>();

        /**
         * Position to display at
         */
        private BlockPos pos = BlockPos.ZERO;

        /**
         * Timepoint of removal (world gametime)
         */
        private long removalTimePoint = 0;

        /**
         * Color code for the box
         */
        private int hexColor = 0xFFFFFF;

        public TimedBoxRenderData addText(final String text)
        {
            this.text.add(text);
            return this;
        }

        public TimedBoxRenderData setRemovalTimePoint(final long removalTimePoint)
        {
            this.removalTimePoint = removalTimePoint;
            return this;
        }

        public TimedBoxRenderData setPos(final BlockPos pos)
        {
            this.pos = pos;
            return this;
        }

        public TimedBoxRenderData setColor(final int hexColor)
        {
            this.hexColor = hexColor;
            return this;
        }

        /**
         * Get red %
         *
         * @return
         */
        private float getRed()
        {
            return ((hexColor >> 16) & 255) / 255f;
        }

        /**
         * Get green %
         *
         * @return
         */
        private float getGreen()
        {
            return ((hexColor >> 8) & 255) / 255f;
        }

        /**
         * Get blue %
         *
         * @return
         */
        private float getBlue()
        {
            return ((hexColor) & 255) / 255f;
        }
    }

    /**
     * Adds a box to be rendered for the given category
     *
     * @param category
     * @param data
     */
    public static void addRenderBox(final String category, final TimedBoxRenderData data)
    {
        HIGHLIGHT_MAP.computeIfAbsent(category, k -> new ArrayList<>()).add(data);
    }

    /**
     * Clears all boxes of a category
     *
     * @param category
     */
    public static void clearCategory(final String category)
    {
        HIGHLIGHT_MAP.remove(category);
    }
}
