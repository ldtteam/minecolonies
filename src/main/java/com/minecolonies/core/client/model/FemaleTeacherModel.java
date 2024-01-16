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

public class FemaleTeacherModel extends CitizenModel<AbstractEntityCitizen>
{

    public FemaleTeacherModel(final ModelPart part)
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

        PartDefinition feather_r1 = bipedHead.addOrReplaceChild("feather_r1", CubeListBuilder.create().texOffs(56, 16).addBox(-2.3F, -1.9F, -8.6F, 0.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -10.5F, 4.5F, 0.0F, -0.2356F, 0.0F));

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition glasses = bipedHead.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(74, 0).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(74, 8).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(90, 0).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F))
          .texOffs(85, 16).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F))
          .texOffs(71, 16).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, -3.7F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bag = bipedBody.addOrReplaceChild("bag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition map_r1 = bag.addOrReplaceChild("map_r1", CubeListBuilder.create().texOffs(65, 27).addBox(-2.8F, -3.2F, -6.5F, 6.0F, 6.0F, 13.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(-5.5F, 8.0F, 0.0F, 0.0F, 0.0F, 1.0472F));

        PartDefinition strapback_r1 = bag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(122, 46).addBox(-0.5F, -14.3F, 0.0F, 1.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));

        PartDefinition strapfrontb_r1 = bag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(123, 46).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4558F, -0.5558F, 1.1519F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition strapfrontb_r2 = bag.addOrReplaceChild("strapfrontb_r2", CubeListBuilder.create().texOffs(123, 51).addBox(-0.5F, -2.5F, 0.5F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6008F, 1.2992F, -3.0977F, -0.2269F, 0.0F, 0.7854F));

        PartDefinition strapfronta_r1 = bag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(126, 46).addBox(-0.5F, -8.0F, 0.0F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 9.4F, -4.1F, -0.0873F, 0.0F, 0.7854F));

        PartDefinition bag_r1 = bag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(106, 32).addBox(-1.9F, -3.0F, -4.0F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition ruler = bag.addOrReplaceChild("ruler", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5511F, 8.4214F, 3.0794F, 0.0F, 0.0F, -0.0873F));

        PartDefinition ruler_r1 = ruler.addOrReplaceChild("ruler_r1", CubeListBuilder.create().texOffs(116, 54).addBox(-0.5F, -4.0F, -1.3F, 1.0F, 8.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1732F, -0.0806F, -0.0335F));

        PartDefinition dress = bipedBody.addOrReplaceChild("dress", CubeListBuilder.create().texOffs(90, 14).addBox(-5.0F, -11.0F, -4.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
          .texOffs(92, 6).addBox(-5.0F, -12.0F, -3.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
          .texOffs(96, 0).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 1.0F, 5.0F, new CubeDeformation(0.251F)), PartPose.offset(0.0F, 24.0F, -0.5F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.4F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-1.4F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.8F, 2.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
          .texOffs(62, 30).addBox(-0.7F, 7.5F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offset(5.0F, 2.0F, 0.0F));

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
        head.getChild("feather_r1").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("glasses").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("HairExtension").visible = entity.getPose() != Pose.SLEEPING;
        body.getChild("dress").visible = entity.getPose() != Pose.SLEEPING;
        body.getChild("bag").visible = entity.getPose() != Pose.SLEEPING && isWorking(entity);
    }
}
