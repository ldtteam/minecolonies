package com.minecolonies.structures.lib;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class RenderUtil
{

    public static final float QUARTER = 90F;
    public static final float HALF = 180F;

    private RenderUtil()
    {
        throw new IllegalArgumentException("Utility Class");
    }

    public static void applyRotationToYAxis(@NotNull final Rotation rotation)
    {
        GlStateManager.translate(0.5F, 0F, 0.5F);

        float angle;
        switch (rotation)
        {
            case NONE:
                angle = 0F;
                break;
            case CLOCKWISE_90:
                angle = -QUARTER;
                break;
            case CLOCKWISE_180:
                angle = -HALF;
                break;
            case COUNTERCLOCKWISE_90:
                angle = QUARTER;
                break;
            default:
                angle = 0F;
                break;
        }

        GlStateManager.rotate(angle, 0, 1, 0);

        GlStateManager.translate(-0.5F, 0F, -0.5F);
    }

    public static void applyMirror(@NotNull final Mirror mirror, @NotNull final BlockPos appliedPrimaryBlockOff)
    {
        switch (mirror)
        {
            case NONE:
                GlStateManager.scale(1, 1, 1);
                break;
            case FRONT_BACK:
                GlStateManager.translate((2 * appliedPrimaryBlockOff.getX()) + 1, 0, 0);
                GlStateManager.scale(-1, 1, 1);
                break;
            case LEFT_RIGHT:
                GlStateManager.translate(0, 0, (2 * appliedPrimaryBlockOff.getZ()) + 1);
                GlStateManager.scale(1, 1, -1);
                break;
            default:
                //Should never occur.
                break;
        }
    }
}