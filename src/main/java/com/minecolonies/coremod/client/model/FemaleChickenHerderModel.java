// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;

public class FemaleChickenHerderModel extends CitizenModel<AbstractEntityCitizen>
{

    public FemaleChickenHerderModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition Hat = bipedHead.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(54, 14).addBox(-3.5F, -1.5F, -6.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.11F))
          .texOffs(78, 14).addBox(-2.0F, -2.45F, -3.5F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.5F))
          .texOffs(75, 13).addBox(-4.0F, 8.1311F, -3.0F, 8.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(70, 25).addBox(-2.5F, -3.0115F, -3.0F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.9F, 0.5F, -0.0611F, 0.0F, 0.0F));

        PartDefinition ribbonC_r1 = Hat.addOrReplaceChild("ribbonC_r1", CubeListBuilder.create().texOffs(58, 32).addBox(0.0F, 0.0F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -3.0F, -2.5F, 0.0F, 0.5236F, 0.0F));

        PartDefinition ribbonB_r1 = Hat.addOrReplaceChild("ribbonB_r1", CubeListBuilder.create().texOffs(58, 29).addBox(-2.0F, 0.0F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -3.0F, -2.5F, 0.0F, -0.5236F, 0.0F));

        PartDefinition ribbonA_r1 = Hat.addOrReplaceChild("ribbonA_r1", CubeListBuilder.create().texOffs(58, 26).addBox(-2.5F, -2.9F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.1F, -3.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition strapLeftTop_r1 = Hat.addOrReplaceChild("strapLeftTop_r1", CubeListBuilder.create().texOffs(76, 29).addBox(0.0F, -4.9575F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0709F, 0.4F, -2.5F, 0.0F, 0.0F, -0.8116F));

        PartDefinition strapRightTop_r1 = Hat.addOrReplaceChild("strapRightTop_r1", CubeListBuilder.create().texOffs(78, 29).addBox(0.0F, -4.9837F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0742F, 0.4F, -2.5F, 0.0F, 0.0F, 0.8116F));

        PartDefinition strapRight_r1 = Hat.addOrReplaceChild("strapRight_r1", CubeListBuilder.create().texOffs(82, 26).mirror().addBox(-0.05F, -0.1F, -0.5F, 0.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 0.5F, -2.5F, 0.0F, 0.0F, -0.2618F));

        PartDefinition strapLeft_r1 = Hat.addOrReplaceChild("strapLeft_r1", CubeListBuilder.create().texOffs(80, 26).addBox(0.15F, -0.1F, -0.5F, 0.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9F, 0.5F, -2.5F, 0.0F, 0.0F, 0.2635F));

        PartDefinition bottomRight_r1 = Hat.addOrReplaceChild("bottomRight_r1", CubeListBuilder.create().texOffs(74, 25).addBox(-0.5F, -2.0F, -5.5F, 3.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-4.2419F, 1.6799F, -0.5F, 0.0F, 0.0F, -0.6981F));

        PartDefinition bottomLeft_r1 = Hat.addOrReplaceChild("bottomLeft_r1", CubeListBuilder.create().texOffs(58, 26).addBox(-2.5F, -2.0F, -5.5F, 3.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.2419F, 1.6799F, -0.5F, 0.0F, 0.0F, 0.6981F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition seedsBag = bipedBody.addOrReplaceChild("seedsBag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition strapback_r1 = seedsBag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(122, 50).addBox(-0.5F, -14.3F, 0.0F, 1.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));

        PartDefinition strapfrontb_r1 = seedsBag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(123, 50).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4558F, -0.5558F, 1.1519F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition strapfrontb_r2 = seedsBag.addOrReplaceChild("strapfrontb_r2", CubeListBuilder.create().texOffs(123, 55).addBox(-0.5F, -2.5F, 0.5F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6008F, 1.2992F, -3.0977F, -0.2269F, 0.0F, 0.7854F));

        PartDefinition strapfronta_r1 = seedsBag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(126, 50).addBox(-0.5F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 9.4F, -4.1F, -0.0873F, 0.0F, 0.7854F));

        PartDefinition bag_r1 = seedsBag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(108, 37).addBox(-0.9F, -3.1F, -4.0F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        body.getChild("seedsBag").visible = isWorking(entity);
        head.getChild("Hat").visible = displayHat(entity);
    }
}
