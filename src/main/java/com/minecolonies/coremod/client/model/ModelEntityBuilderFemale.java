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

public class ModelEntityBuilderFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityBuilderFemale(final ModelPart part)
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
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
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

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(17, 32).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 3.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.0F, 0.0F));

        PartDefinition beltDefinition = bodyDefinition.addOrReplaceChild("belt",
          CubeListBuilder.create()
            .texOffs(0, 40).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 1.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rulerDefinition = bodyDefinition.addOrReplaceChild("ruler",
          CubeListBuilder.create()
            .texOffs(17, 47).addBox(2.0F, 7.3F, -2.2F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hammerHandleDefinition = bodyDefinition.addOrReplaceChild("hammerHandle",
          CubeListBuilder.create()
            .texOffs(2, 49).addBox(1.0F, 7.3F, -2.4F, 1.0F, 4.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3142F));

        PartDefinition hammerHeadDefinition = hammerHandleDefinition.addOrReplaceChild("hammerHead",
          CubeListBuilder.create()
            .texOffs(0, 47).addBox(0.0F, 7.5F, -2.5F, 3.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hatBaseDefinition = headDefinition.addOrReplaceChild("hatBase",
          CubeListBuilder.create()
            .texOffs(57, 19).addBox(-4.0F, -9.7F, -4.0F, 8.0F, 2.0F, 7.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

        PartDefinition hatBottomMiddleDefinition = hatBaseDefinition.addOrReplaceChild("hatBottomMiddle",
          CubeListBuilder.create()
            .texOffs(57, 8).addBox(-3.0F, -10.0F, -5.0F, 6.0F, 2.0F, 9.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatBackDefinition = hatBottomMiddleDefinition.addOrReplaceChild("hatBack",
          CubeListBuilder.create()
            .texOffs(64, 31).addBox(-3.5F, -8.0F, 4.0F, 7.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatFrontDefinition = hatBottomMiddleDefinition.addOrReplaceChild("hatFront",
          CubeListBuilder.create()
            .texOffs(66, 28).addBox(-2.5F, -9.0F, -6.0F, 5.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatTopMiddleDefinition = hatBottomMiddleDefinition.addOrReplaceChild("hatTopMiddle",
          CubeListBuilder.create()
            .texOffs(61, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 1.0F, 7.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatBrimBaseDefinition = hatBaseDefinition.addOrReplaceChild("hatBrimBase",
          CubeListBuilder.create()
            .texOffs(53, 33).addBox(-4.5F, -8.0F, -6.0F, 9.0F, 1.0F, 10.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatBrimFrontDefinition = hatBrimBaseDefinition.addOrReplaceChild("hatBrimFront",
          CubeListBuilder.create()
            .texOffs(64, 44).addBox(-3.5F, -8.0F, -7.0F, 7.0F, 1.0F, 1.0F).mirror()
            .texOffs(66, 46).addBox(-2.5F, -8.0F, -8.0F, 5.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatBrimFrontTipDefinition = hatBrimFrontDefinition.addOrReplaceChild("hatBrimFrontTip",
          CubeListBuilder.create()
            .texOffs(66, 46).addBox(-2.5F, -8.0F, -8.0F, 5.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ponytailBaseDefinition = headDefinition.addOrReplaceChild("ponytailBase",
          CubeListBuilder.create()
            .texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ponytailTailDefinition = ponytailBaseDefinition.addOrReplaceChild("ponytailTail",
          CubeListBuilder.create()
            .texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1222F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
