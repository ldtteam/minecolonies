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

public class ModelEntityFemaleAristocrat extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFemaleAristocrat(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hair1Definition = headDefinition.addOrReplaceChild("hair1",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 3.0F, 6.0F)
          , PartPose.offset(0.0F, -8.0F, 1.0F));

        PartDefinition hair2Definition = headDefinition.addOrReplaceChild("hair2",
          CubeListBuilder.create()
            .texOffs(56, 0).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 2.0F)
          , PartPose.offset(0.0F, -11.0F, 1.0F));

        PartDefinition hair3Definition = headDefinition.addOrReplaceChild("hair3",
          CubeListBuilder.create()
            .texOffs(32, 10).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 2.0F, 4.0F)
          , PartPose.offset(0.0F, -13.0F, 1.0F));

        PartDefinition hair4Definition = headDefinition.addOrReplaceChild("hair4",
          CubeListBuilder.create()
            .texOffs(48, 10).addBox(-4.0F, 8.0F, 2.0F, 4.0F, 10.0F, 1.0F)
            .texOffs(48, 10).addBox(0.0F, 8.0F, 2.0F, 4.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, -8.0F, 1.0F));

        PartDefinition hair6Definition = headDefinition.addOrReplaceChild("hair6",
          CubeListBuilder.create()
            .texOffs(54, 33).addBox(-4.0F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F)
          , PartPose.offset(0.0F, -8.0F, 1.0F));

        PartDefinition hair7Definition = headDefinition.addOrReplaceChild("hair7",
          CubeListBuilder.create()
            .texOffs(59, 33).addBox(3.0F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, -8.0F, 1.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(12, 17).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 3.0F)
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition breastDefinition = bodyDefinition.addOrReplaceChild("breast",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-3.0F, -0.7321F, -3.5F, 8.0F, 4.0F, 3.0F)
          , PartPose.offsetAndRotation(-1.0F, 3.0F, 1.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition dressPart1Definition = bodyDefinition.addOrReplaceChild("dressPart1",
          CubeListBuilder.create()
            .texOffs(18, 33).addBox(-5.3227F, 0.9F, -7.5437F, 12.0F, 6.0F, 11.0F, new CubeDeformation(0.36F))
            .texOffs(0, 40).addBox(21.9002F, -3.6408F, 28.2909F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 40).addBox(-28.8701F, -3.0268F, 24.8091F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 40).addBox(-27.3144F, 6.1334F, -26.5652F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 40).addBox(26.2843F, 5.4701F, -23.3793F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(2.7F, -2.0F, -3.0F, -0.1745F, -0.7854F, 0.0F));

        PartDefinition dressPart2Definition = bodyDefinition.addOrReplaceChild("dressPart2",
          CubeListBuilder.create()
            .texOffs(18, 33).addBox(-5.9F, 0.925F, -6.45F, 12.0F, 6.0F, 11.0F, new CubeDeformation(0.36F))
          , PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition dressPart3Definition = bodyDefinition.addOrReplaceChild("dressPart3",
          CubeListBuilder.create()
            .texOffs(30, 50).addBox(-12.7815F, 0.8F, -22.2562F, 10.0F, 4.0F, 7.0F, new CubeDeformation(0.4F))
          , PartPose.offsetAndRotation(18.9849F, 11.875F, 6.2661F, 0.0F, 0.8203F, 0.0F));

        PartDefinition dressPart4Definition = bodyDefinition.addOrReplaceChild("dressPart4",
          CubeListBuilder.create()
            .texOffs(30, 50).addBox(-5.1087F, -2.0F, -3.0018F, 10.0F, 4.0F, 7.0F, new CubeDeformation(0.31F))
          , PartPose.offsetAndRotation(-1.2963F, 14.675F, -0.7978F, 0.0F, -1.5533F, 0.0F));

        PartDefinition dressPart5Definition = bodyDefinition.addOrReplaceChild("dressPart5",
          CubeListBuilder.create()
            .texOffs(30, 50).addBox(-4.8F, -2.0F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(0.2F))
          , PartPose.offsetAndRotation(2.0094F, 14.675F, -1.0005F, 0.0F, -1.5708F, 0.0F));

        PartDefinition SkirtPart2Definition = bodyDefinition.addOrReplaceChild("SkirtPart2",
          CubeListBuilder.create()
          , PartPose.offset(-36.8F, 9.6F, -3.0F));

        PartDefinition dressPart13Definition = SkirtPart2Definition.addOrReplaceChild("dressPart13",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(21.9002F, -3.6408F, 28.2909F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(-3.0F, -2.0F, -3.0F, -0.1745F, 0.7854F, 0.0F));

        PartDefinition dressPart12Definition = SkirtPart2Definition.addOrReplaceChild("dressPart12",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-28.8701F, -3.0268F, 24.8091F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(-3.0F, -2.0F, 3.0F, -0.1745F, 2.3562F, 0.0F));

        PartDefinition dressPart11Definition = SkirtPart2Definition.addOrReplaceChild("dressPart11",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-27.3144F, 6.1334F, -26.5652F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(3.0F, -2.0F, 3.0F, -0.1745F, -2.3562F, 0.0F));

        PartDefinition dressPart10Definition = SkirtPart2Definition.addOrReplaceChild("dressPart10",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(26.2843F, 5.4701F, -23.3793F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(2.7F, -2.0F, -3.0F, -0.1745F, -0.7854F, 0.0F));

        PartDefinition dressPart9Definition = SkirtPart2Definition.addOrReplaceChild("dressPart9",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(0.1F, 6.2733F, -40.8257F, 4.0F, 7.0F, 4.0F)
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, -1.5708F, 0.0F));

        PartDefinition dressPart8Definition = SkirtPart2Definition.addOrReplaceChild("dressPart8",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-4.1F, -6.5767F, 32.0501F, 4.0F, 7.0F, 4.0F)
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 1.5708F, 0.0F));

        PartDefinition dressPart6Definition = SkirtPart2Definition.addOrReplaceChild("dressPart6",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F)
          , PartPose.offsetAndRotation(36.9F, 3.271F, 4.3333F, 0.2443F, 0.0F, 0.0F));

        PartDefinition dressPart7Definition = SkirtPart2Definition.addOrReplaceChild("dressPart7",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F))
          , PartPose.offsetAndRotation(36.9F, 3.0243F, -0.2986F, -0.2967F, 0.0F, 0.0F));

        PartDefinition umbrellaDefinition = bodyDefinition.addOrReplaceChild("umbrella",
          CubeListBuilder.create()
            .texOffs(0, 54).addBox(-4.0F, -0.0075F, -3.9862F, 9.0F, 1.0F, 9.0F)
            .texOffs(60, 10).addBox(0.0F, -2.0F, 0.0F, 1.0F, 21.0F, 1.0F)
          , PartPose.offsetAndRotation(5.0F, 6.0F, -5.0F, 2.5656F, 0.0F, 0.0F));

        PartDefinition umbrellaHandDefinition = bodyDefinition.addOrReplaceChild("umbrellaHand",
          CubeListBuilder.create()
            .texOffs(60, 10).addBox(0.0F, -2.0F, 0.0F, 1.0F, 21.0F, 1.0F)
          , PartPose.offsetAndRotation(5.0F, 6.0F, -5.0F, 2.5656F, 0.0F, 0.0F));

        PartDefinition leftArm1Definition = bodyDefinition.addOrReplaceChild("leftArm1",
          CubeListBuilder.create()
            .texOffs(34, 17).addBox(0.0F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F)
          , PartPose.offset(4.0F, 0.0F, 0.0F));

        PartDefinition leftArm2Definition = bodyDefinition.addOrReplaceChild("leftArm2",
          CubeListBuilder.create()
            .texOffs(46, 22).addBox(-0.99F, -1.01F, -1.0F, 3.0F, 7.0F, 3.0F)
          , PartPose.offsetAndRotation(5.0F, 6.0F, 1.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(34, 17).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F)
          , PartPose.offset(-5.0F, 0.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 17).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F)
          , PartPose.offset(-1.0F, 12.0F, 1.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
