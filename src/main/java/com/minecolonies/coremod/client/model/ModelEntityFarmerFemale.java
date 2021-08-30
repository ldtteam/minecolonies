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

public class ModelEntityFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFarmerFemale(final ModelPart part)
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

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(17, 32).addBox(-3.5F, 2.7F, -0.6F, 7.0F, 3.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.0F, 0.0F));

        PartDefinition boxBottomDefinition = bodyDefinition.addOrReplaceChild("boxBottom",
          CubeListBuilder.create()
            .texOffs(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition boxFrontDefinition = bodyDefinition.addOrReplaceChild("boxFront",
          CubeListBuilder.create()
            .texOffs(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, -4.0F));

        PartDefinition boxBackDefinition = bodyDefinition.addOrReplaceChild("boxBack",
          CubeListBuilder.create()
            .texOffs(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 9.0F, 0.0F));

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
            .texOffs(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(3.0F, 4.0F, -4.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition strapRightDefinition = bodyDefinition.addOrReplaceChild("strapRight",
          CubeListBuilder.create()
            .texOffs(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F).mirror()
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

        PartDefinition hatFrillDefinition = headDefinition.addOrReplaceChild("hatFrill",
          CubeListBuilder.create()
            .texOffs(57, 21).addBox(-5.5F, -5.7F, -8.0F, 11.0F, 1.0F, 10.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6981F, 0.0F, 0.0F));

        PartDefinition hatBottomDefinition = headDefinition.addOrReplaceChild("hatBottom",
          CubeListBuilder.create()
            .texOffs(61, 9).addBox(-5.0F, -7.8F, -7.0F, 10.0F, 3.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition hatTopDefinition = headDefinition.addOrReplaceChild("hatTop",
          CubeListBuilder.create()
            .texOffs(64, 1).addBox(-4.5F, -8.5F, -6.0F, 9.0F, 1.0F, 6.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition hatStrapDefinition = headDefinition.addOrReplaceChild("hatStrap",
          CubeListBuilder.create()
            .texOffs(68, 33).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition ponytailBaseDefinition = headDefinition.addOrReplaceChild("ponytailBase",
          CubeListBuilder.create()
            .texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ponytailTailDefinition = headDefinition.addOrReplaceChild("ponytailTail",
          CubeListBuilder.create()
            .texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
