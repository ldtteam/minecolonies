package com.schematica.core.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GL11;

public final class GeometryTessellator extends Tessellator
{
    @Nullable
    private static GeometryTessellator instance = null;

    private static double deltaS = 0;
    private        double delta  = 0;

    private GeometryTessellator()
    {
        super(0x200000);
    }

    @Nullable
    public static synchronized GeometryTessellator getInstance()
    {
        if (instance == null)
        {
            instance = new GeometryTessellator();
        }

        return instance;
    }

    public static void setStaticDelta(final double delta)
    {
        deltaS = delta;
    }

    public static void drawCuboid(@Nonnull final VertexBuffer VertexBuffer, @Nonnull final BlockPos pos, final int sides, final int argb)
    {
        drawCuboid(VertexBuffer, pos, pos, sides, argb);
    }

    private static void drawCuboid(@Nonnull final VertexBuffer VertexBuffer, @Nonnull final BlockPos begin, @Nonnull final BlockPos end, final int sides, final int argb)
    {
        drawCuboid(VertexBuffer, begin, end, sides, argb, GeometryTessellator.deltaS);
    }

    public void setTranslation(final double x, final double y, final double z)
    {
        getBuffer().setTranslation(x, y, z);
    }

    public void beginQuads()
    {
        begin(GL11.GL_QUADS);
    }

    private void begin(final int mode)
    {
        getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public void beginLines()
    {
        begin(GL11.GL_LINES);
    }

    public void setDelta(final double delta)
    {
        this.delta = delta;
    }

    public void drawCuboid(@Nonnull final BlockPos pos, final int sides, final int argb)
    {
        drawCuboid(pos, pos, sides, argb);
    }

    public void drawCuboid(@Nonnull final BlockPos begin, @Nonnull final BlockPos end, final int sides, final int argb)
    {
        drawCuboid(getBuffer(), begin, end, sides, argb, this.delta);
    }

    private static void drawCuboid(@Nonnull final VertexBuffer VertexBuffer, @Nonnull final BlockPos begin, @Nonnull final BlockPos end, final int sides, final int argb, final double delta)
    {
        if (VertexBuffer.getDrawMode() == -1 || sides == 0)
        {
            return;
        }

        final double x0 = begin.getX() - delta;
        final double y0 = begin.getY() - delta;
        final double z0 = begin.getZ() - delta;
        final double x1 = end.getX() + 1 + delta;
        final double y1 = end.getY() + 1 + delta;
        final double z1 = end.getZ() + 1 + delta;

        switch (VertexBuffer.getDrawMode())
        {
            case GL11.GL_QUADS:
                drawQuads(VertexBuffer, x0, y0, z0, x1, y1, z1, sides, argb);
                break;

            case GL11.GL_LINES:
                drawLines(VertexBuffer, x0, y0, z0, x1, y1, z1, sides, argb);
                break;

            default:
                throw new IllegalStateException("Unsupported mode!");
        }
    }

    private static void drawQuads(
                                   @Nonnull final VertexBuffer VertexBuffer, final double x0, final double y0, final double z0,
                                   final double x1, final double y1, final double z1, final int sides, final int argb)
    {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;

        drawQuads(VertexBuffer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
    }

    private static void drawLines(
                                   @Nonnull final VertexBuffer VertexBuffer, final double x0, final double y0, final double z0,
                                   final double x1, final double y1, final double z1, final int sides, final int argb)
    {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;

        drawLines(VertexBuffer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
    }

    private static void drawQuads(
                                   @Nonnull final VertexBuffer VertexBuffer, final double x0, final double y0, final double z0,
                                   final double x1, final double y1, final double z1, final int sides, final int a, final int r, final int g, final int b)
    {
        if ((sides & GeometryMasks.Quad.DOWN) != 0)
        {
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.UP) != 0)
        {
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.NORTH) != 0)
        {
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.SOUTH) != 0)
        {
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0)
        {
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.EAST) != 0)
        {
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }
    }

    private static void drawLines(
                                   @Nonnull final VertexBuffer VertexBuffer, final double x0, final double y0, final double z0,
                                   final double x1, final double y1, final double z1, final int sides, final int a, final int r, final int g, final int b)
    {
        if ((sides & GeometryMasks.Line.DOWN_WEST) != 0)
        {
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_WEST) != 0)
        {
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_EAST) != 0)
        {
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_EAST) != 0)
        {
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0)
        {
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_NORTH) != 0)
        {
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0)
        {
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_SOUTH) != 0)
        {
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_WEST) != 0)
        {
            VertexBuffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_EAST) != 0)
        {
            VertexBuffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0)
        {
            VertexBuffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0)
        {
            VertexBuffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            VertexBuffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }
    }
}
