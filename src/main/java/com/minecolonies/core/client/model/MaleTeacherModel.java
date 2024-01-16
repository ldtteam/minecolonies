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

public class MaleTeacherModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleTeacherModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition glasses = bipedHead.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(64, 0).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(64, 8).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(80, 0).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F))
          .texOffs(75, 16).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F))
          .texOffs(61, 16).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, -3.7F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition SailorHat1 = bipedHead.addOrReplaceChild("SailorHat1", CubeListBuilder.create().texOffs(102, 0).addBox(-4.0F, 1.33F, -5.318F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(112, 32).addBox(-4.0F, -2.65F, -5.318F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(80, 0).addBox(-4.0F, -0.85F, -4.218F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.2F))
          .texOffs(88, 10).addBox(-3.0F, -2.05F, -3.218F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
          .texOffs(106, 44).addBox(-5.0F, -2.9F, 0.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.55F, 0.318F, -0.1047F, 0.0F, 0.0F));

        PartDefinition feather2_r1 = SailorHat1.addOrReplaceChild("feather2_r1", CubeListBuilder.create().texOffs(110, 48).addBox(-1.5F, -6.3F, -2.3F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, -1.7F, 1.0F, -0.2182F, -0.7418F, 0.0F));

        PartDefinition flap_r1 = SailorHat1.addOrReplaceChild("flap_r1", CubeListBuilder.create().texOffs(112, 40).addBox(0.0F, -4.0F, -5.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(101, 21).addBox(0.0F, 0.0F, -5.0F, 8.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0942F, 1.35F, -2.2385F, 0.0F, -0.9163F, 0.0F));

        PartDefinition flap_r2 = SailorHat1.addOrReplaceChild("flap_r2", CubeListBuilder.create().texOffs(90, 32).addBox(7.9668F, -4.0F, -5.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0739F, 1.35F, -2.2121F, 0.0F, -0.9163F, 0.0F));

        PartDefinition flap_r3 = SailorHat1.addOrReplaceChild("flap_r3", CubeListBuilder.create().texOffs(112, 36).addBox(-7.0F, -4.0F, -5.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(101, 10).addBox(-7.0F, -0.001F, -5.0F, 8.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.483F, 1.35F, -1.4451F, 0.0F, 0.9163F, 0.0F));

        PartDefinition flap_r4 = SailorHat1.addOrReplaceChild("flap_r4", CubeListBuilder.create().texOffs(90, 17).addBox(-7.045F, -4.0F, -5.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.455F, 1.35F, -1.4816F, 0.0F, 0.9163F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bag = bipedBody.addOrReplaceChild("bag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -0.2F));

        PartDefinition map_r1 = bag.addOrReplaceChild("map_r1", CubeListBuilder.create().texOffs(59, 23).addBox(-2.8F, -3.2F, -6.5F, 6.0F, 6.0F, 13.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(-5.5F, 8.0F, 0.0F, 0.0F, 0.0F, 1.0472F));

        PartDefinition strapback_r1 = bag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(92, 49).addBox(-0.5F, -14.3F, 0.0F, 1.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));

        PartDefinition strapfrontb_r1 = bag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(93, 49).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4558F, -0.5558F, 1.1519F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition strapfrontb_r2 = bag.addOrReplaceChild("strapfrontb_r2", CubeListBuilder.create().texOffs(93, 54).addBox(-0.5F, -2.5F, 0.5F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6008F, 1.2992F, -3.0977F, -0.2269F, 0.0F, 0.7854F));

        PartDefinition strapfronta_r1 = bag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(96, 49).addBox(-0.5F, -8.0F, 0.0F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 9.4F, -4.1F, -0.0873F, 0.0F, 0.7854F));

        PartDefinition bag_r1 = bag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(64, 50).addBox(-1.9F, -3.0F, -4.0F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition ruler = bag.addOrReplaceChild("ruler", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5511F, 8.4214F, 3.0794F, 0.0F, 0.0F, -0.0873F));

        PartDefinition ruler_r1 = ruler.addOrReplaceChild("ruler_r1", CubeListBuilder.create().texOffs(86, 54).addBox(-0.5F, -4.0F, -1.3F, 1.0F, 8.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1732F, -0.0806F, -0.0335F));

        PartDefinition bipedRightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.3F, 2.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition bipedLeftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
          .texOffs(56, 26).addBox(-0.4F, 8.3F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("SailorHat1").visible = displayHat(entity);
        head.getChild("glasses").visible = entity.getPose() != Pose.SLEEPING;
        body.getChild("bag").visible = entity.getPose() != Pose.SLEEPING && isWorking(entity);
    }
}
