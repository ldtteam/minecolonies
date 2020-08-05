// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityConcreteMixerMale extends CitizenModel<AbstractEntityCitizen> {
	ModelRenderer hair;
	ModelRenderer mask;
	ModelRenderer headDetail;

	public ModelEntityConcreteMixerMale() {
		textureWidth = 128;
		textureHeight = 64;

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

		hair = new ModelRenderer(this);
		hair.setRotationPoint(0.0F, 24.0F, 0.0F);
		bipedHead.addChild(hair);
		hair.setTextureOffset(61, 46).addBox(-4.35F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, true);
		hair.setTextureOffset(33, 46).addBox(-4.25F, -32.25F, 4.02F, 8.5F, 7.35F, 0.25F, 0.0F, true);
		hair.setTextureOffset(0, 46).addBox(-4.0F, -32.25F, -3.48F, 8.0F, 2.25F, 7.5F, 0.0F, true);
		hair.setTextureOffset(51, 58).addBox(-2.75F, -32.25F, -4.48F, 5.5F, 2.25F, 1.0F, 0.0F, true);
		hair.setTextureOffset(6, 59).addBox(-4.35F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, true);
		hair.setTextureOffset(0, 55).addBox(-4.35F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, true);
		hair.setTextureOffset(0, 46).addBox(-4.35F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, true);
		hair.setTextureOffset(16, 56).addBox(-4.45F, -26.0F, 1.02F, 0.45F, 1.1F, 1.0F, 0.0F, false);
		hair.setTextureOffset(12, 56).addBox(4.0F, -26.0F, 1.02F, 0.45F, 1.1F, 1.0F, 0.0F, false);
		hair.setTextureOffset(51, 46).addBox(4.0F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, false);
		hair.setTextureOffset(12, 60).addBox(2.75F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, false);
		hair.setTextureOffset(18, 58).addBox(4.0F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, false);
		hair.setTextureOffset(23, 57).addBox(4.0F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, false);
		hair.setTextureOffset(26, 46).addBox(4.0F, -26.0F, 2.02F, 0.35F, 1.1F, 2.0F, 0.0F, false);
		hair.setTextureOffset(24, 49).addBox(-4.35F, -26.0F, 2.02F, 0.35F, 1.1F, 2.0F, 0.0F, true);

		mask = new ModelRenderer(this);
		mask.setRotationPoint(6.0F, -0.5F, -5.0F);
		bipedHead.addChild(mask);
		mask.setTextureOffset(72, 0).addBox(-10.28F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);
		mask.setTextureOffset(87, 0).addBox(-10.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
		mask.setTextureOffset(87, 4).addBox(-7.5F, -3.0F, 0.76F, 3.0F, 4.0F, 0.75F, 0.0F, false);
		mask.setTextureOffset(87, 10).addBox(-5.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
		mask.setTextureOffset(94, 0).addBox(-9.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
		mask.setTextureOffset(95, 3).addBox(-5.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
		mask.setTextureOffset(95, 6).addBox(-2.0F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);

		headDetail = new ModelRenderer(this);
		headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
		bipedHead.addChild(headDetail);
		headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
		bipedLeftArm.setTextureOffset(20, 32).addBox(-1.5F, 5.0F, -2.25F, 5.0F, 0.75F, 4.5F, 0.0F, false);

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedRightArm.setTextureOffset(0, 32).addBox(-3.5F, 5.0F, -2.25F, 5.0F, 0.75F, 4.5F, 0.0F, true);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}