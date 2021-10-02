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

public class ModelEntityPigFarmerMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityPigFarmerMale(final ModelPart part)
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

        PartDefinition carrot1Definition = bodyDefinition.addOrReplaceChild("carrot1",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-2.5F, 6.0F, -1.5F, 1.0F, 3.0F, 0.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1115F, 0.0F, -0.0175F));

        PartDefinition carrot2Definition = bodyDefinition.addOrReplaceChild("carrot2",
          CubeListBuilder.create()
            .texOffs(2, 33).addBox(0.5F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3346F, 0.1115F));

        PartDefinition carrot3Definition = bodyDefinition.addOrReplaceChild("carrot3",
          CubeListBuilder.create()
            .texOffs(4, 33).addBox(1.0F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1115F, 0.1487F));

        PartDefinition carrot4Definition = bodyDefinition.addOrReplaceChild("carrot4",
          CubeListBuilder.create()
            .texOffs(6, 33).addBox(0.0F, 6.5F, -2.5F, 1.0F, 3.0F, 0.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1487F, -0.1859F));

        PartDefinition carrotBaseDefinition = bodyDefinition.addOrReplaceChild("carrotBase",
          CubeListBuilder.create()
            .texOffs(0, 49).addBox(-3.5F, 8.0F, -3.5F, 7.0F, 3.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition strapLDefinition = bodyDefinition.addOrReplaceChild("strapL",
          CubeListBuilder.create()
            .texOffs(10, 36).addBox(2.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition strapRDefinition = bodyDefinition.addOrReplaceChild("strapR",
          CubeListBuilder.create()
            .texOffs(0, 36).addBox(-3.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0698F, 0.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
