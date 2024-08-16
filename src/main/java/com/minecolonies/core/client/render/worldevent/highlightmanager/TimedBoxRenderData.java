package com.minecolonies.core.client.render.worldevent.highlightmanager;

import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import net.minecraft.core.BlockPos;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Highlight render data for marking blocks in the world with potential warnings on them.
 */
public class TimedBoxRenderData implements IHighlightRenderData
{
    /**
     * List of texts to display.
     */
    private final List<String> text = new ArrayList<>();

    /**
     * Position where to render the box.
     */
    private final BlockPos pos;

    /**
     * How long the box should stay.
     */
    private Duration duration;

    /**
     * The colour at which the box should render.
     */
    private int argbColor = 0xffffffff;

    /**
     * Default constructor.
     */
    public TimedBoxRenderData(final BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void render(final WorldEventContext context)
    {
        context.pushPoseCameraToPos(pos);
        context.renderLineBoxWithShadow(BlockPos.ZERO, argbColor, WorldEventContext.DEFAULT_LINE_WIDTH);
        if (!text.isEmpty())
        {
            context.renderDebugText(BlockPos.ZERO, text, true, 3);
        }
        context.popPose();
    }

    @Override
    public Duration getDuration()
    {
        return duration;
    }

    /**
     * Duration of the box.
     */
    public TimedBoxRenderData setDuration(final Duration duration)
    {
        this.duration = duration;
        return this;
    }

    /**
     * List of strings to display
     */
    public TimedBoxRenderData addText(final String text)
    {
        this.text.add(text);
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
