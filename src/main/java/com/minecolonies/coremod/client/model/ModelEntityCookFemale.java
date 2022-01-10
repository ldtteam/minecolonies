// Made with Blockbench 3.6.5
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

public class ModelEntityCookFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCookFemale(final ModelPart part)
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

        PartDefinition skirtDefinition = bodyDefinition.addOrReplaceChild("skirt",
          CubeListBuilder.create()
          , PartPose.offset(-4.0F, 12.0F, -4.0F));

        PartDefinition dress1Definition = skirtDefinition.addOrReplaceChild("dress1",
          CubeListBuilder.create()
            .texOffs(0, 49).addBox(0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition dress2Definition = skirtDefinition.addOrReplaceChild("dress2",
          CubeListBuilder.create()
            .texOffs(0, 56).addBox(0.0F, 0.0F, 0.0F, 10.0F, 4.0F, 8.0F).mirror()
          , PartPose.offset(-1.0F, 1.0F, 0.0F));

        PartDefinition dress3Definition = skirtDefinition.addOrReplaceChild("dress3",
          CubeListBuilder.create()
            .texOffs(0, 68).addBox(0.0F, 0.0F, 0.0F, 12.0F, 3.0F, 10.0F).mirror()
          , PartPose.offset(-2.0F, 5.0F, -1.0F));

        PartDefinition bipedChestDefinition = bodyDefinition.addOrReplaceChild("bipedChest",
          CubeListBuilder.create()
            .texOffs(17, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(0, 39).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 8.0F).mirror()
          , PartPose.offsetAndRotation(-4.5F, -9.2F, 0.0F, -0.8551F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
