// Made with Blockbench 3.6.5
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

public class FemaleCookModel extends CitizenModel<AbstractEntityCitizen>
{

    public FemaleCookModel(final ModelPart part)
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

        PartDefinition hairCircle = bipedHead.addOrReplaceChild("hairCircle", CubeListBuilder.create().texOffs(94, 54).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -9.2F, 0.0F, -0.8551F, 0.0F, 0.0F));

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition BakerHat = bipedHead.addOrReplaceChild("BakerHat", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.7F, 0.9F, -0.7854F, 0.0F, 0.0F));

        PartDefinition top_r1 = BakerHat.addOrReplaceChild("top_r1", CubeListBuilder.create().texOffs(86, 12).addBox(-6.4F, -1.1F, -6.2F, 11.0F, 2.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -3.481F, -1.2339F, 0.6076F, 0.0469F, -0.1477F));

        PartDefinition middle_r1 = BakerHat.addOrReplaceChild("middle_r1", CubeListBuilder.create().texOffs(94, 24).addBox(-4.48F, -1.8F, -4.7F, 9.0F, 4.0F, 8.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -2.3267F, -1.1035F, 0.5659F, 0.0334F, -0.0807F));

        PartDefinition base_r1 = BakerHat.addOrReplaceChild("base_r1", CubeListBuilder.create().texOffs(88, 0).addBox(-5.0F, -1.1F, -5.4F, 10.0F, 2.0F, 10.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -0.0246F, -0.4004F, 0.48F, 0.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition skirt = bipedBody.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(58, 15).addBox(0.0F, -0.3F, 1.0F, 8.0F, 1.0F, 6.0F, new CubeDeformation(0.15F))
          .texOffs(54, 22).addBox(-1.0F, 0.7F, 0.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(54, 34).addBox(-2.0F, 4.7F, -1.0F, 12.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 11.0F, -4.0F));

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
        body.getChild("skirt").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("BakerHat").visible = isWorking(entity) && displayHat(entity);
    }
}
