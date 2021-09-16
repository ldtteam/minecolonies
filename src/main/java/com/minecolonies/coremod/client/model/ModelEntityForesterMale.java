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

public class ModelEntityForesterMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityForesterMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head =
          partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
              .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body =
          partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LogPack = body.addOrReplaceChild("LogPack", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Logs = LogPack.addOrReplaceChild("Logs",
          CubeListBuilder.create().texOffs(89, 0).addBox(-5.0F, 8.0F, 3.0F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
          PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LogMiddle_r1 = Logs.addOrReplaceChild("LogMiddle_r1",
          CubeListBuilder.create().texOffs(92, 6).addBox(-5.0F, 6.0F, -1.0F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
          PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5585F, 0.0F, 0.0F));

        PartDefinition LogTop_r1 = Logs.addOrReplaceChild("LogTop_r1",
          CubeListBuilder.create().texOffs(92, 12).addBox(-3.0F, -2.0F, 3.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
          PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5061F));

        PartDefinition Basket = LogPack.addOrReplaceChild("Basket",
          CubeListBuilder.create().texOffs(65, 0).addBox(-3.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(69, 0).addBox(2.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(73, 0).addBox(-3.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(77, 0).addBox(2.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(81, 0).addBox(2.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(81, 4).addBox(-3.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(73, 9).addBox(-2.0F, 4.0F, 6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(73, 11).addBox(-2.0F, 11.0F, 6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(73, 13).addBox(-2.0F, 11.0F, 2.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
          PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketE2_r1 =
          Basket.addOrReplaceChild("BasketE2_r1",
            CubeListBuilder.create().texOffs(87, 8).addBox(-3.0F, 2.8F, 1.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.001F))
              .texOffs(83, 8).addBox(2.0F, 2.8F, 1.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.001F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4136F, 0.0F, 0.0F));

        PartDefinition Basket8_r1 =
          Basket.addOrReplaceChild("Basket8_r1",
            CubeListBuilder.create().texOffs(69, 13).addBox(-3.0F, 1.8F, 0.95F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F))
              .texOffs(65, 13).addBox(2.0F, 1.8F, 0.95F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8081F, 0.0F, 0.0F));

        PartDefinition right_arm =
          partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition left_arm =
          partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
            PartPose.offset(5.0F, 2.0F, 0.0F));

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
}
