// Made with Blockbench 4.0.0-beta.0
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class FemaleNobleModle extends CitizenModel<AbstractEntityCitizen>
{
    public FemaleNobleModle(final ModelPart part)
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

        PartDefinition hairback1_r1 = bipedHead.addOrReplaceChild("hairback1_r1", CubeListBuilder.create().texOffs(74, 0).addBox(-2.0F, -2.0F, -2.2F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(74, 7).addBox(-1.0F, -1.0F, 0.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -4.9F, 5.9F, 0.1309F, 0.0F, 0.0F));

        PartDefinition Hair = bipedHead.addOrReplaceChild("Hair", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition Hat = bipedHead.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(80, 8).addBox(-5.0F, -1.5F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.1F))
          .texOffs(88, 0).addBox(-3.0F, -2.8F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -7.1F, 0.0F, -0.0436F, 0.0F, 0.0F));

        PartDefinition feather2_r1 = Hat.addOrReplaceChild("feather2_r1", CubeListBuilder.create().texOffs(110, 43).addBox(0.0F, -5.3F, -1.6F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.1F, -0.5403F, 6.6193F, -2.891F, 0.51F, -3.0173F));

        PartDefinition SimpleHat = bipedHead.addOrReplaceChild("SimpleHat", CubeListBuilder.create().texOffs(82, 21).addBox(-3.5F, -1.5F, -6.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.11F))
          .texOffs(106, 21).addBox(-2.0F, -2.45F, -3.5F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.5F))
          .texOffs(103, 20).addBox(-4.0F, 8.1311F, -3.0F, 8.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(98, 32).addBox(-2.5F, -3.0115F, -3.0F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.9F, 0.5F, -0.0611F, 0.0F, 0.0F));

        PartDefinition ribbonC_r1 = SimpleHat.addOrReplaceChild("ribbonC_r1", CubeListBuilder.create().texOffs(86, 39).addBox(0.0F, 0.0F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -3.0F, -2.5F, 0.0F, 0.5236F, 0.0F));

        PartDefinition ribbonB_r1 = SimpleHat.addOrReplaceChild("ribbonB_r1", CubeListBuilder.create().texOffs(86, 36).addBox(-2.0F, 0.0F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -3.0F, -2.5F, 0.0F, -0.5236F, 0.0F));

        PartDefinition ribbonA_r1 = SimpleHat.addOrReplaceChild("ribbonA_r1", CubeListBuilder.create().texOffs(86, 33).addBox(-2.5F, -2.9F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.1F, -3.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition strapLeftTop_r1 = SimpleHat.addOrReplaceChild("strapLeftTop_r1", CubeListBuilder.create().texOffs(104, 36).addBox(0.0F, -4.9575F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0709F, 0.4F, -2.5F, 0.0F, 0.0F, -0.8116F));

        PartDefinition strapRightTop_r1 = SimpleHat.addOrReplaceChild("strapRightTop_r1", CubeListBuilder.create().texOffs(106, 36).addBox(0.0F, -4.9837F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0742F, 0.4F, -2.5F, 0.0F, 0.0F, 0.8116F));

        PartDefinition strapRight_r1 = SimpleHat.addOrReplaceChild("strapRight_r1", CubeListBuilder.create().texOffs(110, 33).mirror().addBox(-0.05F, -0.1F, -0.5F, 0.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 0.5F, -2.5F, 0.0F, 0.0F, -0.2618F));

        PartDefinition strapLeft_r1 = SimpleHat.addOrReplaceChild("strapLeft_r1", CubeListBuilder.create().texOffs(108, 33).addBox(0.15F, -0.1F, -0.5F, 0.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9F, 0.5F, -2.5F, 0.0F, 0.0F, 0.2635F));

        PartDefinition bottomRight_r1 = SimpleHat.addOrReplaceChild("bottomRight_r1", CubeListBuilder.create().texOffs(102, 32).addBox(-0.5F, -2.0F, -5.5F, 3.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-4.2419F, 1.6799F, -0.5F, 0.0F, 0.0F, -0.6981F));

        PartDefinition bottomLeft_r1 = SimpleHat.addOrReplaceChild("bottomLeft_r1", CubeListBuilder.create().texOffs(86, 33).addBox(-2.5F, -2.0F, -5.5F, 3.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.2419F, 1.6799F, -0.5F, 0.0F, 0.0F, 0.6981F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.249F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition NobleDress = bipedBody.addOrReplaceChild("NobleDress", CubeListBuilder.create().texOffs(0, 112).addBox(-8.0F, -4.0F, -6.0F, 16.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
          .texOffs(4, 98).addBox(-7.0F, -7.0F, -5.0F, 14.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
          .texOffs(8, 87).addBox(-6.0F, -9.0F, -4.0F, 12.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
          .texOffs(12, 78).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
          .texOffs(16, 71).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 24.0F, -0.5F));

        PartDefinition handbag = bipedBody.addOrReplaceChild("handbag", CubeListBuilder.create().texOffs(57, 9).addBox(4.0F, -18.4F, -3.4F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
          .texOffs(68, 15).addBox(4.0F, -24.3F, -2.3F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1F, 24.0F, -0.2F));

        PartDefinition handback_r1 = handbag.addOrReplaceChild("handback_r1", CubeListBuilder.create().texOffs(68, 10).addBox(-0.5F, -5.97F, -0.02F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -18.4F, 3.6F, 0.1484F, 0.0F, 0.0F));

        PartDefinition handfront_r1 = handbag.addOrReplaceChild("handfront_r1", CubeListBuilder.create().texOffs(66, 10).addBox(-0.5F, -6.4F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -18.0F, -3.4F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.1F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.1F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(5.1F, 2.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("SimpleHat").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("Hat").visible = displayHat(entity);
        body.getChild("NobleDress").visible = entity.getPose() != Pose.SLEEPING;
        body.getChild("handbag").visible = entity.getPose() != Pose.SLEEPING;
    }
}
