// Made with Blockbench 4.1.5
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.workers.guard.EntityAIDruid.RENDER_META_POTION;

public class MaleDruidModel extends CitizenModel<AbstractEntityCitizen>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modelentitydruidmale"), "main");

    public MaleDruidModel(ModelPart root)
    {
        super(root);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Horns = bipedHead.addOrReplaceChild("Horns", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition righthorn_r1 = Horns.addOrReplaceChild("righthorn_r1", CubeListBuilder.create().texOffs(82, 41).addBox(-0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -6.0F, -3.0F, 0.0F, 0.48F, 0.0F));

        PartDefinition lefthorn_r1 = Horns.addOrReplaceChild("lefthorn_r1", CubeListBuilder.create().texOffs(82, 27).addBox(0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -6.0F, -3.0F, 0.0F, -0.48F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition potionBag = bipedBody.addOrReplaceChild("potionBag", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Potion1 = potionBag.addOrReplaceChild("Potion1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.3152F, -2.1035F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.1F))
          .texOffs(112, 14).addBox(-1.8152F, -1.3035F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.0F))
          .texOffs(116, 10).addBox(-0.8152F, -2.4535F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
          .texOffs(97, 18).addBox(-2.3152F, -0.4535F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(2.1F, -21.42F, -4.2F, 0.0F, 0.0F, -0.6196F));

        PartDefinition Potion2 = potionBag.addOrReplaceChild("Potion2", CubeListBuilder.create().texOffs(96, 26).addBox(-2.5F, -2.18F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.1F))
          .texOffs(112, 33).addBox(-2.0F, -1.38F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.0F))
          .texOffs(116, 29).addBox(-1.0F, -2.53F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
          .texOffs(97, 37).addBox(-2.5F, -0.53F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-0.667F, -18.8666F, -4.2F, 0.0F, 0.0F, -0.6196F));

        PartDefinition Potion3 = potionBag.addOrReplaceChild("Potion3", CubeListBuilder.create().texOffs(96, 45).addBox(-2.4653F, -2.4009F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.1F))
          .texOffs(112, 52).addBox(-1.9653F, -1.6009F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.0F))
          .texOffs(116, 48).addBox(-0.9653F, -2.7509F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
          .texOffs(97, 56).addBox(-2.4653F, -0.7509F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-3.3233F, -15.9508F, -4.2F, 0.0F, 0.0F, -0.6196F));

        PartDefinition mainStrap = potionBag.addOrReplaceChild("mainStrap", CubeListBuilder.create(), PartPose.offset(0.0F, -18.3F, -2.0F));

        PartDefinition mainStrap_r1 = mainStrap.addOrReplaceChild("mainStrap_r1", CubeListBuilder.create().texOffs(63, 13).addBox(-7.0F, -1.0F, -0.5F, 14.0F, 2.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.3F, 0.0F, 0.0F, -0.829F));

        PartDefinition symbol = potionBag.addOrReplaceChild("symbol", CubeListBuilder.create(), PartPose.offset(4.5F, -23.2F, -3.1F));

        PartDefinition symbol_r1 = symbol.addOrReplaceChild("symbol_r1", CubeListBuilder.create().texOffs(83, 20).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-1.4F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.2F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        body.getChild("potionBag").visible = entity.getRenderMetadata().contains(RENDER_META_POTION) && isWorking(entity);
    }
}
