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

public class ModelEntityBeekeeperMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityBeekeeperMale(final ModelPart part)
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

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(38, 50).addBox(-1.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F)
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
            .texOffs(38, 42).addBox(-3.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatBottomDefinition = headDefinition.addOrReplaceChild("hatBottom",
          CubeListBuilder.create()
            .texOffs(57, 25).addBox(-5.5F, -5.4856F, -5.457F, 11.25F, 1.0F, 10.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

        PartDefinition hatTopDefinition = headDefinition.addOrReplaceChild("hatTop",
          CubeListBuilder.create()
            .texOffs(64, 3).addBox(-3.5F, -9.0358F, -2.9483F, 7.0F, 1.0F, 5.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

        PartDefinition hatRightDefinition = headDefinition.addOrReplaceChild("hatRight",
          CubeListBuilder.create()
            .texOffs(81, 40).addBox(-5.5F, -4.3454F, 1.5018F, 11.0F, 5.0F, 1.0F).mirror()
            .texOffs(58, 46).addBox(4.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F).mirror()
            .texOffs(76, 46).addBox(-5.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F).mirror()
            .texOffs(57, 40).addBox(-5.5F, -4.4849F, -7.5006F, 11.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, -0.0349F, 0.0F, 0.0F));

        PartDefinition hatNeckDefinition = headDefinition.addOrReplaceChild("hatNeck",
          CubeListBuilder.create()
            .texOffs(92, 8).addBox(-4.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F).mirror()
            .texOffs(97, 8).addBox(3.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F).mirror()
            .texOffs(92, 17).addBox(-4.5F, 1.05F, -2.7F, 8.75F, 0.75F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition hatMDefinition = headDefinition.addOrReplaceChild("hatM",
          CubeListBuilder.create()
            .texOffs(58, 11).addBox(-4.75F, -8.3358F, -4.6983F, 9.5F, 3.0F, 8.6F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
