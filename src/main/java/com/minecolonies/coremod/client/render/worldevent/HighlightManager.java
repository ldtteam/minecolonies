package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class HighlightManager
{
    /**
     * A position to highlight with a unique id.
     */
    private static final Map<String, List<TimedBoxRenderData>> HIGHLIGHT_MAP = new HashMap<>();

    /**
     * Highlights positions
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (HIGHLIGHT_MAP.isEmpty())
        {
            return;
        }

        final long worldTime = ctx.clientLevel.getGameTime();

        for (final Iterator<List<TimedBoxRenderData>> categoryIterator = HIGHLIGHT_MAP.values().iterator(); categoryIterator.hasNext();)
        {
            final List<TimedBoxRenderData> boxes = categoryIterator.next();
            for (final Iterator<TimedBoxRenderData> boxListIterator = boxes.iterator(); boxListIterator.hasNext();)
            {
                final TimedBoxRenderData boxRenderData = boxListIterator.next();

                if (boxRenderData.removalTimePoint <= worldTime)
                {
                    boxListIterator.remove();
                    continue;
                }

                final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, buffer, new AABB(boxRenderData.pos), 0.025f, boxRenderData.argbColor, true);
                if (!boxRenderData.text.isEmpty())
                {
                   WorldRenderMacros.renderDebugText(boxRenderData.pos, boxRenderData.text, ctx.poseStack, true, 3, buffer);
                }
                ColonyWorldRenderMacros.endRenderLineBox(buffer);
                buffer.endBatch();
            }

            if (boxes.isEmpty())
            {
                categoryIterator.remove();
            }
        }
    }

    /**
     * Box data for rendering
     */
    public static class TimedBoxRenderData
    {
        private List<String> text = new ArrayList<>();
        private BlockPos pos = BlockPos.ZERO;
        private long removalTimePoint = 0;
        private int argbColor = 0xffffffff;

        /**
         * List of strings to display
         */
        public TimedBoxRenderData addText(final String text)
        {
            this.text.add(text);
            return this;
        }

        /**
         * Timepoint of removal (world gametime)
         */
        public TimedBoxRenderData setRemovalTimePoint(final long removalTimePoint)
        {
            this.removalTimePoint = removalTimePoint;
            return this;
        }

        /**
         * Position to display at
         */
        public TimedBoxRenderData setPos(final BlockPos pos)
        {
            this.pos = pos;
            return this;
        }

        /**
         * Color code for the box, argb format
         */
        public TimedBoxRenderData setColor(final int argbColor)
        {
            this.argbColor = argbColor;
            return this;
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
