package com.minecolonies.structures.client;

import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StructureClientHandler
{

    private StructureClientHandler()
    {
        throw new IllegalArgumentException("Utility class");
    }

    public static void renderStructure(@NotNull final Structure structure, final float partialTicks, final BlockPos pos)
    {
        renderStructure(structure, Minecraft.getMinecraft().player, partialTicks, pos);
    }

    public static void renderStructure(@NotNull final Structure structure, @Nullable final Entity perspectiveEntity, final float partialTicks, final BlockPos pos)
    {
        if (perspectiveEntity != null)
        {
            final double interpolatedEntityPosX = perspectiveEntity.lastTickPosX + (perspectiveEntity.posX - perspectiveEntity.lastTickPosX) * partialTicks;
            final double interpolatedEntityPosY = perspectiveEntity.lastTickPosY + (perspectiveEntity.posY - perspectiveEntity.lastTickPosY) * partialTicks;
            final double interpolatedEntityPosZ = perspectiveEntity.lastTickPosZ + (perspectiveEntity.posZ - perspectiveEntity.lastTickPosZ) * partialTicks;

            final double renderOffsetX = pos.getX() - interpolatedEntityPosX;
            final double renderOffsetY = pos.getY() - interpolatedEntityPosY;
            final double renderOffsetZ = pos.getZ() - interpolatedEntityPosZ;

            final Vector3d renderOffset = new Vector3d();
            renderOffset.x = renderOffsetX;
            renderOffset.y = renderOffsetY;
            renderOffset.z = renderOffsetZ;

            TemplateRenderHandler.getInstance().draw(structure.getTemplate(), structure.getSettings().getRotation(), structure.getSettings().getMirror(), renderOffset, partialTicks, pos);
        }
    }
}
