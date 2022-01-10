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

public class ModelEntityCrafterMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCrafterMale(final ModelPart part)
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
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition RArmRotDefinition = rightArmDefinition.addOrReplaceChild("RArmRot",
          CubeListBuilder.create()
            .texOffs(58, 27).addBox(-0.2F, -6.5F, -1.5F, 1.0F, 1.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(-5.2F, 1.65F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition FingerLDefinition = rightArmDefinition.addOrReplaceChild("FingerL",
          CubeListBuilder.create()
            .texOffs(60, 48).addBox(4.8F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition FingerMDefinition = rightArmDefinition.addOrReplaceChild("FingerM",
          CubeListBuilder.create()
            .texOffs(56, 48).addBox(3.5F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition FingerRDefinition = rightArmDefinition.addOrReplaceChild("FingerR",
          CubeListBuilder.create()
            .texOffs(52, 48).addBox(2.2F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition RArmB2Definition = rightArmDefinition.addOrReplaceChild("RArmB2",
          CubeListBuilder.create()
            .texOffs(52, 45).addBox(1.7F, 3.5F, -2.3F, 4.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition RArmBDefinition = rightArmDefinition.addOrReplaceChild("RArmB",
          CubeListBuilder.create()
            .texOffs(52, 45).addBox(1.7F, 3.5F, -2.3F, 4.0F, 1.0F, 1.0F).mirror()
            .texOffs(48, 40).addBox(1.7F, -2.5F, 1.3F, 1.0F, 6.0F, 1.0F).mirror()
            .texOffs(62, 31).addBox(2.7F, -4.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(82, 35).addBox(1.7F, 4.5F, 1.3F, 3.0F, 1.0F, 1.0F).mirror()
            .texOffs(82, 31).addBox(2.7F, 1.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition RArmLDefinition = rightArmDefinition.addOrReplaceChild("RArmL",
          CubeListBuilder.create()
            .texOffs(48, 33).addBox(1.7F, -2.5F, -2.3F, 1.0F, 6.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition RArmMDefinition = rightArmDefinition.addOrReplaceChild("RArmM",
          CubeListBuilder.create()
            .texOffs(52, 33).addBox(1.7F, -1.5F, -2.3F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition CoreFDefinition = rightArmDefinition.addOrReplaceChild("CoreF",
          CubeListBuilder.create()
            .texOffs(75, 32).addBox(0.8F, -3.7F, 1.7F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(-4.0F, 2.7F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition CoreBDefinition = rightArmDefinition.addOrReplaceChild("CoreB",
          CubeListBuilder.create()
            .texOffs(67, 31).addBox(0.8F, -3.7F, 1.5F, 3.0F, 3.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(-4.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition RArmFTDefinition = rightArmDefinition.addOrReplaceChild("RArmFT",
          CubeListBuilder.create()
            .texOffs(58, 31).addBox(2.7F, -4.5F, -2.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition RArmBTDefinition = rightArmDefinition.addOrReplaceChild("RArmBT",
          CubeListBuilder.create()
            .texOffs(62, 31).addBox(2.7F, -4.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition RArmBBBDefinition = rightArmDefinition.addOrReplaceChild("RArmBBB",
          CubeListBuilder.create()
            .texOffs(82, 35).addBox(1.7F, 4.5F, 1.3F, 3.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition RArmBBMDefinition = rightArmDefinition.addOrReplaceChild("RArmBBM",
          CubeListBuilder.create()
            .texOffs(82, 31).addBox(2.7F, 1.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition Vent12Definition = rightArmDefinition.addOrReplaceChild("Vent12",
          CubeListBuilder.create()
            .texOffs(40, 66).addBox(1.7F, 3.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent10Definition = rightArmDefinition.addOrReplaceChild("Vent10",
          CubeListBuilder.create()
            .texOffs(40, 60).addBox(1.7F, 2.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent8Definition = rightArmDefinition.addOrReplaceChild("Vent8",
          CubeListBuilder.create()
            .texOffs(40, 54).addBox(1.7F, 1.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent6Definition = rightArmDefinition.addOrReplaceChild("Vent6",
          CubeListBuilder.create()
            .texOffs(40, 48).addBox(1.7F, 0.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent4Definition = rightArmDefinition.addOrReplaceChild("Vent4",
          CubeListBuilder.create()
            .texOffs(40, 42).addBox(1.7F, -1.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent1Definition = rightArmDefinition.addOrReplaceChild("Vent1",
          CubeListBuilder.create()
            .texOffs(40, 66).addBox(1.7F, 3.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
            .texOffs(40, 60).addBox(1.7F, 2.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
            .texOffs(40, 33).addBox(1.7F, -2.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
            .texOffs(40, 69).addBox(1.7F, 3.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
            .texOffs(40, 63).addBox(1.7F, 2.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent2Definition = rightArmDefinition.addOrReplaceChild("Vent2",
          CubeListBuilder.create()
            .texOffs(40, 36).addBox(1.7F, -2.0F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent5Definition = rightArmDefinition.addOrReplaceChild("Vent5",
          CubeListBuilder.create()
            .texOffs(40, 45).addBox(1.7F, -0.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent7Definition = rightArmDefinition.addOrReplaceChild("Vent7",
          CubeListBuilder.create()
            .texOffs(40, 51).addBox(1.7F, 0.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent9Definition = rightArmDefinition.addOrReplaceChild("Vent9",
          CubeListBuilder.create()
            .texOffs(40, 57).addBox(1.7F, 1.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent13Definition = rightArmDefinition.addOrReplaceChild("Vent13",
          CubeListBuilder.create()
            .texOffs(40, 69).addBox(1.7F, 3.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent11Definition = rightArmDefinition.addOrReplaceChild("Vent11",
          CubeListBuilder.create()
            .texOffs(40, 63).addBox(1.7F, 2.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Vent3Definition = rightArmDefinition.addOrReplaceChild("Vent3",
          CubeListBuilder.create()
            .texOffs(40, 39).addBox(1.7F, -1.5F, -1.5F, 1.0F, 0.0F, 3.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headdetailDefinition = headDefinition.addOrReplaceChild("headdetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition HatTDefinition = headDefinition.addOrReplaceChild("HatT",
          CubeListBuilder.create()
            .texOffs(0, 76).addBox(-2.0F, -11.5F, -2.7F, 4.0F, 1.0F, 5.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition BrimBaDefinition = headDefinition.addOrReplaceChild("BrimBa",
          CubeListBuilder.create()
            .texOffs(0, 45).addBox(-5.0F, -9.0F, 3.5F, 10.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition BrimRDefinition = headDefinition.addOrReplaceChild("BrimR",
          CubeListBuilder.create()
            .texOffs(0, 57).addBox(-5.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition BrimLDefinition = headDefinition.addOrReplaceChild("BrimL",
          CubeListBuilder.create()
            .texOffs(0, 47).addBox(4.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition BrimFDefinition = headDefinition.addOrReplaceChild("BrimF",
          CubeListBuilder.create()
            .texOffs(0, 43).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition BrimBoDefinition = headDefinition.addOrReplaceChild("BrimBo",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-5.0F, -8.5F, -5.0F, 10.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition HatMDefinition = headDefinition.addOrReplaceChild("HatM",
          CubeListBuilder.create()
            .texOffs(0, 67).addBox(-3.5F, -10.5F, -3.7F, 7.0F, 2.0F, 7.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition SpecLBDefinition = headDefinition.addOrReplaceChild("SpecLB",
          CubeListBuilder.create()
            .texOffs(30, 47).addBox(0.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3346F, 0.0F, 0.0F));

        PartDefinition SpecLDefinition = headDefinition.addOrReplaceChild("SpecL",
          CubeListBuilder.create()
            .texOffs(30, 47).addBox(0.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F).mirror()
            .texOffs(30, 50).addBox(1.3F, -9.0F, -7.4F, 1.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3346F, 0.0F, 0.0F));

        PartDefinition SpecMidDefinition = headDefinition.addOrReplaceChild("SpecMid",
          CubeListBuilder.create()
            .texOffs(30, 44).addBox(-1.0F, -9.0F, -6.3F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3346F, 0.0F, 0.0F));

        PartDefinition SpecRDefinition = headDefinition.addOrReplaceChild("SpecR",
          CubeListBuilder.create()
            .texOffs(23, 47).addBox(-2.3F, -9.0F, -6.7F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(23, 44).addBox(-2.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3346F, 0.0F, 0.0F));

        PartDefinition SpecRBDefinition = headDefinition.addOrReplaceChild("SpecRB",
          CubeListBuilder.create()
            .texOffs(23, 44).addBox(-2.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3346F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
