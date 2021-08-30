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

public class ModelEntityFemaleCitizen extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityFemaleCitizen(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(46, 17).addBox(-4.0F, 0.4F, 2.1F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hatPieceDefinition = headDefinition.addOrReplaceChild("hatPiece",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(12, 17).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 3.0F)
          , PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition breastDefinition = bodyDefinition.addOrReplaceChild("breast",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-3.0F, 2.0F, -4.5F, 8.0F, 4.0F, 3.0F)
          , PartPose.offsetAndRotation(-1.0F, 3.0F, 1.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition dressPart1Definition = bodyDefinition.addOrReplaceChild("dressPart1",
          CubeListBuilder.create()
            .texOffs(26, 46).addBox(-5.0F, 2.0F, -7.0F, 10.0F, 9.0F, 9.0F)
          , PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition dressPart2Definition = bodyDefinition.addOrReplaceChild("dressPart2",
          CubeListBuilder.create()
            .texOffs(28, 38).addBox(-5.0F, 1.0F, -6.0F, 10.0F, 1.0F, 7.0F)
          , PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition dressPart3Definition = bodyDefinition.addOrReplaceChild("dressPart3",
          CubeListBuilder.create()
            .texOffs(32, 32).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 1.0F, 5.0F)
          , PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(34, 17).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F).mirror()
          , PartPose.offset(5.0F, 0.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(34, 17).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F)
          , PartPose.offset(-5.0F, 0.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F)
          , PartPose.offset(-1.0F, 12.0F, 1.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
