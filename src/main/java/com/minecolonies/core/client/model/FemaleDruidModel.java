// Made with Blockbench 4.1.5
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.workers.guard.EntityAIDruid.RENDER_META_POTION;

public class FemaleDruidModel extends CitizenModel<AbstractEntityCitizen>
{
	public FemaleDruidModel(ModelPart root)
	{
        super(root);
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

        PartDefinition Braid = bipedHead.addOrReplaceChild("Braid", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hair1 = Braid.addOrReplaceChild("hair1", CubeListBuilder.create().texOffs(74, 1).mirror().addBox(-0.1F, -1.0F, 5.1F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.9333F, 0.0F));

        PartDefinition hair2 = Braid.addOrReplaceChild("hair2", CubeListBuilder.create().texOffs(78, 0).mirror().addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.9333F, 0.0F));

        PartDefinition hair3 = Braid.addOrReplaceChild("hair3", CubeListBuilder.create().texOffs(76, 2).mirror().addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 1.3384F, 0.0F));

        PartDefinition hair4 = Braid.addOrReplaceChild("hair4", CubeListBuilder.create().texOffs(86, 0).mirror().addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.4833F, 0.0F));

        PartDefinition Horns = bipedHead.addOrReplaceChild("Horns", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition righthorn_r1 = Horns.addOrReplaceChild("righthorn_r1", CubeListBuilder.create().texOffs(80, 24).addBox(-0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -6.0F, -3.0F, 0.0F, 0.48F, 0.0F));

        PartDefinition lefthorn_r1 = Horns.addOrReplaceChild("lefthorn_r1", CubeListBuilder.create().texOffs(80, 10).addBox(0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -6.0F, -3.0F, 0.0F, -0.48F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition potionSatchet = bipedBody.addOrReplaceChild("potionSatchet", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition potions = potionSatchet.addOrReplaceChild("potions", CubeListBuilder.create(), PartPose.offset(0.0F, -14.2F, -4.0F));

        PartDefinition Potion1 = potions.addOrReplaceChild("Potion1", CubeListBuilder.create().texOffs(96, 7).addBox(-1.9152F, 1.4965F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.4F))
          .texOffs(112, 14).addBox(-1.4152F, 2.2965F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.3F))
          .texOffs(116, 10).addBox(-0.4152F, 1.6465F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
          .texOffs(97, 18).addBox(-1.9152F, 3.1465F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.3F)), PartPose.offset(1.8F, -5.22F, -0.2F));

        PartDefinition Potion2 = potions.addOrReplaceChild("Potion2", CubeListBuilder.create().texOffs(97, 26).addBox(-2.1F, 2.42F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.4F))
          .texOffs(112, 33).addBox(-1.6F, 3.22F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.3F))
          .texOffs(116, 29).addBox(-0.6F, 2.57F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
          .texOffs(97, 37).addBox(-2.1F, 4.07F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.3F)), PartPose.offset(-0.467F, -6.1666F, -0.2F));

        PartDefinition Potion3 = potions.addOrReplaceChild("Potion3", CubeListBuilder.create().texOffs(96, 45).addBox(-2.4653F, -0.8009F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-1.4F))
          .texOffs(112, 52).addBox(-1.9653F, -0.0009F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-1.3F))
          .texOffs(116, 48).addBox(-0.9653F, -0.6509F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
          .texOffs(97, 56).addBox(-2.4653F, 0.8491F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-1.3F)), PartPose.offset(-2.5233F, -2.9508F, -0.2F));

        PartDefinition satchet = potionSatchet.addOrReplaceChild("satchet", CubeListBuilder.create().texOffs(60, 12).addBox(-4.5F, 0.8F, -2.2F, 9.0F, 1.0F, 4.0F, new CubeDeformation(-0.3F))
          .texOffs(59, 28).addBox(-4.5F, -3.6F, 0.8F, 9.0F, 5.0F, 1.0F, new CubeDeformation(-0.31F))
          .texOffs(60, 24).addBox(-4.0F, -1.5F, -2.09F, 8.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(70, 17).addBox(3.5F, -1.6F, -2.2F, 1.0F, 3.0F, 4.0F, new CubeDeformation(-0.3F))
          .texOffs(60, 17).addBox(-4.5F, -1.6F, -2.2F, 1.0F, 3.0F, 4.0F, new CubeDeformation(-0.3F)), PartPose.offset(0.0F, -14.0F, -4.0F));

        PartDefinition lid = satchet.addOrReplaceChild("lid", CubeListBuilder.create(), PartPose.offset(0.0F, 1.3F, -0.2F));

        PartDefinition locket_r1 = lid.addOrReplaceChild("locket_r1", CubeListBuilder.create().texOffs(66, 18).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -5.2F, -0.6F, -0.3142F, 0.0F, 0.0F));

        PartDefinition lid_r1 = lid.addOrReplaceChild("lid_r1", CubeListBuilder.create().texOffs(59, 34).addBox(-4.5F, -2.5F, -0.5F, 9.0F, 3.0F, 1.0F, new CubeDeformation(-0.311F)), PartPose.offsetAndRotation(0.0F, -4.5F, 1.4F, 1.0472F, 0.0F, 0.0F));

        PartDefinition straps = potionSatchet.addOrReplaceChild("straps", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightStrap_r1 = straps.addOrReplaceChild("rightStrap_r1", CubeListBuilder.create().texOffs(58, 16).addBox(0.0F, -9.0F, -0.5F, 0.0F, 10.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-4.3F, -14.7F, -4.1F, -0.2094F, 0.0F, 0.0F));

        PartDefinition leftStrap_r1 = straps.addOrReplaceChild("leftStrap_r1", CubeListBuilder.create().texOffs(56, 16).addBox(0.0F, -9.0F, -0.5F, 0.0F, 10.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(4.3F, -14.7F, -4.1F, -0.2007F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

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
        body.getChild("potionSatchet").visible = entity.getRenderMetadata().contains(RENDER_META_POTION) && isWorking(entity);
    }
}
