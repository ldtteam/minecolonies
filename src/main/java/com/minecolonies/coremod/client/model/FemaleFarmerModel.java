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

public class FemaleFarmerModel extends CitizenModel<AbstractEntityCitizen>
{

    public FemaleFarmerModel(final ModelPart part)
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

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition LassCap = bipedHead.addOrReplaceChild("LassCap", CubeListBuilder.create().texOffs(108, 0).addBox(-5.0F, -4.6026F, -4.9584F, 1.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
          .texOffs(92, 32).addBox(-4.5F, -4.6026F, -4.9484F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.01F))
          .texOffs(97, 1).addBox(-4.5F, -3.6026F, 3.0716F, 9.0F, 7.0F, 1.0F, new CubeDeformation(-0.01F))
          .texOffs(68, 0).addBox(4.0F, -4.6026F, -4.9584F, 1.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
          .texOffs(119, 0).addBox(-5.0F, 3.3974F, -3.9484F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(119, 3).mirror().addBox(5.0F, 3.3974F, -3.9484F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.9974F, 0.4484F));

        PartDefinition detailBottom_r1 = LassCap.addOrReplaceChild("detailBottom_r1", CubeListBuilder.create().texOffs(88, 21).addBox(-4.8F, 3.65F, -2.6F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.2F, -5.3651F, 3.3152F, -0.9163F, 0.0F, 0.0F));

        PartDefinition detail_r1 = LassCap.addOrReplaceChild("detail_r1", CubeListBuilder.create().texOffs(80, 9).addBox(-5.0F, -0.6F, -1.1F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, -5.6026F, 2.5516F, -0.9163F, 0.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition seedBox = bipedBody.addOrReplaceChild("seedBox", CubeListBuilder.create(), PartPose.offset(4.0F, 3.7F, -3.9F));

        PartDefinition strapLeft_r1 = seedBox.addOrReplaceChild("strapLeft_r1", CubeListBuilder.create().texOffs(54, 37).mirror().addBox(0.1F, -0.5F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.11F)).mirror(false)
          .texOffs(70, 37).mirror().addBox(-8.1F, -0.5F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.11F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.9599F, 0.0F, 0.0F));

        PartDefinition seeds_r1 = seedBox.addOrReplaceChild("seeds_r1", CubeListBuilder.create().texOffs(62, 33).mirror().addBox(-3.0F, -1.25F, -1.6667F, 6.0F, 1.0F, 3.0F, new CubeDeformation(-0.1F)).mirror(false)
          .texOffs(71, 24).mirror().addBox(-4.0F, -1.55F, -2.6667F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(55, 24).mirror().addBox(3.0F, -1.55F, -2.6667F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(63, 26).mirror().addBox(-3.0F, -1.55F, 1.3333F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(63, 22).mirror().addBox(-3.0F, -1.55F, -2.6667F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(61, 18).mirror().addBox(-3.0F, 0.45F, -1.6667F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, 4.05F, -0.3333F, -0.1309F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

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
        body.getChild("seedBox").visible = entity.getPose() != Pose.SLEEPING && isWorking(entity);
        head.getChild("LassCap").visible = displayHat(entity);
    }
}
