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

public class ModelEntityCrafterFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCrafterFemale(final ModelPart part)
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
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headdetailDefinition = headDefinition.addOrReplaceChild("headdetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition strapDefinition = headDefinition.addOrReplaceChild("strap",
          CubeListBuilder.create()
            .texOffs(0, 85).addBox(-1.2F, -7.5F, -4.2F, 1.0F, 2.0F, 9.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition backDefinition = headDefinition.addOrReplaceChild("back",
          CubeListBuilder.create()
            .texOffs(12, 96).addBox(0.5F, -5.5F, -4.4F, 3.0F, 2.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lens1Definition = headDefinition.addOrReplaceChild("lens1",
          CubeListBuilder.create()
            .texOffs(0, 96).addBox(1.0F, -5.0F, -4.6F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lens2Definition = headDefinition.addOrReplaceChild("lens2",
          CubeListBuilder.create()
            .texOffs(0, 98).addBox(1.25F, -5.0F, -5.0F, 1.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack1Definition = headDefinition.addOrReplaceChild("HairBack1",
          CubeListBuilder.create()
            .texOffs(0, 80).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 1.0F, 1.0F).mirror()
            .texOffs(0, 51).addBox(-2.5F, -2.5F, 1.5F, 7.0F, 1.0F, 3.0F).mirror()
            .texOffs(0, 55).addBox(-0.5F, -1.5F, 1.55F, 5.0F, 1.0F, 3.0F).mirror()
            .texOffs(16, 55).addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F).mirror()
            .texOffs(0, 59).addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F).mirror()
            .texOffs(12, 59).addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.9333F, 0.0F));

        PartDefinition HairBack2Definition = headDefinition.addOrReplaceChild("HairBack2",
          CubeListBuilder.create()
            .texOffs(5, 80).addBox(3.5F, -7.1F, -0.55F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));

        PartDefinition HairBack3Definition = headDefinition.addOrReplaceChild("HairBack3",
          CubeListBuilder.create()
            .texOffs(0, 69).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack4Definition = headDefinition.addOrReplaceChild("HairBack4",
          CubeListBuilder.create()
            .texOffs(12, 75).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack5Definition = headDefinition.addOrReplaceChild("HairBack5",
          CubeListBuilder.create()
            .texOffs(0, 75).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack6Definition = headDefinition.addOrReplaceChild("HairBack6",
          CubeListBuilder.create()
            .texOffs(0, 48).addBox(-0.1F, -1.0F, 5.1F, 1.0F, 2.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.9333F, 0.0F));

        PartDefinition HairBack7Definition = headDefinition.addOrReplaceChild("HairBack7",
          CubeListBuilder.create()
            .texOffs(0, 62).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack8Definition = headDefinition.addOrReplaceChild("HairBack8",
          CubeListBuilder.create()
            .texOffs(4, 48).addBox(0.5F, -0.5F, 2.6F, 4.0F, 1.0F, 2.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack9Definition = headDefinition.addOrReplaceChild("HairBack9",
          CubeListBuilder.create()
            .texOffs(0, 39).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 6.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack10Definition = headDefinition.addOrReplaceChild("HairBack10",
          CubeListBuilder.create()
            .texOffs(0, 51).addBox(-2.5F, -2.5F, 1.5F, 7.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack11Definition = headDefinition.addOrReplaceChild("HairBack11",
          CubeListBuilder.create()
            .texOffs(0, 55).addBox(-0.5F, -1.5F, 1.55F, 5.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairBack12Definition = headDefinition.addOrReplaceChild("HairBack12",
          CubeListBuilder.create()
            .texOffs(16, 55).addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.4833F, 0.0F));

        PartDefinition HairBack13Definition = headDefinition.addOrReplaceChild("HairBack13",
          CubeListBuilder.create()
            .texOffs(0, 59).addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.3384F, 0.0F));

        PartDefinition HairBack14Definition = headDefinition.addOrReplaceChild("HairBack14",
          CubeListBuilder.create()
            .texOffs(12, 59).addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.9333F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition skirtBaDefinition = bodyDefinition.addOrReplaceChild("skirtBa",
          CubeListBuilder.create()
            .texOffs(47, 41).addBox(-4.5F, 11.1F, -4.5F, 9.0F, 6.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition skirtFDefinition = bodyDefinition.addOrReplaceChild("skirtF",
          CubeListBuilder.create()
            .texOffs(25, 41).addBox(-4.5F, 11.1F, 2.5F, 9.0F, 6.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4712F, 0.0F, 0.0F));

        PartDefinition skirtRDefinition = bodyDefinition.addOrReplaceChild("skirtR",
          CubeListBuilder.create()
            .texOffs(53, 49).addBox(-17.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.3963F));

        PartDefinition skirtBo1Definition = bodyDefinition.addOrReplaceChild("skirtBo1",
          CubeListBuilder.create()
            .texOffs(29, 56).addBox(-4.5F, 15.4F, -4.0F, 9.0F, 1.0F, 8.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition skirtT1Definition = bodyDefinition.addOrReplaceChild("skirtT1",
          CubeListBuilder.create()
            .texOffs(25, 33).addBox(-4.5F, 11.0F, -3.0F, 9.0F, 2.0F, 6.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition skirtLDefinition = bodyDefinition.addOrReplaceChild("skirtL",
          CubeListBuilder.create()
            .texOffs(29, 49).addBox(11.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.3963F));

        PartDefinition skirtBo2Definition = bodyDefinition.addOrReplaceChild("skirtBo2",
          CubeListBuilder.create()
            .texOffs(29, 65).addBox(-4.5F, 16.4F, -5.5F, 9.0F, 1.0F, 11.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(0, 32).addBox(-3.5F, -0.5F, -5.5F, 7.0F, 3.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.9341F, 0.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition coreDefinition = rightArmDefinition.addOrReplaceChild("core",
          CubeListBuilder.create()
            .texOffs(38, 93).addBox(1.75F, 7.0F, 1.5F, 2.0F, 2.0F, 2.0F).mirror()
          , PartPose.offsetAndRotation(-4.6F, 2.0F, 0.0F, 0.0F, 0.75F, 0.0F));

        PartDefinition frontBeamDefinition = rightArmDefinition.addOrReplaceChild("frontBeam",
          CubeListBuilder.create()
            .texOffs(25, 78).addBox(3.7F, -4.56F, -2.5F, 1.0F, 13.0F, 5.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition SideBeamDefinition = rightArmDefinition.addOrReplaceChild("SideBeam",
          CubeListBuilder.create()
            .texOffs(38, 78).addBox(2.5F, -4.5F, -0.5F, 4.0F, 13.0F, 1.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
