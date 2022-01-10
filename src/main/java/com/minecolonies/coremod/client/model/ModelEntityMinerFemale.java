// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityMinerFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityMinerFemale(final ModelPart part)
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
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition googlesDefinition = headDefinition.addOrReplaceChild("googles",
          CubeListBuilder.create()
            .texOffs(34, 32).addBox(-4.3333F, -28.3333F, -4.6667F, 2.2F, 0.8667F, 3.5333F)
            .texOffs(33, 37).addBox(0.8667F, -28.3333F, -4.6667F, 3.5333F, 1.0F, 3.5333F)
            .texOffs(45, 36).addBox(-1.0F, -28.3333F, -4.6667F, 1.9333F, 1.0F, 2.3333F)
            .texOffs(46, 40).addBox(-3.0F, -28.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F)
            .texOffs(36, 42).addBox(1.0F, -28.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F)
            .texOffs(32, 33).addBox(1.2667F, -28.2F, -5.2F, 1.4F, 1.1333F, 0.4F)
            .texOffs(43, 33).addBox(-2.7333F, -28.2F, -5.2F, 1.4F, 1.1333F, 0.4F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-4.4667F, -28.0667F, 0.2667F, 1.0F, 1.0F, 3.6667F)
            .texOffs(0, 38).addBox(-4.4667F, -29.0F, -1.0667F, 1.0F, 1.0F, 5.0F)
            .texOffs(0, 45).addBox(-4.4667F, -30.8667F, -4.1333F, 1.1333F, 1.8F, 8.0667F)
            .texOffs(11, 54).addBox(3.4F, -30.8667F, -4.1333F, 1.1F, 1.8F, 8.0667F)
            .texOffs(10, 33).addBox(-3.2667F, -30.8667F, -4.2F, 6.4333F, 1.8F, 3.4F)
            .texOffs(13, 39).addBox(-3.9333F, -30.8667F, 0.7333F, 7.9F, 3.8F, 3.4F)
            .texOffs(11, 47).addBox(-3.2667F, -27.1334F, 0.7333F, 6.4333F, 1.4F, 3.4F)
            .texOffs(1, 56).addBox(-2.3333F, -25.8F, 0.7333F, 4.4333F, 1.0F, 3.4F)
            .texOffs(27, 48).addBox(3.5333F, -29.1333F, -1.0667F, 1.0F, 1.4F, 5.0F)
            .texOffs(22, 56).addBox(3.5333F, -27.8F, 0.2667F, 1.0F, 0.8667F, 3.6667F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition helmetDefinition = headDefinition.addOrReplaceChild("helmet",
          CubeListBuilder.create()
            .texOffs(55, 36).addBox(-3.2333F, 1.4047F, -3.9653F, 1.9333F, 1.7667F, 0.6F)
            .texOffs(52, 36).addBox(-6.8667F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F)
            .texOffs(71, 33).addBox(-2.3F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F)
            .texOffs(31, 47).addBox(-5.1F, 0.838F, -3.6986F, 5.6667F, 1.1333F, 9.2667F)
            .texOffs(54, 55).addBox(-6.4333F, 0.8713F, -3.032F, 8.3333F, 1.1F, 7.9333F)
            .texOffs(80, 53).addBox(-5.2333F, -0.1953F, -3.032F, 5.9333F, 1.1F, 7.9333F)
          , PartPose.offsetAndRotation(2.2667F, -9.8667F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition ponytailDefinition = headDefinition.addOrReplaceChild("ponytail",
          CubeListBuilder.create()
            .texOffs(66, 0).addBox(-4.0667F, -1.5334F, -0.6287F, 0.9667F, 5.2667F, 0.8667F)
          , PartPose.offsetAndRotation(3.3333F, -4.0F, 4.9333F, 0.4364F, 0.0F, 0.0F));

        PartDefinition ponytail2Definition = ponytailDefinition.addOrReplaceChild("ponytail2",
          CubeListBuilder.create()
            .texOffs(66, 7).addBox(-0.35F, -2.5F, -0.3667F, 0.7F, 5.0F, 0.7334F)
          , PartPose.offsetAndRotation(-3.5833F, 5.9396F, -0.9715F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beltDefinition = bodyDefinition.addOrReplaceChild("belt",
          CubeListBuilder.create()
            .texOffs(100, 37).addBox(-4.3333F, -14.6F, -2.2667F, 9.0F, 1.0F, 4.6F)
            .texOffs(101, 44).addBox(-0.8667F, -15.0F, -2.5333F, 2.2F, 1.9333F, 0.6F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition backpackDefinition = bodyDefinition.addOrReplaceChild("backpack",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition stonesDefinition = backpackDefinition.addOrReplaceChild("stones",
          CubeListBuilder.create()
            .texOffs(98, 26).addBox(-1.3481F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F)
            .texOffs(102, 26).addBox(-1.3481F, -22.0152F, 2.2418F, 1.0F, 1.0F, 1.0F)
            .texOffs(106, 26).addBox(-2.0148F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(110, 26).addBox(-2.0148F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F)
            .texOffs(114, 26).addBox(-1.3481F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(118, 26).addBox(-0.2814F, -22.1485F, 3.5752F, 1.0F, 1.0F, 1.0F)
            .texOffs(122, 26).addBox(-0.6814F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(98, 28).addBox(-0.6814F, -21.6152F, 2.9085F, 1.0F, 1.0F, 1.0F)
            .texOffs(102, 28).addBox(-0.6814F, -22.0152F, 2.9333F, 1.0F, 1.0F, 1.0F)
            .texOffs(106, 28).addBox(-0.6814F, -22.0152F, 1.8667F, 1.0F, 1.0F, 1.0F)
            .texOffs(110, 28).addBox(-2.4514F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F)
            .texOffs(114, 28).addBox(-2.4514F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F)
            .texOffs(118, 28).addBox(-2.9975F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F)
            .texOffs(122, 28).addBox(-2.8642F, -21.6821F, 2.025F, 0.8667F, 1.2667F, 1.1333F)
            .texOffs(98, 30).addBox(-2.9975F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F)
            .texOffs(102, 30).addBox(-0.3308F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F)
            .texOffs(106, 30).addBox(-0.3308F, -21.5487F, 2.025F, 1.0F, 1.1333F, 1.0F)
            .texOffs(110, 30).addBox(0.2153F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F)
            .texOffs(114, 30).addBox(0.2153F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F)
            .texOffs(118, 30).addBox(1.9852F, -21.8819F, 2.9333F, 1.0F, 1.0F, 1.0F)
            .texOffs(98, 32).addBox(1.9852F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(102, 32).addBox(1.9852F, -21.6152F, 1.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(106, 32).addBox(1.3186F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(110, 32).addBox(0.7852F, -22.1485F, 2.9085F, 1.0F, 1.0F, 1.0F)
            .texOffs(114, 32).addBox(0.6519F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(118, 32).addBox(0.6519F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F)
            .texOffs(122, 32).addBox(1.3186F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F)
            .texOffs(98, 34).addBox(1.3186F, -22.0152F, 1.9752F, 1.0F, 1.0F, 1.0F)
            .texOffs(102, 34).addBox(-0.3308F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F)
            .texOffs(106, 34).addBox(-0.3308F, -21.8154F, 2.825F, 1.0F, 1.1333F, 1.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torchleftDefinition = backpackDefinition.addOrReplaceChild("torchleft",
          CubeListBuilder.create()
            .texOffs(80, 25).addBox(3.1167F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F)
            .texOffs(88, 25).addBox(3.25F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F)
            .texOffs(88, 23).addBox(2.9833F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition packDefinition = backpackDefinition.addOrReplaceChild("pack",
          CubeListBuilder.create()
            .texOffs(64, 16).addBox(-3.0F, -21.9333F, 5.0F, 6.0F, 7.4F, 1.0F)
            .texOffs(79, 17).addBox(-3.0F, -14.5333F, 2.0F, 6.3333F, 1.0F, 3.0F)
            .texOffs(98, 16).addBox(3.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F)
            .texOffs(107, 16).addBox(-4.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F)
            .texOffs(79, 22).addBox(2.9833F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F)
            .texOffs(84, 22).addBox(-4.0167F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torchrightDefinition = backpackDefinition.addOrReplaceChild("torchright",
          CubeListBuilder.create()
            .texOffs(93, 22).addBox(-4.0167F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F)
            .texOffs(84, 25).addBox(-3.9F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F)
            .texOffs(91, 25).addBox(-3.7083F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(53, 29).addBox(-6.1333F, -2.6114F, 1.5219F, 6.0F, 2.8F, 3.0F)
          , PartPose.offsetAndRotation(3.2F, 1.7333F, -4.9333F, -0.7854F, 0.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition gloverightDefinition = rightArmDefinition.addOrReplaceChild("gloveright",
          CubeListBuilder.create()
            .texOffs(96, 9).addBox(-7.5333F, -16.3333F, -2.6667F, 4.0F, 1.0F, 5.2667F)
            .texOffs(97, 0).addBox(-7.4F, -15.3333F, -2.4F, 3.7333F, 4.0F, 4.7333F)
          , PartPose.offset(5.0F, 22.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition gloveleftDefinition = leftArmDefinition.addOrReplaceChild("gloveleft",
          CubeListBuilder.create()
            .texOffs(75, 9).addBox(3.4667F, -16.3333F, -2.6667F, 4.0F, 1.0F, 5.2667F)
            .texOffs(78, 0).addBox(3.6F, -15.3333F, -2.4F, 3.7333F, 4.0F, 4.7333F)
          , PartPose.offset(-5.0F, 22.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
