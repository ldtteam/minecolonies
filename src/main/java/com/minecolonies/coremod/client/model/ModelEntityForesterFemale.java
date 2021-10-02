// Made with Blockbench 4.0.0-beta.0
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelEntityForesterFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityForesterFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ponytailBase = head.addOrReplaceChild("ponytailBase", CubeListBuilder.create().texOffs(87, 55).addBox(-0.5F, 3.2F, 3.8F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5585F, 0.0F, 0.0F));

		PartDefinition ponytailEnd = ponytailBase.addOrReplaceChild("ponytailEnd", CubeListBuilder.create().texOffs(86, 49).addBox(-1.0F, -2.0F, 4.2F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2296F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition breast = body.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition LogPack = body.addOrReplaceChild("LogPack", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Logs = LogPack.addOrReplaceChild("Logs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition logMiddle_r1 = Logs.addOrReplaceChild("logMiddle_r1", CubeListBuilder.create().texOffs(92, 6).mirror().addBox(-1.3F, 6.7F, -1.0F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6458F, 0.2967F, 0.0F));

		PartDefinition logBottom_r1 = Logs.addOrReplaceChild("logBottom_r1", CubeListBuilder.create().texOffs(89, 0).mirror().addBox(-5.3F, 8.5F, 2.5F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

		PartDefinition logTop_r1 = Logs.addOrReplaceChild("logTop_r1", CubeListBuilder.create().texOffs(92, 12).mirror().addBox(-4.2F, 2.0F, 0.7F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.2094F));

		PartDefinition Basket = LogPack.addOrReplaceChild("Basket", CubeListBuilder.create().texOffs(81, 0).mirror().addBox(2.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(73, 9).mirror().addBox(-2.0F, 4.0F, 6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(81, 4).mirror().addBox(-3.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(73, 11).mirror().addBox(-2.0F, 11.0F, 2.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(65, 0).mirror().addBox(-3.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(69, 0).mirror().addBox(2.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(73, 0).mirror().addBox(2.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(77, 0).mirror().addBox(-3.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(73, 13).mirror().addBox(-2.0F, 11.0F, 6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BasketTMR_r1 = Basket.addOrReplaceChild("BasketTMR_r1", CubeListBuilder.create().texOffs(69, 13).mirror().addBox(-4.1F, 1.4F, 0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8081F, 0.1745F, 0.0F));

		PartDefinition BasketTML_r1 = Basket.addOrReplaceChild("BasketTML_r1", CubeListBuilder.create().texOffs(65, 13).mirror().addBox(3.1F, 1.4F, 0.6F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8081F, -0.1745F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}
}
