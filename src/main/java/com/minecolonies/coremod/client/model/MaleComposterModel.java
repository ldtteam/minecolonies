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
import org.jetbrains.annotations.NotNull;

public class MaleComposterModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleComposterModel(final ModelPart part)
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

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition apronBody = bipedBody.addOrReplaceChild("apronBody", CubeListBuilder.create().texOffs(104, 15).addBox(-4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(94, 15).addBox(4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(96, 21).addBox(-4.0F, -13.8F, 2.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.25F))
          .texOffs(96, 11).addBox(-4.0F, -20.0F, -2.8F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.249F))
          .texOffs(94, 15).addBox(2.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(112, 15).addBox(-3.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(90, 11).addBox(-3.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(108, 11).addBox(2.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(98, 22).addBox(-2.5F, -24.4F, 0.6F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 24.0F, 0.3F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition apronRightLeg = bipedRightLeg.addOrReplaceChild("apronRightLeg", CubeListBuilder.create().texOffs(104, 23).addBox(-4.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition apronLeftLeg = bipedLeftLeg.addOrReplaceChild("apronLeftLeg", CubeListBuilder.create().texOffs(96, 23).addBox(0.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

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
