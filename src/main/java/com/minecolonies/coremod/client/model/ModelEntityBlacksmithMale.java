// Made with Blockbench 3.6.5
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

public class ModelEntityBlacksmithMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityBlacksmithMale(final ModelPart part)
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
            .texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F)
          , PartPose.offset(-5.0F, 6.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 6.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F)
          , PartPose.offset(-2.0F, 14.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 14.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F).mirror()
            .texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition headdetailDefinition = headDefinition.addOrReplaceChild("headdetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(0, 39).addBox(3.3F, -23.5F, -4.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(4, 40).addBox(-4.2F, -23.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(8, 40).addBox(-4.0F, -24.5F, -4.5F, 8.0F, 1.0F, 1.0F).mirror()
            .texOffs(19, 35).addBox(-3.5F, -25.15F, -3.75F, 7.0F, 2.0F, 7.0F).mirror()
            .texOffs(40, 39).addBox(-4.5F, -24.5F, -3.5F, 9.0F, 2.0F, 1.0F).mirror()
            .texOffs(40, 36).addBox(-4.5F, -24.5F, -2.5F, 9.0F, 2.0F, 1.0F).mirror()
            .texOffs(60, 38).addBox(-4.5F, -24.5F, -1.5F, 9.0F, 2.0F, 2.0F).mirror()
            .texOffs(82, 36).addBox(-4.5F, -24.5F, 0.5F, 9.0F, 4.0F, 2.0F).mirror()
            .texOffs(47, 42).addBox(-4.5F, -24.5F, 2.5F, 9.0F, 1.0F, 1.0F).mirror()
            .texOffs(67, 42).addBox(-4.5F, -22.5F, -0.5F, 9.0F, 1.0F, 1.0F).mirror()
            .texOffs(0, 44).addBox(-4.5F, -23.5F, 2.5F, 9.0F, 6.0F, 2.0F).mirror()
            .texOffs(87, 42).addBox(-4.5F, -17.5F, 3.5F, 9.0F, 1.0F, 1.0F).mirror()
            .texOffs(9, 38).addBox(-3.5F, -16.5F, 3.5F, 7.0F, 1.0F, 1.0F).mirror()
            .texOffs(20, 44).addBox(-3.5F, -15.5F, 3.5F, 2.0F, 1.0F, 1.0F).mirror()
            .texOffs(26, 44).addBox(-2.5F, -14.5F, 3.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(22, 46).addBox(0.5F, -15.5F, 3.5F, 3.0F, 1.0F, 1.0F).mirror()
            .texOffs(22, 48).addBox(1.5F, -14.5F, 3.5F, 1.0F, 2.0F, 1.0F).mirror()
            .texOffs(30, 44).addBox(-4.5F, -20.5F, -0.5F, 9.0F, 1.0F, 3.0F).mirror()
            .texOffs(43, 59).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardDefinition = headDefinition.addOrReplaceChild("beard",
          CubeListBuilder.create()
            .texOffs(0, 54).addBox(-0.5F, 7.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(4, 54).addBox(-2.5F, 3.5F, -4.5F, 5.0F, 1.0F, 1.0F).mirror()
            .texOffs(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F).mirror()
            .texOffs(0, 56).addBox(-1.5F, 4.5F, -5.0F, 3.0F, 1.0F, 2.0F).mirror()
            .texOffs(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F).mirror()
            .texOffs(16, 56).addBox(-4.5F, 1.5F, -4.5F, 9.0F, 1.0F, 2.0F).mirror()
            .texOffs(1, 60).addBox(-4.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F).mirror()
            .texOffs(13, 59).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 4.0F).mirror()
            .texOffs(32, 52).addBox(-3.5F, -1.5F, -4.5F, 7.0F, 1.0F, 3.0F).mirror()
            .texOffs(65, 59).addBox(1.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardBDefinition = beardDefinition.addOrReplaceChild("beardB",
          CubeListBuilder.create()
            .texOffs(0, 54).addBox(-0.5F, 7.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardMMBDefinition = beardDefinition.addOrReplaceChild("beardMMB",
          CubeListBuilder.create()
            .texOffs(4, 54).addBox(-2.5F, 3.5F, -4.5F, 5.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardMBBDefinition = beardDefinition.addOrReplaceChild("beardMBB",
          CubeListBuilder.create()
            .texOffs(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardring1Definition = beardDefinition.addOrReplaceChild("beardring1",
          CubeListBuilder.create()
            .texOffs(0, 56).addBox(-1.5F, 4.5F, -5.0F, 3.0F, 1.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition beardBBB2Definition = beardDefinition.addOrReplaceChild("beardBBB2",
          CubeListBuilder.create()
            .texOffs(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardMBDefinition = beardDefinition.addOrReplaceChild("beardMB",
          CubeListBuilder.create()
            .texOffs(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F).mirror()
            .texOffs(16, 56).addBox(-4.5F, 1.5F, -4.5F, 9.0F, 1.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition moustacheLBDefinition = beardDefinition.addOrReplaceChild("moustacheLB",
          CubeListBuilder.create()
            .texOffs(0, 59).addBox(2.7F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardRMDefinition = beardDefinition.addOrReplaceChild("beardRM",
          CubeListBuilder.create()
            .texOffs(1, 60).addBox(-4.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardTMDefinition = beardDefinition.addOrReplaceChild("beardTM",
          CubeListBuilder.create()
            .texOffs(13, 59).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardTTDefinition = beardDefinition.addOrReplaceChild("beardTT",
          CubeListBuilder.create()
            .texOffs(32, 52).addBox(-3.5F, -1.5F, -4.5F, 7.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition moustacheTMDefinition = beardDefinition.addOrReplaceChild("moustacheTM",
          CubeListBuilder.create()
            .texOffs(36, 56).addBox(-2.5F, -1.0F, -5.0F, 5.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition moustacheLMDefinition = beardDefinition.addOrReplaceChild("moustacheLM",
          CubeListBuilder.create()
            .texOffs(35, 59).addBox(2.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition moustacheRMDefinition = beardDefinition.addOrReplaceChild("moustacheRM",
          CubeListBuilder.create()
            .texOffs(39, 59).addBox(-3.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition moustacheRBDefinition = beardDefinition.addOrReplaceChild("moustacheRB",
          CubeListBuilder.create()
            .texOffs(43, 59).addBox(-3.7333F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hairMDefinition = beardDefinition.addOrReplaceChild("hairM",
          CubeListBuilder.create()
            .texOffs(43, 59).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beardLMDefinition = beardDefinition.addOrReplaceChild("beardLM",
          CubeListBuilder.create()
            .texOffs(65, 59).addBox(1.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
