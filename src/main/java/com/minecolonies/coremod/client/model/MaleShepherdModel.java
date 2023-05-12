// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class MaleShepherdModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleShepherdModel(final ModelPart part)
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

        PartDefinition straw_r1 = straw.addOrReplaceChild("straw_r1", CubeListBuilder.create().texOffs(102, 30).addBox(-2.4F, -1.9375F, -6.1F, 4.0F, 7.0F, 8.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(1.0F, 4.4375F, -3.4F, -0.3927F, -0.4363F, 0.0F));

        PartDefinition Cap = bipedHead.addOrReplaceChild("Cap", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.6F, 0.1F, -0.0873F, 0.0F, 0.0F));

        PartDefinition center = Cap.addOrReplaceChild("center", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition tip = Cap.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(88, 0).addBox(-0.5F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.0F, -0.7418F, 0.0F));

        PartDefinition sideFront = Cap.addOrReplaceChild("sideFront", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -0.3454F, -4.0685F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, -0.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition sideLeft = Cap.addOrReplaceChild("sideLeft", CubeListBuilder.create().texOffs(110, 0).addBox(-3.5F, -0.3975F, -4.364F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, -0.5F, 0.1745F, -1.5708F, 0.0F));

        PartDefinition sideRight = Cap.addOrReplaceChild("sideRight", CubeListBuilder.create().texOffs(92, 2).addBox(-3.5F, -0.3801F, -4.2655F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, 0.5F, 0.1745F, 1.5708F, 0.0F));

        PartDefinition sideBack = Cap.addOrReplaceChild("sideBack", CubeListBuilder.create().texOffs(110, 2).addBox(-4.0F, -0.2933F, -3.7731F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, 0.5F, 0.1745F, 3.1416F, 0.0F));

        PartDefinition visor = Cap.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(88, 4).addBox(-4.0F, -0.5F, -1.5F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.9F, -5.5F, 0.0873F, 0.0F, 0.0F));

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

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition FoldedRightArm = bipedBody.addOrReplaceChild("FoldedRightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition staff_r1 = FoldedRightArm.addOrReplaceChild("staff_r1", CubeListBuilder.create().texOffs(88, 19).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.0F, -3.5F, 0.0436F, 0.0F, 0.0F));

        PartDefinition shoulderArm = FoldedRightArm.addOrReplaceChild("shoulderArm", CubeListBuilder.create().texOffs(56, 16).addBox(-3.01F, -2.01F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(56, 26).addBox(-3.011F, -2.0F, -2.01F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition foreArm = FoldedRightArm.addOrReplaceChild("foreArm", CubeListBuilder.create().texOffs(72, 16).addBox(-2.01F, -1.5F, -2.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.01F))
          .texOffs(72, 27).addBox(-2.01F, -1.45F, -2.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.251F)), PartPose.offsetAndRotation(-1.0F, 4.5F, 0.5F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        final boolean showPole = entity.getPose() != Pose.SLEEPING && isWorking(entity) && entity.getMainHandItem().isEmpty();
        body.getChild("FoldedRightArm").visible = showPole;
        rightArm.visible = !showPole;
        head.getChild("straw").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("Cap").visible = displayHat(entity);
    }
}
