package com.minecolonies.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the Spear. The model is a long wooden rod with an iron head and leather handle.
 */
@OnlyIn(Dist.CLIENT)
public class SpearModel extends Model
{
    private final ModelPart handle;

    public SpearModel(ModelPart handle)
    {
        super(RenderType::entitySolid);
        this.handle = handle;
    }

    public static LayerDefinition createLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition currentHandle = partdefinition.addOrReplaceChild("handle",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-0.5F, -4.875F, -0.5F, 1.0F, 26.0F, 1.0F)
            .texOffs(4, 0).addBox(-1.0F, 5.125F, -1.0F, 2.0F, 5.0F, 2.0F),
          PartPose.offset(0.0F, 2.875F, 0.0F));

        currentHandle.addOrReplaceChild("arrow_flair",  CubeListBuilder.create()
          .texOffs(4, 7).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
          PartPose.offsetAndRotation(0.0F, -6.375F, 0.0F, 0.0F, 0.7854F, 0.0F));

        currentHandle.addOrReplaceChild("arrow_head",  CubeListBuilder.create()
            .texOffs(4, 7).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
          PartPose.offsetAndRotation(0.0F, -9.375F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(
      final @NotNull PoseStack matrixStack,
      final @NotNull VertexConsumer buffer,
      final int packedLight,
      final int packedOverlay,
      final int color)
    {
        handle.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}
