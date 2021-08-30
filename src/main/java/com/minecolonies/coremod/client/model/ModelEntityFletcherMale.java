// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityFletcherMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFletcherMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(87, 18).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F).mirror()
            .texOffs(5, 38).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F).mirror()
            .texOffs(8, 39).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F).mirror()
            .texOffs(12, 39).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F)
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition knifebladeDefinition = bodyDefinition.addOrReplaceChild("knifeblade",
          CubeListBuilder.create()
            .texOffs(72, 26).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F).mirror()
            .texOffs(79, 26).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F).mirror()
            .texOffs(86, 26).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F)
            .texOffs(69, 26).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F).mirror()
            .texOffs(76, 26).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F).mirror()
            .texOffs(83, 26).addBox(-0.5F, -15.5F, -3.35F, 0.25F, 2.5F, 0.6F)
            .texOffs(69, 26).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F).mirror()
            .texOffs(76, 26).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F).mirror()
            .texOffs(83, 26).addBox(-0.75F, -15.5F, -3.35F, 0.25F, 2.25F, 0.6F)
            .texOffs(83, 26).addBox(-1.0F, -15.5F, -3.35F, 0.25F, 1.75F, 0.6F)
          , PartPose.offset(2.5F, 26.0F, 0.5F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(69, 19).addBox(-1.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
            .texOffs(69, 13).addBox(-3.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(101, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F)
            .texOffs(0, 45).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F)
            .texOffs(52, 52).addBox(1.0F, -30.0F, -4.5F, 1.25F, 0.25F, 0.5F)
            .texOffs(45, 56).addBox(0.0F, -30.0F, -4.5F, 1.0F, 0.5F, 0.5F)
            .texOffs(33, 47).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 0.75F, 0.5F)
            .texOffs(41, 47).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 1.0F, 0.5F)
            .texOffs(58, 52).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F)
            .texOffs(58, 55).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F)
            .texOffs(36, 54).addBox(2.25F, -29.8F, -4.5F, 0.5F, 0.75F, 0.5F)
            .texOffs(50, 47).addBox(2.75F, -29.8F, -4.5F, 1.25F, 1.5F, 0.5F)
            .texOffs(54, 47).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.25F, 0.5F)
            .texOffs(77, 46).addBox(4.0F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F)
            .texOffs(88, 48).addBox(-4.25F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F)
            .texOffs(83, 45).addBox(4.0F, -27.0F, 1.75F, 0.25F, 3.75F, 2.25F)
            .texOffs(95, 45).addBox(-4.25F, -27.0F, 1.75F, 0.25F, 4.0F, 2.25F)
            .texOffs(73, 46).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.0F, 1.25F)
            .texOffs(61, 46).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 3.0F, 1.25F)
            .texOffs(69, 45).addBox(4.0F, -32.0F, -3.0F, 0.25F, 4.5F, 1.5F)
            .texOffs(65, 45).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 4.25F, 1.5F)
            .texOffs(76, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F)
            .texOffs(88, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
