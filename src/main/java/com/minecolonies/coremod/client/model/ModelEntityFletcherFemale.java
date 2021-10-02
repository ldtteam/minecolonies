// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityFletcherFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFletcherFemale(final ModelPart part)
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
            .texOffs(83, 20).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F).mirror()
            .texOffs(5, 40).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F).mirror()
            .texOffs(11, 40).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F).mirror()
            .texOffs(26, 40).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F)
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition knifebladeDefinition = bodyDefinition.addOrReplaceChild("knifeblade",
          CubeListBuilder.create()
            .texOffs(73, 28).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F).mirror()
            .texOffs(80, 28).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F).mirror()
            .texOffs(87, 28).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F)
            .texOffs(70, 28).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F).mirror()
            .texOffs(77, 28).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F).mirror()
            .texOffs(84, 28).addBox(-0.5F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F)
            .texOffs(70, 28).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F).mirror()
            .texOffs(77, 28).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F).mirror()
            .texOffs(84, 28).addBox(-0.75F, -15.5F, -3.25F, 0.25F, 2.25F, 0.5F)
            .texOffs(84, 28).addBox(-1.0F, -15.5F, -3.25F, 0.25F, 1.75F, 0.5F)
          , PartPose.offset(2.5F, 26.0F, 0.5F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(38, 32).addBox(-6.0F, -2.5179F, -10.5745F, 6.0F, 3.0F, 3.0F)
          , PartPose.offsetAndRotation(3.0F, -3.0F, 5.0F, 0.8727F, 0.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
            .texOffs(69, 12).addBox(-1.25F, 7.0F, -2.25F, 3.5F, 1.0F, 4.5F).mirror()
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
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
            .texOffs(69, 18).addBox(-2.25F, 7.0F, -2.25F, 3.5F, 1.0F, 4.5F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(32, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.75F, 5.5F)
            .texOffs(11, 49).addBox(-4.25F, -32.0F, -0.25F, 0.25F, 5.5F, 4.25F)
            .texOffs(111, 48).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 6.0F, 1.5F)
            .texOffs(116, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 6.75F, 1.25F)
            .texOffs(24, 51).addBox(4.0F, -32.0F, -3.0F, 0.25F, 5.25F, 1.5F)
            .texOffs(52, 52).addBox(-2.5F, -19.5F, 3.0F, 5.0F, 3.0F, 1.25F)
            .texOffs(49, 47).addBox(-3.25F, -22.5F, 3.0F, 6.75F, 3.0F, 1.25F)
            .texOffs(66, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F)
            .texOffs(86, 49).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F)
            .texOffs(6, 49).addBox(1.0F, -30.0F, -4.5F, 1.25F, 1.25F, 0.5F)
            .texOffs(39, 48).addBox(0.0F, -30.0F, -4.5F, 1.0F, 2.0F, 0.5F)
            .texOffs(26, 48).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 2.5F, 0.5F)
            .texOffs(0, 55).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 3.0F, 0.5F)
            .texOffs(0, 51).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F)
            .texOffs(0, 46).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F)
            .texOffs(11, 49).addBox(2.25F, -30.0F, -4.5F, 0.5F, 1.0F, 0.5F)
            .texOffs(7, 55).addBox(2.75F, -30.0F, -4.5F, 1.25F, 2.25F, 0.5F)
            .texOffs(0, 48).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.0F, 0.5F)
            .texOffs(45, 54).addBox(4.0F, -26.25F, -0.25F, 0.25F, 2.0F, 2.0F)
            .texOffs(32, 47).addBox(-4.25F, -26.5F, -0.25F, 0.25F, 2.0F, 2.0F)
            .texOffs(43, 47).addBox(4.0F, -26.25F, 1.75F, 0.25F, 3.75F, 2.25F)
            .texOffs(18, 46).addBox(-4.25F, -26.5F, 1.75F, 0.25F, 3.75F, 2.25F)
            .texOffs(28, 52).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.75F, 1.25F)
            .texOffs(90, 49).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 5.5F, 1.25F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
