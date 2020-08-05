// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityChildFemale extends CitizenModel<AbstractEntityCitizen> {
	ModelRenderer overRightLeg;
	ModelRenderer overLeftLeg;
	ModelRenderer pouch;
	ModelRenderer headDress;

	public ModelEntityChildFemale() {
		textureWidth = 128;
		textureHeight = 64;

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(44, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(44, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		overRightLeg = new ModelRenderer(this);
		overRightLeg.setRotationPoint(-4.5F, 12.0F, -2.5F);
		bipedRightLeg.addChild(overRightLeg);
		overRightLeg.setTextureOffset(0, 33).addBox(2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F, 0.0F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		overLeftLeg = new ModelRenderer(this);
		overLeftLeg.setRotationPoint(-0.5F, 12.0F, -2.5F);
		bipedLeftLeg.addChild(overLeftLeg);
		overLeftLeg.setTextureOffset(0, 33).addBox(-2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F, 0.0F, true);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 12.0F, 5.0F, 0.0F, true);

		pouch = new ModelRenderer(this);
		pouch.setRotationPoint(-4.0F, 9.5F, -3.5F);
		bipedBody.addChild(pouch);
		pouch.setTextureOffset(20, 33).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 1.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

		headDress = new ModelRenderer(this);
		headDress.setRotationPoint(-4.5F, -8.5F, -2.0F);
		bipedHead.addChild(headDress);
		setRotationAngle(headDress, -0.5236F, 0.0F, 0.0F);
		headDress.setTextureOffset(20, 37).addBox(0.0F, 0.0F, 0.0F, 9.0F, 1.0F, 8.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}