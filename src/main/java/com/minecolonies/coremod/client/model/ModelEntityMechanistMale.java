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

public class ModelEntityMechanistMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityMechanistMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;

        IMinecoloniesAPI.getInstance().getModelTypeRegistry().register(BipedModelType.MECHANIST, false, this);
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beltDefinition = bodyDefinition.addOrReplaceChild("belt",
          CubeListBuilder.create()
            .texOffs(0, 42).addBox(-0.5F, -14.0F, -4.0F, 4.0F, 3.0F, 2.0F).mirror()
            .texOffs(13, 45).addBox(1.0F, -13.45F, -4.2F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(0, 33).addBox(-4.5F, -13.0F, -3.0F, 9.0F, 2.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition torch1Definition = bodyDefinition.addOrReplaceChild("torch1",
          CubeListBuilder.create()
            .texOffs(0, 56).addBox(-0.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F)
            .texOffs(0, 62).addBox(-0.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F)
            .texOffs(1, 54).addBox(0.0F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F)
            .texOffs(1, 57).addBox(0.0F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition torch2Definition = bodyDefinition.addOrReplaceChild("torch2",
          CubeListBuilder.create()
            .texOffs(5, 62).addBox(1.0F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F)
            .texOffs(5, 56).addBox(1.0F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F)
            .texOffs(6, 57).addBox(1.25F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F)
            .texOffs(6, 54).addBox(1.25F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition torch3Definition = bodyDefinition.addOrReplaceChild("torch3",
          CubeListBuilder.create()
            .texOffs(11, 57).addBox(2.5F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F)
            .texOffs(10, 56).addBox(2.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F)
            .texOffs(10, 62).addBox(2.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F)
            .texOffs(11, 54).addBox(2.5F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F)
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition gloverightDefinition = rightArmDefinition.addOrReplaceChild("gloveright",
          CubeListBuilder.create()
            .texOffs(56, 16).addBox(-8.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
            .texOffs(96, 10).addBox(-9.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F).mirror()
          , PartPose.offset(5.0F, 22.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition gloveleftDefinition = leftArmDefinition.addOrReplaceChild("gloveleft",
          CubeListBuilder.create()
            .texOffs(96, 17).addBox(3.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F).mirror()
            .texOffs(76, 16).addBox(3.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F).mirror()
          , PartPose.offset(-5.0F, 22.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition maskDefinition = headDefinition.addOrReplaceChild("mask",
          CubeListBuilder.create()
            .texOffs(64, 8).addBox(3.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F).mirror()
            .texOffs(56, 0).addBox(-4.5F, -32.5F, -4.5F, 9.0F, 1.0F, 3.0F).mirror()
            .texOffs(76, 10).addBox(-4.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
            .texOffs(77, 1).addBox(-4.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
            .texOffs(82, 2).addBox(-4.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F).mirror()
            .texOffs(56, 4).addBox(-4.5F, -25.0F, -4.5F, 9.0F, 1.0F, 3.0F).mirror()
            .texOffs(24, 0).addBox(1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F).mirror()
            .texOffs(82, 10).addBox(-4.0F, -28.0F, -5.0F, 8.0F, 3.0F, 1.0F).mirror()
            .texOffs(72, 9).addBox(-1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F).mirror()
            .texOffs(77, 0).addBox(3.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(30, 0).addBox(-3.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F).mirror()
            .texOffs(94, 7).addBox(-4.0F, -32.0F, -5.0F, 8.0F, 2.0F, 1.0F).mirror()
            .texOffs(89, 1).addBox(3.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
            .texOffs(99, 1).addBox(3.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F).mirror()
            .texOffs(83, 0).addBox(-4.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
