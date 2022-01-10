// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.render.modeltype.registry.ModelTypeRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityBakerFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityBakerFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breastDefinition = bodyDefinition.addOrReplaceChild("breast",
          CubeListBuilder.create()
            .texOffs(18, 33).addBox(-2.5F, 2.5F, -5.366F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.5F))
          , PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headdetailDefinition = headDefinition.addOrReplaceChild("headdetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponytailDefinition = headDefinition.addOrReplaceChild("ponytail",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ponyTailBDefinition = ponytailDefinition.addOrReplaceChild("ponyTailB",
          CubeListBuilder.create()
            .texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ponyTailTDefinition = ponytailDefinition.addOrReplaceChild("ponyTailT",
          CubeListBuilder.create()
            .texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition hatPieceDefinition = headDefinition.addOrReplaceChild("hatPiece",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition topLDefinition = hatPieceDefinition.addOrReplaceChild("topL",
          CubeListBuilder.create()
            .texOffs(64, 4).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition topFDefinition = hatPieceDefinition.addOrReplaceChild("topF",
          CubeListBuilder.create()
            .texOffs(64, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition topRDefinition = hatPieceDefinition.addOrReplaceChild("topR",
          CubeListBuilder.create()
            .texOffs(78, 4).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 1.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition midRDefinition = hatPieceDefinition.addOrReplaceChild("midR",
          CubeListBuilder.create()
            .texOffs(76, 10).addBox(-4.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition midLDefinition = hatPieceDefinition.addOrReplaceChild("midL",
          CubeListBuilder.create()
            .texOffs(64, 10).addBox(1.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lipRDefinition = hatPieceDefinition.addOrReplaceChild("lipR",
          CubeListBuilder.create()
            .texOffs(22, 70).addBox(2.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1842F, -0.8754F, -1.2905F));

        PartDefinition lipTDefinition = hatPieceDefinition.addOrReplaceChild("lipT",
          CubeListBuilder.create()
            .texOffs(0, 67).addBox(-5.0F, -9.2F, -1.0F, 10.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2231F, 0.0F, 0.0F));

        PartDefinition lipLDefinition = hatPieceDefinition.addOrReplaceChild("lipL",
          CubeListBuilder.create()
            .texOffs(0, 70).addBox(-4.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1844F, 0.8755F, 1.2904F));

        PartDefinition lipBDefinition = hatPieceDefinition.addOrReplaceChild("lipB",
          CubeListBuilder.create()
            .texOffs(0, 80).addBox(-5.0F, -5.1F, -1.5F, 10.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.3756F, 0.0F, 0.0F));

        PartDefinition baseTDefinition = hatPieceDefinition.addOrReplaceChild("baseT",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-4.5F, -8.2F, -6.5F, 9.0F, 1.0F, 6.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8923F, 0.0F, 0.0F));

        PartDefinition baseBDefinition = hatPieceDefinition.addOrReplaceChild("baseB",
          CubeListBuilder.create()
            .texOffs(0, 57).addBox(-5.0F, -5.2F, -8.0F, 10.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8923F, 0.0F, 0.0F));

        PartDefinition baseMDefinition = hatPieceDefinition.addOrReplaceChild("baseM",
          CubeListBuilder.create()
            .texOffs(0, 47).addBox(-4.5F, -7.2F, -7.5F, 9.0F, 2.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8923F, 0.0F, 0.0F));

        PartDefinition botLDefinition = hatPieceDefinition.addOrReplaceChild("botL",
          CubeListBuilder.create()
            .texOffs(64, 14).addBox(1.5F, -5.5F, -1.5F, 3.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
