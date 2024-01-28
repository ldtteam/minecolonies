// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.workers.production.herders.EntityAIWorkCowboy.RENDER_META_BUCKET;

public class MaleCowHerderModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleCowHerderModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition straw = bipedHead.addOrReplaceChild("straw", CubeListBuilder.create(), PartPose.offset(0.0F, -6.4375F, -0.1F));

        PartDefinition straw_r1 = straw.addOrReplaceChild("straw_r1", CubeListBuilder.create().texOffs(92, 30).addBox(-2.4F, -1.9375F, -6.1F, 4.0F, 7.0F, 8.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(1.0F, 4.4375F, -3.4F, -0.3927F, -0.4363F, 0.0F));

        PartDefinition RoundHat = bipedHead.addOrReplaceChild("RoundHat", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0F, -2.9525F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.6F))
          .texOffs(76, 11).addBox(-8.0F, -0.3625F, -5.0F, 16.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(98, 22).addBox(-5.0F, -0.3625F, -8.0F, 10.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(93, 22).addBox(-7.0F, -0.3625F, -7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(85, 22).addBox(5.0F, -0.3625F, -7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(93, 26).addBox(-7.0F, -0.3625F, 5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(85, 26).addBox(5.0F, -0.3625F, 5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(98, 26).addBox(-5.0F, -0.3625F, 5.0F, 10.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0375F, -0.1F, -0.0873F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bucketwithHandle = bipedRightLeg.addOrReplaceChild("bucketwithHandle", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.1309F));

        PartDefinition handle_r1 = bucketwithHandle.addOrReplaceChild("handle_r1", CubeListBuilder.create().texOffs(118, 59).addBox(-2.0F, -4.0F, -0.9F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 3.5F, -3.2F, -0.4363F, 0.0F, 0.0F));

        PartDefinition bucket = bucketwithHandle.addOrReplaceChild("bucket", CubeListBuilder.create().texOffs(112, 42).addBox(-2.0F, -0.5248F, -1.7164F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(112, 47).addBox(-2.0F, -0.5248F, -1.7164F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(116, 55).addBox(-1.5F, 2.8752F, -1.2164F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 3.3F, -3.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        rightLeg.getChild("bucketwithHandle").visible = entity.getRenderMetadata().contains(RENDER_META_BUCKET);
        head.getChild("RoundHat").visible = displayHat(entity);
        head.getChild("straw").visible = entity.getPose() != Pose.SLEEPING;
    }
}
