package com.minecolonies.core.client.render;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for a normal tile entity (Nothing special with rendering).
 */
@OnlyIn(Dist.CLIENT)
public class EmptyTileEntitySpecialRenderer implements BlockEntityRenderer<AbstractTileEntityColonyBuilding>
{

    public EmptyTileEntitySpecialRenderer(BlockEntityRendererProvider.Context context)
    {
        super();
    }

    @Override
    public void render(
      @NotNull final AbstractTileEntityColonyBuilding tileEntity,
      final float v,
      @NotNull final PoseStack matrixStack,
      @NotNull final MultiBufferSource iRenderTypeBuffer,
      final int i,
      final int i1)
    {

    }
}
