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

public class MaleMechanistModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleMechanistModel(final ModelPart part)
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

        PartDefinition glassesOff = bipedHead.addOrReplaceChild("glassesOff", CubeListBuilder.create().texOffs(98, 16).addBox(-4.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(108, 16).addBox(3.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(104, 10).addBox(4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(88, 10).addBox(-4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(96, 19).addBox(-4.0F, -4.0F, 4.01F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(101, 12).addBox(-1.0F, -4.1F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.21F)), PartPose.offset(0.0F, -2.6F, 0.0F));

        PartDefinition crystalLeft2 = glassesOff.addOrReplaceChild("crystalLeft2", CubeListBuilder.create().texOffs(111, 12).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 10).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(117, 11).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(107, 11).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 15).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(4.0F, 1.5F, 0.0F));

        PartDefinition crystalRight2 = glassesOff.addOrReplaceChild("crystalRight2", CubeListBuilder.create().texOffs(91, 12).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 10).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(97, 11).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(87, 11).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 15).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition glassesOn = bipedHead.addOrReplaceChild("glassesOn", CubeListBuilder.create().texOffs(98, 6).addBox(-4.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(108, 6).addBox(3.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(104, 0).addBox(4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(88, 0).addBox(-4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(96, 9).addBox(-4.0F, -4.0F, 4.01F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(101, 2).addBox(-1.0F, -4.1F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.21F)), PartPose.offset(0.0F, -0.2F, 0.0F));

        PartDefinition crystalLeft = glassesOn.addOrReplaceChild("crystalLeft", CubeListBuilder.create().texOffs(111, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(117, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(107, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(4.0F, 1.5F, 0.0F));

        PartDefinition crystalRight = glassesOn.addOrReplaceChild("crystalRight", CubeListBuilder.create().texOffs(91, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(97, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(87, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

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
        final boolean working = isWorking(entity);
        head.getChild("glassesOn").visible = working;
        head.getChild("glassesOff").visible = !working;
    }
}
