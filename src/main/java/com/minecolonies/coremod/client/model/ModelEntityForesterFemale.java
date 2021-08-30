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

public class ModelEntityForesterFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityForesterFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;

        IMinecoloniesAPI.getInstance().getModelTypeRegistry().register(BipedModelType.FORESTER, true, this);
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponytailBaseDefinition = headDefinition.addOrReplaceChild("ponytailBase",
          CubeListBuilder.create()
            .texOffs(58, 25).addBox(-0.5F, 3.2F, 3.8F, 1.0F, 5.0F, 1.0F)
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5585F, 0.0F, 0.0F));

        PartDefinition ponytailEndDefinition = ponytailBaseDefinition.addOrReplaceChild("ponytailEnd",
          CubeListBuilder.create()
            .texOffs(57, 19).addBox(-1.0F, -2.0F, 4.2F, 2.0F, 5.0F, 1.0F)
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2296F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(17, 33).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 4.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5934F, 0.0F, 0.0F));

        PartDefinition logBottomDefinition = bodyDefinition.addOrReplaceChild("logBottom",
          CubeListBuilder.create()
            .texOffs(17, 58).addBox(-5.3F, 8.5F, 2.5F, 10.0F, 3.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition logMiddleDefinition = bodyDefinition.addOrReplaceChild("logMiddle",
          CubeListBuilder.create()
            .texOffs(17, 51).addBox(-1.3F, 6.7F, -1.0F, 5.0F, 3.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6458F, 0.2967F, 0.0F));

        PartDefinition logTopDefinition = bodyDefinition.addOrReplaceChild("logTop",
          CubeListBuilder.create()
            .texOffs(17, 41).addBox(-4.2F, 2.0F, 0.7F, 3.0F, 7.0F, 3.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.2094F));

        PartDefinition BasketBLDefinition = bodyDefinition.addOrReplaceChild("BasketBL",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(2.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketTBDefinition = bodyDefinition.addOrReplaceChild("BasketTB",
          CubeListBuilder.create()
            .texOffs(0, 38).addBox(-2.0F, 4.0F, 6.0F, 4.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketBRDefinition = bodyDefinition.addOrReplaceChild("BasketBR",
          CubeListBuilder.create()
            .texOffs(0, 33).addBox(-3.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketTMLDefinition = bodyDefinition.addOrReplaceChild("BasketTML",
          CubeListBuilder.create()
            .texOffs(11, 33).addBox(3.1F, 1.4F, 0.6F, 1.0F, 6.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8081F, -0.1745F, 0.0F));

        PartDefinition BasketBFDefinition = bodyDefinition.addOrReplaceChild("BasketBF",
          CubeListBuilder.create()
            .texOffs(0, 38).addBox(-2.0F, 11.0F, 2.0F, 4.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketMFRDefinition = bodyDefinition.addOrReplaceChild("BasketMFR",
          CubeListBuilder.create()
            .texOffs(11, 41).addBox(-3.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketMFLDefinition = bodyDefinition.addOrReplaceChild("BasketMFL",
          CubeListBuilder.create()
            .texOffs(11, 41).addBox(2.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketMBLDefinition = bodyDefinition.addOrReplaceChild("BasketMBL",
          CubeListBuilder.create()
            .texOffs(6, 41).addBox(2.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketMBRDefinition = bodyDefinition.addOrReplaceChild("BasketMBR",
          CubeListBuilder.create()
            .texOffs(6, 41).addBox(-3.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BasketTMRDefinition = bodyDefinition.addOrReplaceChild("BasketTMR",
          CubeListBuilder.create()
            .texOffs(11, 33).addBox(-4.1F, 1.4F, 0.5F, 1.0F, 6.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8081F, 0.1745F, 0.0F));

        PartDefinition BasketBBDefinition = bodyDefinition.addOrReplaceChild("BasketBB",
          CubeListBuilder.create()
            .texOffs(0, 38).addBox(-2.0F, 11.0F, 6.0F, 4.0F, 1.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
