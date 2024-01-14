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
import org.jetbrains.annotations.NotNull;

public class MaleBakerModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleBakerModel(final ModelPart part)
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

        PartDefinition hat = bipedHead.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(92, 36).mirror().addBox(-4.5F, -1.0F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.1F)).mirror(false)
          .texOffs(98, 47).mirror().addBox(-3.5F, -2.0F, -3.7F, 7.0F, 1.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false)
          .texOffs(104, 56).mirror().addBox(-2.5F, -3.0F, -3.3F, 5.0F, 1.0F, 7.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, -7.8F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition BakerHat = bipedHead.addOrReplaceChild("BakerHat", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition top_r1 = BakerHat.addOrReplaceChild("top_r1", CubeListBuilder.create().texOffs(86, 12).mirror().addBox(-6.4F, -1.1F, -6.2F, 11.0F, 2.0F, 10.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.9564F, -1.8335F, 0.6076F, 0.0469F, -0.1477F));

        PartDefinition middle_r1 = BakerHat.addOrReplaceChild("middle_r1", CubeListBuilder.create().texOffs(94, 24).mirror().addBox(-4.48F, -1.8F, -4.7F, 9.0F, 4.0F, 8.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.8021F, -1.7031F, 0.5659F, 0.0334F, -0.0807F));

        PartDefinition base_r1 = BakerHat.addOrReplaceChild("base_r1", CubeListBuilder.create().texOffs(88, 0).mirror().addBox(-5.0F, -1.1F, -5.4F, 10.0F, 2.0F, 10.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.5F, -1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition apronBody = bipedBody.addOrReplaceChild("apronBody", CubeListBuilder.create().texOffs(78, 47).addBox(-4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(68, 47).addBox(4.4F, -13.9F, -2.9F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.15F))
          .texOffs(70, 53).addBox(-4.0F, -13.8F, 2.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.25F))
          .texOffs(70, 43).addBox(-4.0F, -20.0F, -2.8F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.249F))
          .texOffs(68, 47).addBox(2.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(86, 47).addBox(-3.7F, -24.4F, -2.7F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.15F))
          .texOffs(64, 43).addBox(-3.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(82, 43).addBox(2.7F, -24.4F, -2.4F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.15F))
          .texOffs(72, 54).addBox(-2.5F, -24.4F, 0.6F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 24.0F, 0.3F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition apronRightLeg = bipedRightLeg.addOrReplaceChild("apronRightLeg", CubeListBuilder.create().texOffs(78, 56).addBox(-4.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition apronLeftLeg = bipedLeftLeg.addOrReplaceChild("apronLeftLeg", CubeListBuilder.create().texOffs(70, 56).addBox(0.25F, -11.5F, -2.5F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        final boolean working = isWorking(entity);
        head.getChild("BakerHat").visible = working && displayHat(entity);
        body.getChild("apronBody").visible = working;
        leftLeg.getChild("apronLeftLeg").visible = working;
        rightLeg.getChild("apronRightLeg").visible = working;
    }
}
