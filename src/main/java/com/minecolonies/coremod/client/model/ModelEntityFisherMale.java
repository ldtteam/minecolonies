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

public class ModelEntityFisherMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFisherMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;

        IMinecoloniesAPI.getInstance().getModelTypeRegistry().register(BipedModelType.FISHER, false, this);
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition rightBootDefinition = rightLegDefinition.addOrReplaceChild("rightBoot",
          CubeListBuilder.create()
            .texOffs(20, 79).addBox(-0.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition leftBootDefinition = leftLegDefinition.addOrReplaceChild("leftBoot",
          CubeListBuilder.create()
            .texOffs(0, 79).addBox(-4.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HatDefinition = headDefinition.addOrReplaceChild("Hat",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition shape6Definition = HatDefinition.addOrReplaceChild("shape6",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 2.0F, 10.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition shape7Definition = shape6Definition.addOrReplaceChild("shape7",
          CubeListBuilder.create()
            .texOffs(0, 59).addBox(-4.0F, -10.4F, -4.5F, 8.0F, 2.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0744F, 0.0F, 0.0F));

        PartDefinition shape5Definition = shape6Definition.addOrReplaceChild("shape5",
          CubeListBuilder.create()
            .texOffs(24, 48).addBox(-5.7509F, -8.2682F, -6.0492F, 2.0F, 1.0F, 10.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -0.5432F, 0.5612F, -0.0045F, 0.0F, -0.1487F));

        PartDefinition shape4Definition = HatDefinition.addOrReplaceChild("shape4",
          CubeListBuilder.create()
            .texOffs(0, 69).addBox(-3.0F, -12.0F, -3.5F, 6.0F, 1.0F, 6.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -23.0F, 0.0F, -0.1616F, 0.0F, 0.0F));

        PartDefinition shape3Definition = HatDefinition.addOrReplaceChild("shape3",
          CubeListBuilder.create()
            .texOffs(0, 45).addBox(-5.0F, -8.7F, -6.2F, 10.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.0744F, 0.0F, 0.0F));

        PartDefinition shape2Definition = HatDefinition.addOrReplaceChild("shape2",
          CubeListBuilder.create()
            .texOffs(0, 48).addBox(3.7F, -8.65F, -5.5F, 2.0F, 1.0F, 10.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -0.0744F, 0.0F, 0.1487F));

        PartDefinition shape1Definition = HatDefinition.addOrReplaceChild("shape1",
          CubeListBuilder.create()
            .texOffs(24, 45).addBox(-5.0F, -8.6F, 3.2F, 10.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -0.2231F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition stringDefinition = bodyDefinition.addOrReplaceChild("string",
          CubeListBuilder.create()
            .texOffs(53, 38).addBox(-5.0F, -0.5F, -2.3F, 1.0F, 12.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7436F));

        PartDefinition poleDefinition = stringDefinition.addOrReplaceChild("pole",
          CubeListBuilder.create()
            .texOffs(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition reelDefinition = poleDefinition.addOrReplaceChild("reel",
          CubeListBuilder.create()
            .texOffs(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lineDefinition = poleDefinition.addOrReplaceChild("line",
          CubeListBuilder.create()
            .texOffs(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hookTie1Definition = bodyDefinition.addOrReplaceChild("hookTie1",
          CubeListBuilder.create()
            .texOffs(58, 38).addBox(-3.5F, 3.5F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish1Definition = hookTie1Definition.addOrReplaceChild("fish1",
          CubeListBuilder.create()
            .texOffs(61, 38).addBox(-4.4F, 5.5F, -2.2F, 2.0F, 4.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hookTie2Definition = bodyDefinition.addOrReplaceChild("hookTie2",
          CubeListBuilder.create()
            .texOffs(58, 42).addBox(-1.5F, 5.5F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish2Definition = hookTie2Definition.addOrReplaceChild("fish2",
          CubeListBuilder.create()
            .texOffs(61, 42).addBox(-2.0F, 7.5F, -2.2F, 2.0F, 4.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hookTie3Definition = bodyDefinition.addOrReplaceChild("hookTie3",
          CubeListBuilder.create()
            .texOffs(58, 46).addBox(0.5F, 8.0F, -2.2F, 1.0F, 2.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fish3Definition = hookTie3Definition.addOrReplaceChild("fish3",
          CubeListBuilder.create()
            .texOffs(61, 46).addBox(0.4F, 10.0F, -2.2F, 2.0F, 4.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
