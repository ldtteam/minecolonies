package com.schematica.core.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

public class GeometryTessellator extends Tessellator {
    private static GeometryTessellator instance = null;

    private static double deltaS = 0;
    private double delta = 0;

    private GeometryTessellator() {
        super(0x200000);
    }

    public static GeometryTessellator getInstance() {
        if (instance == null) {
            instance = new GeometryTessellator();
        }

        return instance;
    }

    public void setTranslation(final double x, final double y, final double z) {
        getWorldRenderer().setTranslation(x, y, z);
    }

    public void beginQuads() {
        begin(GL11.GL_QUADS);
    }

    public void beginLines() {
        begin(GL11.GL_LINES);
    }

    private void begin(final int mode) {
        getWorldRenderer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public void setDelta(final double delta) {
        this.delta = delta;
    }

    public static void setStaticDelta(final double delta) {
        deltaS = delta;
    }

    public void drawCuboid(final BlockPos pos, final int sides, final int argb) {
        drawCuboid(pos, pos, sides, argb);
    }

    public void drawCuboid(final BlockPos begin, final BlockPos end, final int sides, final int argb) {
        drawCuboid(getWorldRenderer(), begin, end, sides, argb, this.delta);
    }

    public static void drawCuboid(final WorldRenderer worldRenderer, final BlockPos pos, final int sides, final int argb) {
        drawCuboid(worldRenderer, pos, pos, sides, argb);
    }

    private static void drawCuboid(final WorldRenderer worldRenderer, final BlockPos begin, final BlockPos end, final int sides, final int argb) {
        drawCuboid(worldRenderer, begin, end, sides, argb, GeometryTessellator.deltaS);
    }

    private static void drawCuboid(final WorldRenderer worldRenderer, final BlockPos begin, final BlockPos end, final int sides, final int argb, final double delta) {
        if (worldRenderer.getDrawMode() == -1 || sides == 0) {
            return;
        }

        final double x0 = begin.getX() - delta;
        final double y0 = begin.getY() - delta;
        final double z0 = begin.getZ() - delta;
        final double x1 = end.getX() + 1 + delta;
        final double y1 = end.getY() + 1 + delta;
        final double z1 = end.getZ() + 1 + delta;

        switch (worldRenderer.getDrawMode()) {
        case GL11.GL_QUADS:
            drawQuads(worldRenderer, x0, y0, z0, x1, y1, z1, sides, argb);
            break;

        case GL11.GL_LINES:
            drawLines(worldRenderer, x0, y0, z0, x1, y1, z1, sides, argb);
            break;

        default:
            throw new IllegalStateException("Unsupported mode!");
        }
    }

    private static void drawQuads(final WorldRenderer worldRenderer, final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final int argb) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;

        drawQuads(worldRenderer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
    }

    private static void drawQuads(final WorldRenderer worldRenderer, final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final int a, final int r, final int g, final int b) {
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.UP) != 0) {
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }
    }

    private static void drawLines(final WorldRenderer worldRenderer, final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final int argb) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;

        drawLines(worldRenderer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
    }

    private static void drawLines(final WorldRenderer worldRenderer, final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int sides, final int a, final int r, final int g, final int b) {
        if ((sides & GeometryMasks.Line.DOWN_WEST) != 0) {
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_WEST) != 0) {
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_EAST) != 0) {
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_EAST) != 0) {
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0) {
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_NORTH) != 0) {
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0) {
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.UP_SOUTH) != 0) {
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_WEST) != 0) {
            worldRenderer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.NORTH_EAST) != 0) {
            worldRenderer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0) {
            worldRenderer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0) {
            worldRenderer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        }
    }
}
