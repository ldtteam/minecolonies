// Made with Blockbench 4.0.0-beta.0
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelEntityCourierFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityCourierFemale(ModelPart root)
    {
        super(root);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head =
          partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
              .texOffs(32, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offsetAndRotation(0.0F, 1.0F, -5.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition Ponytail = head.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.3F, 8.05F, 0.8727F, 0.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1",
          CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 1.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false),
          PartPose.offsetAndRotation(-0.5F, -2.0F, 0.65F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1",
          CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 1.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
          PartPose.offsetAndRotation(-1.0F, -5.0F, -2.15F, 0.5577F, 0.0F, 0.0F));

        PartDefinition body =
          partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create()
              .texOffs(16, 16).addBox(-4.0F, 1.0F, -2.1026F, 8.0F, 11.5F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(16, 32).addBox(-4.0F, 1.0F, -2.1026F, 8.0F, 11.5F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition breast =
          body.addOrReplaceChild("breast",
            CubeListBuilder.create()
              .texOffs(64, 49).addBox(-3.0F, 2.2938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
              .texOffs(64, 55).addBox(-3.0F, 2.2938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)),
            PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition backpack = body.addOrReplaceChild("backpack",
          CubeListBuilder.create().texOffs(100, 49).addBox(-4.0F, -8.6F, 1.9F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.3F)),
          PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition lid_r1 = backpack.addOrReplaceChild("lid_r1",
          CubeListBuilder.create().texOffs(100, 41).addBox(-4.0F, -1.2F, -0.5F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.4F)),
          PartPose.offsetAndRotation(0.0F, -9.0F, 2.5F, 0.0436F, 0.0F, 0.0F));

        PartDefinition right_arm =
          partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -0.5F, -1.7F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(40, 32).addBox(-2.0F, -0.5F, -1.7F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(-5.0F, 2.0F, -4.0F));

        PartDefinition left_arm =
          partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -0.5F, -1.7F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(48, 48).addBox(-1.0F, -0.5F, -1.7F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(5.0F, 2.0F, -4.0F));

        PartDefinition right_leg =
          partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition left_leg =
          partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public float getActualRotation()
    {
        return 0.34907F;
    }
}
