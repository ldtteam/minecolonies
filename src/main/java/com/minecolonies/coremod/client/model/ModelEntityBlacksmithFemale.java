// Made with Blockbench 3.6.5
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

public class ModelEntityBlacksmithFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityBlacksmithFemale(final ModelPart part)
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
            .texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F)
          , PartPose.offset(-5.0F, 6.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 6.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F)
          , PartPose.offset(-2.0F, 14.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 14.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F).mirror()
            .texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition headdetailDefinition = headDefinition.addOrReplaceChild("headdetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF1Definition = hairDefinition.addOrReplaceChild("hairF1",
          CubeListBuilder.create()
            .texOffs(0, 37).addBox(1.85F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F).mirror()
            .texOffs(25, 53).addBox(-3.7F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F).mirror()
            .texOffs(26, 50).addBox(-0.55F, -2.5F, -4.2F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(31, 50).addBox(1.6F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F).mirror()
            .texOffs(32, 52).addBox(0.6F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(38, 50).addBox(-1.7F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(22, 38).addBox(-4.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F).mirror()
            .texOffs(32, 43).addBox(-4.6F, -4.3F, -4.0F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(59, 43).addBox(-4.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F).mirror()
            .texOffs(42, 37).addBox(1.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
            .texOffs(61, 39).addBox(-2.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF2Definition = hairDefinition.addOrReplaceChild("hairF2",
          CubeListBuilder.create()
            .texOffs(0, 41).addBox(-4.5F, -3.0F, 2.5F, 1.0F, 1.0F, 2.0F).mirror()
            .texOffs(70, 39).addBox(-0.4F, -4.6F, 3.7F, 1.0F, 10.0F, 1.0F).mirror()
            .texOffs(0, 58).addBox(2.8F, -4.4F, 3.7F, 1.0F, 10.0F, 1.0F).mirror()
            .texOffs(4, 58).addBox(3.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F).mirror()
            .texOffs(20, 58).addBox(-3.8F, -4.4F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
            .texOffs(36, 53).addBox(3.3F, 2.5F, -2.0F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(24, 58).addBox(-4.3F, -2.5F, -2.0F, 1.0F, 6.0F, 1.0F).mirror()
            .texOffs(28, 58).addBox(-4.3F, 1.5F, -0.4F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(32, 58).addBox(-4.3F, -1.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(36, 58).addBox(-4.3F, 1.5F, 1.1F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(33, 55).addBox(-4.3F, -2.5F, 1.0F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF3Definition = hairDefinition.addOrReplaceChild("hairF3",
          CubeListBuilder.create()
            .texOffs(10, 38).addBox(-2.95F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F).mirror()
            .texOffs(43, 51).addBox(-4.3F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(40, 55).addBox(-4.3F, -0.5F, -0.7F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(38, 56).addBox(-4.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F).mirror()
            .texOffs(46, 57).addBox(-0.55F, -4.5F, -4.7F, 1.0F, 2.0F, 2.0F).mirror()
            .texOffs(51, 53).addBox(-4.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F).mirror()
            .texOffs(55, 53).addBox(-4.4F, -4.2F, -4.3F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(59, 53).addBox(3.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F).mirror()
            .texOffs(63, 53).addBox(3.3F, 0.5F, -0.3F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(67, 53).addBox(3.3F, -0.5F, -1.8F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(71, 53).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF4Definition = hairDefinition.addOrReplaceChild("hairF4",
          CubeListBuilder.create()
            .texOffs(20, 39).addBox(-1.3F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F).mirror()
            .texOffs(52, 58).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(56, 57).addBox(3.3F, -0.5F, 0.8F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(60, 57).addBox(3.3F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(64, 57).addBox(3.3F, 2.5F, 1.1F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(68, 57).addBox(3.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F).mirror()
            .texOffs(72, 57).addBox(3.3F, -2.5F, 1.0F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(0, 67).addBox(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F).mirror()
            .texOffs(14, 70).addBox(-4.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF5Definition = hairDefinition.addOrReplaceChild("hairF5",
          CubeListBuilder.create()
            .texOffs(30, 40).addBox(0.35F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF6Definition = hairDefinition.addOrReplaceChild("hairF6",
          CubeListBuilder.create()
            .texOffs(41, 42).addBox(3.2F, -3.5F, -3.5F, 1.0F, 1.0F, 7.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF7Definition = hairDefinition.addOrReplaceChild("hairF7",
          CubeListBuilder.create()
            .texOffs(11, 41).addBox(3.5F, -2.5F, 2.5F, 1.0F, 1.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF8Definition = hairDefinition.addOrReplaceChild("hairF8",
          CubeListBuilder.create()
            .texOffs(49, 42).addBox(3.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBackDefinition = hairDefinition.addOrReplaceChild("hairBack",
          CubeListBuilder.create()
            .texOffs(0, 48).addBox(-4.0F, -4.5F, -3.5F, 8.0F, 1.0F, 8.0F).mirror()
            .texOffs(51, 38).addBox(2.1F, -4.4F, 3.6F, 1.0F, 9.0F, 1.0F).mirror()
            .texOffs(8, 58).addBox(-2.9F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F).mirror()
            .texOffs(12, 58).addBox(-1.2F, -4.4F, 3.6F, 1.0F, 11.0F, 1.0F).mirror()
            .texOffs(16, 58).addBox(0.3F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF9Definition = hairDefinition.addOrReplaceChild("hairF9",
          CubeListBuilder.create()
            .texOffs(2, 48).addBox(3.4F, -4.2F, -4.3F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF10Definition = hairDefinition.addOrReplaceChild("hairF10",
          CubeListBuilder.create()
            .texOffs(25, 53).addBox(-3.7F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF11Definition = hairDefinition.addOrReplaceChild("hairF11",
          CubeListBuilder.create()
            .texOffs(26, 50).addBox(-0.55F, -2.5F, -4.2F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF12Definition = hairDefinition.addOrReplaceChild("hairF12",
          CubeListBuilder.create()
            .texOffs(31, 50).addBox(1.6F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF13Definition = hairDefinition.addOrReplaceChild("hairF13",
          CubeListBuilder.create()
            .texOffs(32, 52).addBox(0.6F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF14Definition = hairDefinition.addOrReplaceChild("hairF14",
          CubeListBuilder.create()
            .texOffs(38, 50).addBox(-1.7F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF15Definition = hairDefinition.addOrReplaceChild("hairF15",
          CubeListBuilder.create()
            .texOffs(22, 38).addBox(-4.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF16Definition = hairDefinition.addOrReplaceChild("hairF16",
          CubeListBuilder.create()
            .texOffs(32, 43).addBox(-4.6F, -4.3F, -4.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF17Definition = hairDefinition.addOrReplaceChild("hairF17",
          CubeListBuilder.create()
            .texOffs(59, 43).addBox(-4.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF18Definition = hairDefinition.addOrReplaceChild("hairF18",
          CubeListBuilder.create()
            .texOffs(42, 37).addBox(1.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack1Definition = hairDefinition.addOrReplaceChild("hairBack1",
          CubeListBuilder.create()
            .texOffs(51, 38).addBox(2.1F, -4.4F, 3.6F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF19Definition = hairDefinition.addOrReplaceChild("hairF19",
          CubeListBuilder.create()
            .texOffs(61, 39).addBox(-2.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF20Definition = hairDefinition.addOrReplaceChild("hairF20",
          CubeListBuilder.create()
            .texOffs(70, 39).addBox(-0.4F, -4.6F, 3.7F, 1.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF21Definition = hairDefinition.addOrReplaceChild("hairF21",
          CubeListBuilder.create()
            .texOffs(0, 58).addBox(2.8F, -4.4F, 3.7F, 1.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF22Definition = hairDefinition.addOrReplaceChild("hairF22",
          CubeListBuilder.create()
            .texOffs(4, 58).addBox(3.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack2Definition = hairDefinition.addOrReplaceChild("hairBack2",
          CubeListBuilder.create()
            .texOffs(8, 58).addBox(-2.9F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack3Definition = hairDefinition.addOrReplaceChild("hairBack3",
          CubeListBuilder.create()
            .texOffs(12, 58).addBox(-1.2F, -4.4F, 3.6F, 1.0F, 11.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack4Definition = hairDefinition.addOrReplaceChild("hairBack4",
          CubeListBuilder.create()
            .texOffs(16, 58).addBox(0.3F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF23Definition = hairDefinition.addOrReplaceChild("hairF23",
          CubeListBuilder.create()
            .texOffs(20, 58).addBox(-3.8F, -4.4F, 3.7F, 1.0F, 9.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF24Definition = hairDefinition.addOrReplaceChild("hairF24",
          CubeListBuilder.create()
            .texOffs(36, 53).addBox(3.3F, 2.5F, -2.0F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF25Definition = hairDefinition.addOrReplaceChild("hairF25",
          CubeListBuilder.create()
            .texOffs(24, 58).addBox(-4.3F, -2.5F, -2.0F, 1.0F, 6.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF26Definition = hairDefinition.addOrReplaceChild("hairF26",
          CubeListBuilder.create()
            .texOffs(28, 58).addBox(-4.3F, 1.5F, -0.4F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF27Definition = hairDefinition.addOrReplaceChild("hairF27",
          CubeListBuilder.create()
            .texOffs(32, 58).addBox(-4.3F, -1.5F, 1.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF28Definition = hairDefinition.addOrReplaceChild("hairF28",
          CubeListBuilder.create()
            .texOffs(36, 58).addBox(-4.3F, 1.5F, 1.1F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF29Definition = hairDefinition.addOrReplaceChild("hairF29",
          CubeListBuilder.create()
            .texOffs(33, 55).addBox(-4.3F, -2.5F, 1.0F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF30Definition = hairDefinition.addOrReplaceChild("hairF30",
          CubeListBuilder.create()
            .texOffs(43, 51).addBox(-4.3F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF31Definition = hairDefinition.addOrReplaceChild("hairF31",
          CubeListBuilder.create()
            .texOffs(40, 55).addBox(-4.3F, -0.5F, -0.7F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF32Definition = hairDefinition.addOrReplaceChild("hairF32",
          CubeListBuilder.create()
            .texOffs(38, 56).addBox(-4.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF33Definition = hairDefinition.addOrReplaceChild("hairF33",
          CubeListBuilder.create()
            .texOffs(46, 57).addBox(-0.55F, -4.5F, -4.7F, 1.0F, 2.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF34Definition = hairDefinition.addOrReplaceChild("hairF34",
          CubeListBuilder.create()
            .texOffs(51, 53).addBox(-4.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF35Definition = hairDefinition.addOrReplaceChild("hairF35",
          CubeListBuilder.create()
            .texOffs(55, 53).addBox(-4.4F, -4.2F, -4.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF36Definition = hairDefinition.addOrReplaceChild("hairF36",
          CubeListBuilder.create()
            .texOffs(59, 53).addBox(3.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF37Definition = hairDefinition.addOrReplaceChild("hairF37",
          CubeListBuilder.create()
            .texOffs(63, 53).addBox(3.3F, 0.5F, -0.3F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF38Definition = hairDefinition.addOrReplaceChild("hairF38",
          CubeListBuilder.create()
            .texOffs(67, 53).addBox(3.3F, -0.5F, -1.8F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF39Definition = hairDefinition.addOrReplaceChild("hairF39",
          CubeListBuilder.create()
            .texOffs(71, 53).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF40Definition = hairDefinition.addOrReplaceChild("hairF40",
          CubeListBuilder.create()
            .texOffs(52, 58).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF41Definition = hairDefinition.addOrReplaceChild("hairF41",
          CubeListBuilder.create()
            .texOffs(56, 57).addBox(3.3F, -0.5F, 0.8F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF42Definition = hairDefinition.addOrReplaceChild("hairF42",
          CubeListBuilder.create()
            .texOffs(60, 57).addBox(3.3F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF43Definition = hairDefinition.addOrReplaceChild("hairF43",
          CubeListBuilder.create()
            .texOffs(64, 57).addBox(3.3F, 2.5F, 1.1F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF44Definition = hairDefinition.addOrReplaceChild("hairF44",
          CubeListBuilder.create()
            .texOffs(68, 57).addBox(3.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF45Definition = hairDefinition.addOrReplaceChild("hairF45",
          CubeListBuilder.create()
            .texOffs(72, 57).addBox(3.3F, -2.5F, 1.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF46Definition = hairDefinition.addOrReplaceChild("hairF46",
          CubeListBuilder.create()
            .texOffs(0, 67).addBox(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairF47Definition = hairDefinition.addOrReplaceChild("hairF47",
          CubeListBuilder.create()
            .texOffs(14, 70).addBox(-4.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBand1Definition = hairDefinition.addOrReplaceChild("hairBand1",
          CubeListBuilder.create()
            .texOffs(20, 69).addBox(-3.5F, -3.6F, -4.1F, 7.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBand2Definition = hairDefinition.addOrReplaceChild("hairBand2",
          CubeListBuilder.create()
            .texOffs(18, 71).addBox(-4.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBandDefinition = hairDefinition.addOrReplaceChild("hairBand",
          CubeListBuilder.create()
            .texOffs(20, 69).addBox(-3.5F, -3.6F, -4.1F, 7.0F, 1.0F, 1.0F).mirror()
            .texOffs(18, 71).addBox(-4.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F).mirror()
            .texOffs(29, 64).addBox(3.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(0, 30).addBox(-3.5F, -1.0001F, -5.0F, 7.0F, 3.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 1.0782F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
