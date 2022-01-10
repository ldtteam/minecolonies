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

public class ModelEntityChildMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityChildMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition overRightLegDefinition = rightLegDefinition.addOrReplaceChild("overRightLeg",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F)
          , PartPose.offset(-4.5F, 12.0F, -2.5F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition overLeftLegDefinition = leftLegDefinition.addOrReplaceChild("overLeftLeg",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F).mirror()
          , PartPose.offset(-0.5F, 12.0F, -2.5F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 12.0F, 5.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition pouchDefinition = bodyDefinition.addOrReplaceChild("pouch",
          CubeListBuilder.create()
            .texOffs(20, 33).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(-4.0F, 9.5F, -3.5F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(44, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(44, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
