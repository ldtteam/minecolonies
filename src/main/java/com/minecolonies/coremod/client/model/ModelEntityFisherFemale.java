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

public class ModelEntityFisherFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFisherFemale(final ModelPart part)
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
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition rightBootDefinition = rightLegDefinition.addOrReplaceChild("rightBoot",
          CubeListBuilder.create()
            .texOffs(20, 102).addBox(-0.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition leftBootDefinition = leftLegDefinition.addOrReplaceChild("leftBoot",
          CubeListBuilder.create()
            .texOffs(0, 102).addBox(-4.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack1Definition = headDefinition.addOrReplaceChild("hairBack1",
          CubeListBuilder.create()
            .texOffs(0, 74).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(0, 53).addBox(-1.5F, 6.5F, 3.5F, 3.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack2Definition = headDefinition.addOrReplaceChild("hairBack2",
          CubeListBuilder.create()
            .texOffs(5, 74).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack3Definition = headDefinition.addOrReplaceChild("hairBack3",
          CubeListBuilder.create()
            .texOffs(0, 63).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack4Definition = headDefinition.addOrReplaceChild("hairBack4",
          CubeListBuilder.create()
            .texOffs(12, 69).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack5Definition = headDefinition.addOrReplaceChild("hairBack5",
          CubeListBuilder.create()
            .texOffs(0, 69).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack6Definition = headDefinition.addOrReplaceChild("hairBack6",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack7Definition = headDefinition.addOrReplaceChild("hairBack7",
          CubeListBuilder.create()
            .texOffs(0, 56).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack8Definition = headDefinition.addOrReplaceChild("hairBack8",
          CubeListBuilder.create()
            .texOffs(0, 45).addBox(-3.5F, 0.5F, 3.5F, 7.0F, 4.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack9Definition = headDefinition.addOrReplaceChild("hairBack9",
          CubeListBuilder.create()
            .texOffs(0, 50).addBox(-2.5F, 4.5F, 3.5F, 5.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairBack10Definition = headDefinition.addOrReplaceChild("hairBack10",
          CubeListBuilder.create()
            .texOffs(0, 53).addBox(-1.5F, 6.5F, 3.5F, 3.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition stringDefinition = bodyDefinition.addOrReplaceChild("string",
          CubeListBuilder.create()
            .texOffs(53, 38).addBox(-8.0F, -0.5F, -2.3F, 1.0F, 5.0F, 1.0F).mirror()
            .texOffs(53, 44).addBox(-9.05F, 1.65F, -2.3F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.3756F));

        PartDefinition string2Definition = bodyDefinition.addOrReplaceChild("string2",
          CubeListBuilder.create()
            .texOffs(53, 44).addBox(-9.05F, 1.65F, -2.3F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.3756F));

        PartDefinition hookTie1Definition = bodyDefinition.addOrReplaceChild("hookTie1",
          CubeListBuilder.create()
            .texOffs(58, 38).addBox(-3.5F, 7.0F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hookTie2Definition = bodyDefinition.addOrReplaceChild("hookTie2",
          CubeListBuilder.create()
            .texOffs(58, 42).addBox(-1.5F, 8.5F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hookTie3Definition = bodyDefinition.addOrReplaceChild("hookTie3",
          CubeListBuilder.create()
            .texOffs(58, 46).addBox(1.0F, 9.0F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish1Definition = bodyDefinition.addOrReplaceChild("fish1",
          CubeListBuilder.create()
            .texOffs(61, 38).addBox(-4.4F, 9.0F, -2.2F, 2.0F, 3.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish2Definition = bodyDefinition.addOrReplaceChild("fish2",
          CubeListBuilder.create()
            .texOffs(61, 42).addBox(-2.0F, 10.5F, -2.2F, 2.0F, 3.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish3Definition = bodyDefinition.addOrReplaceChild("fish3",
          CubeListBuilder.create()
            .texOffs(61, 46).addBox(0.9F, 11.0F, -2.2F, 2.0F, 3.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition reelDefinition = bodyDefinition.addOrReplaceChild("reel",
          CubeListBuilder.create()
            .texOffs(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7436F));

        PartDefinition lineDefinition = bodyDefinition.addOrReplaceChild("line",
          CubeListBuilder.create()
            .texOffs(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7436F));

        PartDefinition poleDefinition = bodyDefinition.addOrReplaceChild("pole",
          CubeListBuilder.create()
            .texOffs(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7436F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(25, 32).addBox(-3.5F, 3.5F, 0.0F, 7.0F, 3.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.632F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
