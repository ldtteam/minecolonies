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

public class MaleChildModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleChildModel(final ModelPart part)
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

        PartDefinition littleSailorHat = bipedHead.addOrReplaceChild("littleSailorHat", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.5F, 0.1F, -0.0873F, 0.0F, 0.0F));

        PartDefinition center = littleSailorHat.addOrReplaceChild("center", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition tip = littleSailorHat.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(88, 0).addBox(-0.5F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.0F, -0.7418F, 0.0F));

        PartDefinition sideFront = littleSailorHat.addOrReplaceChild("sideFront", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -0.3454F, -4.3185F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, -0.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition sideLeft = littleSailorHat.addOrReplaceChild("sideLeft", CubeListBuilder.create().texOffs(110, 0).addBox(-3.5F, -0.3975F, -4.614F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, -0.5F, 0.1745F, -1.5708F, 0.0F));

        PartDefinition sideRight = littleSailorHat.addOrReplaceChild("sideRight", CubeListBuilder.create().texOffs(92, 2).addBox(-3.5F, -0.3801F, -4.5155F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, 0.5F, 0.1745F, 1.5708F, 0.0F));

        PartDefinition sideBack = littleSailorHat.addOrReplaceChild("sideBack", CubeListBuilder.create().texOffs(110, 2).addBox(-4.0F, -0.2933F, -4.0231F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.6F, 0.5F, 0.1745F, 3.1416F, 0.0F));

        PartDefinition feather = littleSailorHat.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(107, 4).addBox(-6.5F, -4.6F, -0.5F, 6.0F, 7.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(6.1F, 3.2F, -0.6F, 0.0873F, 0.0F, 0.0F));

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

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("littleSailorHat").visible = entity.getPose() != Pose.SLEEPING;
    }
}
