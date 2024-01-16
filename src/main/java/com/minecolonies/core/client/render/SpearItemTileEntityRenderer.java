package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpearItemTileEntityRenderer extends BlockEntityWithoutLevelRenderer
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/spear.png");
    private              SpearModel       model;
    private final        EntityModelSet   set;

    public SpearItemTileEntityRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.set = Minecraft.getInstance().getEntityModels();
        this.model = new SpearModel(this.set.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager)
    {
        this.model = new SpearModel(this.set.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void renderByItem(
      final ItemStack stack,
      final ItemTransforms.TransformType transformType,
      final PoseStack matrixStack,
      final MultiBufferSource buffer,
      final int combinedLight,
      final int combinedOverlay)
    {
        matrixStack.pushPose();
        model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(buffer, model.renderType(TEXTURE), false, stack.hasFoil()), combinedLight, combinedOverlay, 1, 1, 1, 1);
        matrixStack.popPose();
    }
}
