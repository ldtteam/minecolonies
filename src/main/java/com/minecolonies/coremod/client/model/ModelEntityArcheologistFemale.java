package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelEntityArcheologistFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityArcheologistFemale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition glasses = bipedHead.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(74, 0).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(74, 8).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(90, 0).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F))
          .texOffs(75, 16).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F))
          .texOffs(61, 16).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, -3.7F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition FancyHat = bipedHead.addOrReplaceChild("FancyHat", CubeListBuilder.create().texOffs(84, 10).addBox(-7.1F, -0.3625F, -5.0F, 12.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(104, 21).addBox(-5.1F, -0.3625F, -7.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(124, 27).addBox(-6.1F, -0.3625F, -6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(106, 27).addBox(4.9F, -2.3625F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(124, 31).addBox(4.9F, -1.3625F, 5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(124, 34).addBox(4.9F, -1.3625F, -6.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(124, 29).addBox(-6.1F, -0.3625F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(104, 24).addBox(-5.1F, -0.3625F, 5.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
          .texOffs(96, 0).addBox(-4.1F, -1.9525F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.6F)), PartPose.offsetAndRotation(0.1F, -7.1375F, 0.0F, 0.0873F, -0.0175F, -0.0436F));

        PartDefinition feather2_r1 = FancyHat.addOrReplaceChild("feather2_r1", CubeListBuilder.create().texOffs(88, 21).addBox(-0.7F, -5.9F, -0.8F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4F, -1.1125F, 1.318F, -0.1858F, -0.528F, -0.0543F));

        PartDefinition bipedBody = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bag = bipedBody.addOrReplaceChild("bag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition map_r1 = bag.addOrReplaceChild("map_r1", CubeListBuilder.create().texOffs(58, 29).addBox(-2.8F, -3.2F, -6.5F, 6.0F, 6.0F, 13.0F, new CubeDeformation(-2.0F)), PartPose.offsetAndRotation(-5.5F, 8.0F, 0.0F, 0.0F, 0.0F, 1.0472F));

        PartDefinition strapback_r1 = bag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(122, 49).addBox(-0.5F, -14.3F, 0.0F, 1.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));

        PartDefinition strapfrontb_r1 = bag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(123, 49).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4558F, -0.5558F, 1.1519F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition strapfrontb_r2 = bag.addOrReplaceChild("strapfrontb_r2", CubeListBuilder.create().texOffs(123, 54).addBox(-0.5F, -2.5F, 0.5F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6008F, 1.2992F, -3.0977F, -0.2269F, 0.0F, 0.7854F));

        PartDefinition strapfronta_r1 = bag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(126, 49).addBox(-0.5F, -8.0F, 0.0F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 9.4F, -4.1F, -0.0873F, 0.0F, 0.7854F));

        PartDefinition bag_r1 = bag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(100, 50).addBox(-1.9F, -3.0F, -4.0F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition bipedRightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.4F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-1.4F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.8F, 2.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition bipedLeftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
