package com.minecolonies.coremod.client.render.worldevent.highlightmanager;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.coremod.client.render.worldevent.ColonyWorldRenderMacros;
import com.minecolonies.coremod.client.render.worldevent.WorldEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

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
    public void startRender(final WorldEventContext context)
    {
        // No-op
    }

    @Override
    public void render(final WorldEventContext context)
    {
        final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        ColonyWorldRenderMacros.renderLineBox(context.getPoseStack(), buffer, new AABB(pos), 0.025f, argbColor, true);
        if (!text.isEmpty())
        {
            WorldRenderMacros.renderDebugText(pos, text, context.getPoseStack(), true, 3, buffer);
        }
        ColonyWorldRenderMacros.endRenderLineBox(buffer);
        buffer.endBatch();
    }

    @Override
    public void stopRender(final WorldEventContext context)
    {
        // No-op
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
