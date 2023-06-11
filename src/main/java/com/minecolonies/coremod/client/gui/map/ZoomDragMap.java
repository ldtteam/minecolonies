package com.minecolonies.coremod.client.gui.map;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.jetbrains.annotations.NotNull;

/**
 * Zoomable and scrollable "online map"-like view, mostly copied from BlockUI's drag view
 */
public class ZoomDragMap extends View
{
    public  double scrollX = 0d;
    public  double scrollY = 0d;
    private double scale   = 1d;

    protected int contentHeight = 0;
    protected int contentWidth  = 0;

    private double  dragFactor  = 1d;
    private boolean dragEnabled = true;

    private double  zoomFactor  = 1.1d;
    private boolean zoomEnabled = true;
    private double  minScale    = 0.02d;
    private double  maxScale    = 6d;

    /**
     * Required default constructor.
     */
    public ZoomDragMap()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public ZoomDragMap(final PaneParams params)
    {
        super(params);
        dragFactor = params.getDouble("dragfactor", dragFactor);
        dragEnabled = params.getBoolean("dragenabled", dragEnabled);
        zoomFactor = params.getDouble("zoomfactor", zoomFactor);
        zoomEnabled = params.getBoolean("zoomenabled", zoomEnabled);
        minScale = params.getDouble("minscale", minScale);
        maxScale = params.getDouble("maxscale", maxScale);
    }

    @Override
    protected boolean childIsVisible(@NotNull final Pane child)
    {
        return calcInverseAbsoluteX(child.getX()) < getInteriorWidth() && calcInverseAbsoluteY(child.getY()) < getInteriorHeight()
                 && calcInverseAbsoluteX(child.getX() + child.getWidth()) >= 0 && calcInverseAbsoluteY(child.getY() + child.getHeight()) >= 0;
    }

    /**
     * Converts X of child to scaled and scrolled X in absolute coordinates.
     */
    private double calcInverseAbsoluteX(final double xIn)
    {
        return xIn * scale - scrollX;
    }

    /**
     * Converts Y of child to scaled and scrolled Y in absolute coordinates.
     */
    private double calcInverseAbsoluteY(final double yIn)
    {
        return yIn * scale - scrollY;
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn - x + scrollX) / scale + x;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    private double calcRelativeY(final double yIn)
    {
        return (yIn - y + scrollY) / scale + y;
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        super.parseChildren(params);
        computeContentSize();
    }

    @Override
    public void addChild(final Pane child)
    {
        super.addChild(child);
        computeContentSize();
    }

    public void addChildFirst(final Pane child)
    {
        child.setWindow(getWindow());
        children.add(0, child);
        adjustChild(child);
        child.setParentView(this);
        computeContentSize();
    }

    /**
     * Compute the height in pixels of the container.
     */
    protected void computeContentSize()
    {
        contentHeight = 0;
        contentWidth = 0;

        for (@NotNull final Pane child : children)
        {
            if (child != null)
            {
                contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
                contentWidth = Math.max(contentWidth, child.getX() + child.getWidth());
            }
        }

        // Recompute scroll
        setScrollY(scrollY);
        setScrollX(scrollX);
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();
        scissorsStart(ms, contentWidth, contentHeight);

        ms.pushPose();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        final float renderScale = (float) Mth.clamp(scale, 0.26219988382999904, 6d);
        ms.scale(renderScale, renderScale, 1.0f);
        super.drawSelf(target, calcRelativeX(mx), calcRelativeY(my));

        ms.popPose();
        scissorsEnd(target);
    }

    @Override
    public void drawSelfLast(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();
        scissorsStart(ms, contentWidth, contentHeight);

        ms.pushPose();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        final float renderScale = (float) Mth.clamp(scale, 0.26219988382999904, 6d);
        ms.scale(renderScale, renderScale, 1.0f);
        super.drawSelfLast(target, calcRelativeX(mx), calcRelativeY(my));
        ms.popPose();

        scissorsEnd(target);
    }

    private void setScrollY(final double offset)
    {
        scrollY = offset;
    }

    private void setScrollX(final double offset)
    {
        scrollX = offset;
    }

    @Override
    public boolean onMouseDrag(final double startX, final double startY, final int speed, final double x, final double y)
    {
        final boolean childResult = super.onMouseDrag(startX, startY, speed, calcRelativeX(x), calcRelativeY(y));
        if (!childResult && dragEnabled)
        {
            setScrollX(scrollX - x * dragFactor);
            setScrollY(scrollY - y * dragFactor);
            return true;
        }
        return childResult;
    }

    @Override
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        final boolean childResult = super.scrollInput(wheel, (width - scrollX) / 2, (height - scrollY) / 2);
        if (!childResult && zoomEnabled)
        {
            final double childX = (width - scrollX) / 2;
            final double childY = (height - scrollY) / 2;
            final double oldX = (childX + scrollX) / scale;
            final double oldY = (childY + scrollY) / scale;
            scale = wheel < 0 ? scale / zoomFactor : scale * zoomFactor;
            scale = Mth.clamp(scale, minScale, maxScale);
            setScrollX(oldX * scale - childX);
            setScrollY(oldY * scale - childY);
            return true;
        }
        return childResult;
    }

    public double getScale()
    {
        return scale;
    }
}
