// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

public class MaleUndertakerModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleUndertakerModel(final ModelPart part)
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

        PartDefinition Hat = bipedHead.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(64, 0).addBox(-3.9664F, -3.1854F, -5.0F, 8.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(90, 0).addBox(-2.9664F, -10.6F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
          .texOffs(64, 12).addBox(-2.9664F, -11.3F, -4.0F, 6.0F, 10.0F, 1.0F, new CubeDeformation(-0.101F))
          .texOffs(64, 22).addBox(2.5336F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, new CubeDeformation(-0.102F))
          .texOffs(80, 22).addBox(-3.4664F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, new CubeDeformation(-0.103F))
          .texOffs(78, 12).addBox(-2.9664F, -11.3F, 3.0F, 6.0F, 10.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, -0.0611F, 0.0F, 0.0F));

        PartDefinition hatpartlowleft = Hat.addOrReplaceChild("hatpartlowleft", CubeListBuilder.create().texOffs(92, 7).addBox(-2.3F, -0.7F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-3.1F, -2.2F, 0.0F, 0.0F, 0.0F, 0.48F));

        PartDefinition hatpartlowright = Hat.addOrReplaceChild("hatpartlowright", CubeListBuilder.create().texOffs(96, 19).addBox(0.4466F, 1.4993F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(6.0079F, -4.9714F, 0.0F, 0.0F, 0.0F, 1.0908F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("Hat").visible = displayHat(entity);
    }
}
