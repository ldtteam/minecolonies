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

public class ModelEntityTeacherMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityTeacherMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F).mirror()
            .texOffs(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F).mirror()
            .texOffs(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F).mirror()
            .texOffs(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F).mirror()
            .texOffs(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F).mirror()
            .texOffs(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F).mirror()
            .texOffs(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F).mirror()
            .texOffs(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F).mirror()
            .texOffs(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F).mirror()
            .texOffs(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F).mirror()
            .texOffs(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F).mirror()
            .texOffs(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_top_1Definition = hairDefinition.addOrReplaceChild("left_top_1",
          CubeListBuilder.create()
            .texOffs(0, 32).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 2.0F, 9.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition backhairDefinition = hairDefinition.addOrReplaceChild("backhair",
          CubeListBuilder.create()
            .texOffs(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairbackTop_2Definition = hairDefinition.addOrReplaceChild("hairbackTop_2",
          CubeListBuilder.create()
            .texOffs(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition hairbackTop_3Definition = hairDefinition.addOrReplaceChild("hairbackTop_3",
          CubeListBuilder.create()
            .texOffs(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, -4.0F));

        PartDefinition hairBackTop_4Definition = hairDefinition.addOrReplaceChild("hairBackTop_4",
          CubeListBuilder.create()
            .texOffs(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairTop_1Definition = hairDefinition.addOrReplaceChild("hairTop_1",
          CubeListBuilder.create()
            .texOffs(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairTop_2Definition = hairDefinition.addOrReplaceChild("hairTop_2",
          CubeListBuilder.create()
            .texOffs(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairLeftTop_1Definition = hairDefinition.addOrReplaceChild("hairLeftTop_1",
          CubeListBuilder.create()
            .texOffs(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairLeftTop_2Definition = hairDefinition.addOrReplaceChild("hairLeftTop_2",
          CubeListBuilder.create()
            .texOffs(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairLeftTop_3Definition = hairDefinition.addOrReplaceChild("hairLeftTop_3",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairLeftTop_4Definition = hairDefinition.addOrReplaceChild("hairLeftTop_4",
          CubeListBuilder.create()
            .texOffs(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition hairLeftTop_5Definition = hairDefinition.addOrReplaceChild("hairLeftTop_5",
          CubeListBuilder.create()
            .texOffs(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairbackbuttom1Definition = hairDefinition.addOrReplaceChild("hairbackbuttom1",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ponytail_1Definition = hairDefinition.addOrReplaceChild("ponytail_1",
          CubeListBuilder.create()
          , PartPose.offsetAndRotation(7.0F, 4.2F, 2.0F, -1.4486F, 0.0F, 0.0F));

        PartDefinition ponytail_2Definition = hairDefinition.addOrReplaceChild("ponytail_2",
          CubeListBuilder.create()
          , PartPose.offsetAndRotation(6.0F, -1.0F, 0.0F, -1.0647F, 0.0F, 0.0F));

        PartDefinition ponytail_3Definition = hairDefinition.addOrReplaceChild("ponytail_3",
          CubeListBuilder.create()
          , PartPose.offsetAndRotation(6.5F, 0.9F, 0.7F, -1.3613F, 0.0F, 0.0F));

        PartDefinition hairRightTop_1Definition = hairDefinition.addOrReplaceChild("hairRightTop_1",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairfrontTop_1Definition = hairDefinition.addOrReplaceChild("hairfrontTop_1",
          CubeListBuilder.create()
            .texOffs(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairfrontTop_2Definition = hairDefinition.addOrReplaceChild("hairfrontTop_2",
          CubeListBuilder.create()
            .texOffs(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairfrontTop_3Definition = hairDefinition.addOrReplaceChild("hairfrontTop_3",
          CubeListBuilder.create()
            .texOffs(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
