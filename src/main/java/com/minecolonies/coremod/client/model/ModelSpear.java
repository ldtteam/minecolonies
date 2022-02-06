package com.minecolonies.coremod.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the Spear. The model is a long wooden rod with an iron head and leather handle.
 */
@OnlyIn(Dist.CLIENT)
public class ModelSpear extends Model
{
    private final ModelRenderer handle;

    public ModelSpear()
    {
        super(RenderType::entitySolid);
        texWidth = 32;
        texHeight = 32;

        handle = new ModelRenderer(this);
        handle.setPos(0.0F, 2.875F, 0.0F);
        handle.texOffs(0, 0).addBox(-0.5F, -4.875F, -0.5F, 1.0F, 26.0F, 1.0F, 0.0F, false);
        handle.texOffs(4, 0).addBox(-1.0F, 5.125F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);

        ModelRenderer arrow_flair = new ModelRenderer(this);
        arrow_flair.setPos(0.0F, -6.375F, 0.0F);
        handle.addChild(arrow_flair);
        setRotationAngle(arrow_flair, 0.0F, 0.7854F, 0.0F);
        arrow_flair.texOffs(4, 7).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);

        ModelRenderer arrow_head = new ModelRenderer(this);
        arrow_head.setPos(0.0F, -9.375F, 0.0F);
        handle.addChild(arrow_head);
        setRotationAngle(arrow_head, 0.0F, -0.7854F, 0.0F);
        arrow_head.texOffs(4, 12).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
    }

    @Override
    public void renderToBuffer(
      @NotNull MatrixStack matrixStack,
      @NotNull IVertexBuilder buffer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha)
    {
        handle.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
