// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.modularcitizen.ModelRenderer;

public class ModelEntityUndertakerMale extends CitizenModel
 {

	public ModelEntityUndertakerMale() {
		
		ModelRenderer Hat;
		ModelRenderer hatpartlowleft;
		ModelRenderer hatpartlowright;
		
		textureWidth = 128;
		textureHeight = 64;

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		Hat = new ModelRenderer(this);
		Hat.setRotationPoint(0.0F, -5.6F, 0.0F);
		bipedHead.addChild(Hat);
		setRotationAngle(Hat, -0.0611F, 0.0F, 0.0F);
		Hat.setTextureOffset(64, 0).addBox(-3.9664F, -3.1854F, -5.0F, 8.0F, 2.0F, 10.0F, 0.0F, false);
		Hat.setTextureOffset(90, 0).addBox(-2.9664F, -10.6F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
		Hat.setTextureOffset(64, 12).addBox(-2.9664F, -11.3F, -4.0F, 6.0F, 10.0F, 1.0F, -0.101F, false);
		Hat.setTextureOffset(64, 22).addBox(2.5336F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, -0.102F, false);
		Hat.setTextureOffset(80, 22).addBox(-3.4664F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, -0.103F, false);
		Hat.setTextureOffset(78, 12).addBox(-2.9664F, -11.3F, 3.0F, 6.0F, 10.0F, 1.0F, -0.1F, false);

		hatpartlowleft = new ModelRenderer(this);
		hatpartlowleft.setRotationPoint(-3.1F, -2.2F, 0.0F);
		Hat.addChild(hatpartlowleft);
		setRotationAngle(hatpartlowleft, 0.0F, 0.0F, 0.48F);
		hatpartlowleft.setTextureOffset(92, 7).addBox(-2.3F, -0.7F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

		hatpartlowright = new ModelRenderer(this);
		hatpartlowright.setRotationPoint(6.0079F, -4.9714F, 0.0F);
		Hat.addChild(hatpartlowright);
		setRotationAngle(hatpartlowright, 0.0F, 0.0F, 1.0908F);
		hatpartlowright.setTextureOffset(96, 19).addBox(0.4466F, 1.4993F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		bipedBody.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedRightArm.setTextureOffset(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedLeftArm.setTextureOffset(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedLeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}