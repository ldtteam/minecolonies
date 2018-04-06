package com.minecolonies.structures.lib;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class RenderUtil
{

    private RenderUtil()
    {
        throw new IllegalArgumentException("Utility Class");
    }

    public static void applyRotationToYAxis(@NotNull final Rotation rotation)
    {
        float angle = 0f;
        switch (rotation)
        {
            case NONE:
                angle = -0f;
                break;
            case CLOCKWISE_90:
                angle = -90f;
                break;
            case CLOCKWISE_180:
                angle = -180f;
                break;
            case COUNTERCLOCKWISE_90:
                angle = 90f;
                break;
        }

        GlStateManager.rotate(angle, 0, 1, 0f);
    }

    public static void applyMirror(@NotNull final Mirror mirror, @NotNull final BlockPos appliedPrimaryBlockOff)
    {
        switch (mirror)
        {
            case NONE:
                GlStateManager.scale(1f, 1f, 1f);
                break;
            case LEFT_RIGHT:
                GlStateManager.scale(-1f, 1f,1f);
                GlStateManager.translate(0f,0f,0f);
                break;
            case FRONT_BACK:
                GlStateManager.translate(0,0,2 * appliedPrimaryBlockOff.getZ());
                GlStateManager.scale(1f, 1f, -1f);
                break;
        }
    }
}