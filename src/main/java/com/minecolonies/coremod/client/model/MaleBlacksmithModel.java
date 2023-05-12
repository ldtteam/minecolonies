// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;

public class MaleBlacksmithModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleBlacksmithModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Beard = Head.addOrReplaceChild("Beard", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, -1.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(74, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -7.0F));

        PartDefinition shortBeard = Head.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition apronBody = Body.addOrReplaceChild("apronBody", CubeListBuilder.create().texOffs(114, 43).addBox(-4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(104, 43).addBox(4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(106, 49).addBox(-4.0F, -13.8F, 2.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.25F))
          .texOffs(106, 39).addBox(-4.0F, -20.0F, -2.8F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.249F))
          .texOffs(104, 43).addBox(2.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(122, 43).addBox(-3.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(100, 39).addBox(-3.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(118, 39).addBox(2.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(108, 50).addBox(-2.5F, -24.4F, 0.6F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.15F))
          .texOffs(108, 59).addBox(-3.0F, -14.6F, -3.3F, 6.0F, 2.0F, 0.0F, new CubeDeformation(0.25F))
          .texOffs(108, 61).addBox(1.0F, -16.4F, -3.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(112, 61).addBox(-0.4F, -16.4F, -3.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.3F));

        PartDefinition Right_Arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Left_Arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition Right_Leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition apronRightLeg = Right_Leg.addOrReplaceChild("apronRightLeg", CubeListBuilder.create().texOffs(114, 51).addBox(-4.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition Left_Leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition apronLeftLeg = Left_Leg.addOrReplaceChild("apronLeftLeg", CubeListBuilder.create().texOffs(106, 51).addBox(0.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        final boolean working = isWorking(entity);
        body.getChild("apronBody").visible = working;
        leftLeg.getChild("apronLeftLeg").visible = working;
        rightLeg.getChild("apronRightLeg").visible = working;
    }
}
