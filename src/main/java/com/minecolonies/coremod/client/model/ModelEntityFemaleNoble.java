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
public class ModelEntityFemaleNoble extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFemaleNoble(final ModelPart part)
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

		PartDefinition Hair = head.addOrReplaceChild("Hair", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition Ponytail = head.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

		PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

		PartDefinition Hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(80, 8).addBox(-5.0F, -1.5F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.1F))
		.texOffs(88, 0).addBox(-3.0F, -2.8F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -7.1F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition breast = body.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.249F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition NobleDress = body.addOrReplaceChild("NobleDress", CubeListBuilder.create().texOffs(0, 112).addBox(-8.0F, -4.0F, -6.0F, 16.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(4, 98).addBox(-7.0F, -7.0F, -5.0F, 14.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(8, 87).addBox(-6.0F, -9.0F, -4.0F, 12.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(12, 78).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(16, 71).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 24.0F, -0.5F));

		PartDefinition handbag = body.addOrReplaceChild("handbag", CubeListBuilder.create().texOffs(57, 9).addBox(4.0F, -18.4F, -3.4F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(68, 15).addBox(4.0F, -24.3F, -2.3F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1F, 24.0F, -0.2F));

		PartDefinition handback_r1 = handbag.addOrReplaceChild("handback_r1", CubeListBuilder.create().texOffs(68, 10).addBox(-0.5F, -5.97F, -0.02F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -18.4F, 3.6F, 0.1484F, 0.0F, 0.0F));

		PartDefinition handfront_r1 = handbag.addOrReplaceChild("handfront_r1", CubeListBuilder.create().texOffs(66, 10).addBox(-0.5F, -6.4F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -18.0F, -3.4F, -0.1745F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(5.4F, 2.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
}
