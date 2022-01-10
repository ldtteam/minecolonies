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

public class ModelEntityHealerMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityHealerMale(final ModelPart part)
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
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition buttonDefinition = bodyDefinition.addOrReplaceChild("button",
          CubeListBuilder.create()
            .texOffs(119, 17).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F)
          , PartPose.offset(2.0F, 10.3F, -4.2F));

        PartDefinition leftarmcoatDefinition = bodyDefinition.addOrReplaceChild("leftarmcoat",
          CubeListBuilder.create()
            .texOffs(82, 16).addBox(-1.5F, -2.0F, -2.5F, 8.0F, 7.0F, 5.0F).mirror()
          , PartPose.offset(2.0F, 1.0F, 0.0F));

        PartDefinition rightarmcoatDefinition = bodyDefinition.addOrReplaceChild("rightarmcoat",
          CubeListBuilder.create()
            .texOffs(56, 16).addBox(-3.5F, 3.0F, -2.5F, 8.0F, 7.0F, 5.0F).mirror()
          , PartPose.offset(-5.0F, -4.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(26, 32).addBox(-3.5F, 3.0F, -2.5F, 2.0F, 3.0F, 2.0F).mirror()
          , PartPose.offset(5.0F, 7.0F, -1.5F));

        PartDefinition beltDefinition = bodyDefinition.addOrReplaceChild("belt",
          CubeListBuilder.create()
            .texOffs(0, 32).addBox(-3.5F, 3.0F, -2.5F, 9.0F, 2.0F, 6.0F).mirror()
          , PartPose.offset(-1.0F, 8.0F, -0.5F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition leftarmGloveDefinition = leftArmDefinition.addOrReplaceChild("leftarmGlove",
          CubeListBuilder.create()
            .texOffs(69, 30).addBox(-8.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
          , PartPose.offset(7.0F, 6.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition rightlegshoeDefinition = rightLegDefinition.addOrReplaceChild("rightlegshoe",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-1.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
          , PartPose.offset(-1.0F, 17.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition leftlegshoeDefinition = leftLegDefinition.addOrReplaceChild("leftlegshoe",
          CubeListBuilder.create()
            .texOffs(20, 40).addBox(-5.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
          , PartPose.offset(3.0F, 17.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskL2Definition = headDefinition.addOrReplaceChild("MaskL2",
          CubeListBuilder.create()
            .texOffs(75, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskTopDefinition = headDefinition.addOrReplaceChild("MaskTop",
          CubeListBuilder.create()
            .texOffs(71, 8).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.2F, 0.0F));

        PartDefinition MaskL3Definition = headDefinition.addOrReplaceChild("MaskL3",
          CubeListBuilder.create()
            .texOffs(98, 6).addBox(3.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskL1Definition = headDefinition.addOrReplaceChild("MaskL1",
          CubeListBuilder.create()
            .texOffs(92, 3).addBox(3.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskR3Definition = headDefinition.addOrReplaceChild("MaskR3",
          CubeListBuilder.create()
            .texOffs(64, 10).addBox(-4.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskR1Definition = headDefinition.addOrReplaceChild("MaskR1",
          CubeListBuilder.create()
            .texOffs(26, 0).addBox(-4.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskR2Definition = headDefinition.addOrReplaceChild("MaskR2",
          CubeListBuilder.create()
            .texOffs(56, 0).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MaskBottomDefinition = headDefinition.addOrReplaceChild("MaskBottom",
          CubeListBuilder.create()
            .texOffs(75, 12).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition maskFaceDefinition = headDefinition.addOrReplaceChild("maskFace",
          CubeListBuilder.create()
            .texOffs(99, 12).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition eyeLDefinition = headDefinition.addOrReplaceChild("eyeL",
          CubeListBuilder.create()
            .texOffs(33, 1).addBox(-3.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.2F, 1.0F, 0.0F));

        PartDefinition eyeRDefinition = headDefinition.addOrReplaceChild("eyeR",
          CubeListBuilder.create()
            .texOffs(33, 1).addBox(1.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(-0.2F, 1.0F, 0.0F));

        PartDefinition maskFace1Definition = headDefinition.addOrReplaceChild("maskFace1",
          CubeListBuilder.create()
            .texOffs(108, 6).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(7.0F, 2.0F, 0.0F));

        PartDefinition maskFace2Definition = headDefinition.addOrReplaceChild("maskFace2",
          CubeListBuilder.create()
            .texOffs(82, 0).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition maskFace3Definition = headDefinition.addOrReplaceChild("maskFace3",
          CubeListBuilder.create()
            .texOffs(104, 4).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition maskFace4Definition = headDefinition.addOrReplaceChild("maskFace4",
          CubeListBuilder.create()
            .texOffs(100, 0).addBox(-4.0F, -7.5F, -5.0F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(3.0F, 2.0F, 0.0F));

        PartDefinition BrimBDefinition = headDefinition.addOrReplaceChild("BrimB",
          CubeListBuilder.create()
            .texOffs(74, 43).addBox(-4.5F, -9.0F, 4.5F, 9.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition BrimLDefinition = headDefinition.addOrReplaceChild("BrimL",
          CubeListBuilder.create()
            .texOffs(72, 45).addBox(5.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition MidMidDefinition = headDefinition.addOrReplaceChild("MidMid",
          CubeListBuilder.create()
            .texOffs(0, 49).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 1.0F, 7.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition BrimRDefinition = headDefinition.addOrReplaceChild("BrimR",
          CubeListBuilder.create()
            .texOffs(54, 45).addBox(-6.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition BrimFDefinition = headDefinition.addOrReplaceChild("BrimF",
          CubeListBuilder.create()
            .texOffs(94, 43).addBox(-4.5F, -9.0F, -5.5F, 9.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition BrimDefinition = headDefinition.addOrReplaceChild("Brim",
          CubeListBuilder.create()
            .texOffs(43, 54).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 1.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition MidTDefinition = headDefinition.addOrReplaceChild("MidT",
          CubeListBuilder.create()
            .texOffs(82, 46).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition MidBDefinition = headDefinition.addOrReplaceChild("MidB",
          CubeListBuilder.create()
            .texOffs(81, 55).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition BeakBottomDefinition = headDefinition.addOrReplaceChild("BeakBottom",
          CubeListBuilder.create()
            .texOffs(107, 0).addBox(-1.0F, -3.4F, -7.2F, 2.0F, 1.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2782F, 0.0F, 0.0F));

        PartDefinition BeakEnd2Definition = headDefinition.addOrReplaceChild("BeakEnd2",
          CubeListBuilder.create()
            .texOffs(109, 6).addBox(-0.5F, -6.2F, -8.5F, 1.0F, 2.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5749F, 0.0F, 0.0F));

        PartDefinition BeakTopDefinition = headDefinition.addOrReplaceChild("BeakTop",
          CubeListBuilder.create()
            .texOffs(114, 0).addBox(-1.0F, -5.6F, -7.5F, 2.0F, 2.0F, 5.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4004F, 0.0F, 0.0F));

        PartDefinition BeakEnd1Definition = headDefinition.addOrReplaceChild("BeakEnd1",
          CubeListBuilder.create()
            .texOffs(116, 7).addBox(-1.0F, -5.7F, -8.0F, 2.0F, 2.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4528F, 0.0F, 0.0F));

        PartDefinition BeakEnd3Definition = headDefinition.addOrReplaceChild("BeakEnd3",
          CubeListBuilder.create()
            .texOffs(103, 0).addBox(-0.5F, -7.2F, -9.0F, 1.0F, 1.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7669F, 0.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition rightarmGloveDefinition = rightArmDefinition.addOrReplaceChild("rightarmGlove",
          CubeListBuilder.create()
            .texOffs(90, 30).addBox(1.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
          , PartPose.offset(-5.0F, 6.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
