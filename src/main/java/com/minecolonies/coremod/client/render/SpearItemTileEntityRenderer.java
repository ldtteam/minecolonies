package com.minecolonies.coremod.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelSpear;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class SpearItemTileEntityRenderer extends ItemStackTileEntityRenderer
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/spear.png");
    private final        ModelSpear       model   = new ModelSpear();

    @Override
    public void renderByItem(
      ItemStack stack,
      @NotNull ItemCameraTransforms.TransformType transformType,
      MatrixStack matrixStack,
      @NotNull IRenderTypeBuffer buffer,
      int combinedLight,
      int combinedOverlay)
    {
        matrixStack.pushPose();
        model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(buffer, model.renderType(TEXTURE), false, stack.hasFoil()), combinedLight, combinedOverlay, 1, 1, 1, 1);
        matrixStack.popPose();
    }
}
