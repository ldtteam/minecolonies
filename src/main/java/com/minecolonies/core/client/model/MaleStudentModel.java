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
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.workers.education.EntityAIStudy.RENDER_META_BOOK;
import static com.minecolonies.core.entity.ai.workers.education.EntityAIStudy.RENDER_META_STUDYING;

public class MaleStudentModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleStudentModel(final ModelPart part)
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

        PartDefinition glasses = Head.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(103, 0).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(103, 8).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(119, 0).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F))
          .texOffs(114, 11).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F))
          .texOffs(114, 18).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, -3.7F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition shortBeard = Head.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Belly = Body.addOrReplaceChild("Belly", CubeListBuilder.create().texOffs(56, 38).mirror().addBox(0.0F, 0.0F, 1.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(56, 36).mirror().addBox(1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 6.0F, -4.0F));

        PartDefinition Right_Arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition book = Right_Arm.addOrReplaceChild("book", CubeListBuilder.create().texOffs(64, 37).mirror().addBox(-7.3F, -14.5F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 22.0F, 0.0F));

        PartDefinition Left_Arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition Right_Arm_Folded = Body.addOrReplaceChild("Right_Arm_Folded", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition rightShoulder = Right_Arm_Folded.addOrReplaceChild("rightShoulder", CubeListBuilder.create().texOffs(56, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(72, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

        PartDefinition rightForeArm = Right_Arm_Folded.addOrReplaceChild("rightForeArm", CubeListBuilder.create().texOffs(88, 16).addBox(-2.01F, -0.6F, -2.3F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(88, 24).addBox(-2.01F, -0.6F, -2.3F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.251F)), PartPose.offsetAndRotation(-1.0F, 2.5F, -3.8F, -1.0472F, 0.0F, 0.0F));

        PartDefinition Left_Arm_Folded = Body.addOrReplaceChild("Left_Arm_Folded", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leftShoulder = Left_Arm_Folded.addOrReplaceChild("leftShoulder", CubeListBuilder.create().texOffs(56, 26).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(72, 26).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

        PartDefinition leftForeArm = Left_Arm_Folded.addOrReplaceChild("leftForeArm", CubeListBuilder.create().texOffs(88, 32).mirror().addBox(-4.0F, -0.6F, -2.3F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(88, 40).mirror().addBox(-4.0F, -0.6F, -2.3F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.252F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 2.5F, -3.8F, -1.0472F, 0.0F, 0.0F));

        PartDefinition Right_Leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.051F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition Left_Leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-1.94F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.591F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        final boolean working = isWorking(entity);
        final boolean studying = entity.getRenderMetadata().contains(RENDER_META_STUDYING);

        rightArm.getChild("book").visible = entity.getRenderMetadata().contains(RENDER_META_BOOK);
        head.getChild("glasses").visible = working;

        body.getChild("Left_Arm_Folded").visible = studying;
        body.getChild("Right_Arm_Folded").visible = studying;
        leftArm.visible = !studying;
        rightArm.visible = !studying;
    }
}
