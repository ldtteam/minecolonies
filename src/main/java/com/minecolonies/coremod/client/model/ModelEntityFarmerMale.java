// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityFarmerMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFarmerMale(final ModelPart part)
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
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition boxBottomDefinition = bodyDefinition.addOrReplaceChild("boxBottom",
          CubeListBuilder.create()
            .texOffs(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition boxBackDefinition = bodyDefinition.addOrReplaceChild("boxBack",
          CubeListBuilder.create()
            .texOffs(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition boxFrontDefinition = bodyDefinition.addOrReplaceChild("boxFront",
          CubeListBuilder.create()
            .texOffs(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition boxLeftDefinition = bodyDefinition.addOrReplaceChild("boxLeft",
          CubeListBuilder.create()
            .texOffs(42, 43).addBox(3.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition boxRightDefinition = bodyDefinition.addOrReplaceChild("boxRight",
          CubeListBuilder.create()
            .texOffs(0, 43).addBox(-4.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition seedsDefinition = bodyDefinition.addOrReplaceChild("seeds",
          CubeListBuilder.create()
            .texOffs(19, 45).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition strapLeftDefinition = bodyDefinition.addOrReplaceChild("strapLeft",
          CubeListBuilder.create()
            .texOffs(92, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(3.0F, 4.0F, -4.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition strapRightDefinition = bodyDefinition.addOrReplaceChild("strapRight",
          CubeListBuilder.create()
            .texOffs(110, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(-4.0F, 4.0F, -4.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hatStrapDefinition = headDefinition.addOrReplaceChild("hatStrap",
          CubeListBuilder.create()
            .texOffs(98, 14).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition hatBottomDefinition = headDefinition.addOrReplaceChild("hatBottom",
          CubeListBuilder.create()
            .texOffs(57, 11).addBox(-5.0F, -9.8F, -6.0F, 10.0F, 3.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2094F, 0.0F, 0.0F));

        PartDefinition hatTopDefinition = hatBottomDefinition.addOrReplaceChild("hatTop",
          CubeListBuilder.create()
            .texOffs(64, 2).addBox(-4.5F, -10.5F, -5.0F, 9.0F, 1.0F, 7.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrillBottomDefinition = hatBottomDefinition.addOrReplaceChild("hatFrillBottom",
          CubeListBuilder.create()
            .texOffs(57, 44).addBox(-7.5F, -6.7F, -8.5F, 15.0F, 1.0F, 14.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrillBackDefinition = hatFrillBottomDefinition.addOrReplaceChild("hatFrillBack",
          CubeListBuilder.create()
            .texOffs(87, 40).addBox(-6.5F, -7.7F, 4.5F, 13.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrillFrontDefinition = hatFrillBottomDefinition.addOrReplaceChild("hatFrillFront",
          CubeListBuilder.create()
            .texOffs(57, 40).addBox(-6.5F, -7.7F, -8.5F, 13.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrillLeftDefinition = hatFrillBottomDefinition.addOrReplaceChild("hatFrillLeft",
          CubeListBuilder.create()
            .texOffs(57, 24).addBox(6.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrillRightDefinition = hatFrillBottomDefinition.addOrReplaceChild("hatFrillRight",
          CubeListBuilder.create()
            .texOffs(88, 24).addBox(-7.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
