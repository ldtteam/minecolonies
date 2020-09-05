// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCookFemale extends CitizenModel<AbstractEntityCitizen>
{
	
	public ModelEntityCookFemale() {
		ModelRenderer skirt;
		ModelRenderer dress1;
		ModelRenderer dress2;
		ModelRenderer dress3;
		ModelRenderer bipedChest;
		ModelRenderer headDetail;
		ModelRenderer hair;
		
		textureWidth = 128;
		textureHeight = 128;

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

		skirt = new ModelRenderer(this);
		skirt.setRotationPoint(-4.0F, 12.0F, -4.0F);
		bipedBody.addChild(skirt);
		

		dress1 = new ModelRenderer(this);
		dress1.setRotationPoint(0.0F, 0.0F, 0.0F);
		skirt.addChild(dress1);
		dress1.setTextureOffset(0, 49).addBox(0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 6.0F, 0.0F, true);

		dress2 = new ModelRenderer(this);
		dress2.setRotationPoint(-1.0F, 1.0F, 0.0F);
		skirt.addChild(dress2);
		dress2.setTextureOffset(0, 56).addBox(0.0F, 0.0F, 0.0F, 10.0F, 4.0F, 8.0F, 0.0F, true);

		dress3 = new ModelRenderer(this);
		dress3.setRotationPoint(-2.0F, 5.0F, -1.0F);
		skirt.addChild(dress3);
		dress3.setTextureOffset(0, 68).addBox(0.0F, 0.0F, 0.0F, 12.0F, 3.0F, 10.0F, 0.0F, true);

		bipedChest = new ModelRenderer(this);
		bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(bipedChest);
		setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
		bipedChest.setTextureOffset(17, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		headDetail = new ModelRenderer(this);
		headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
		bipedHead.addChild(headDetail);
		headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		hair = new ModelRenderer(this);
		hair.setRotationPoint(-4.5F, -9.2F, 0.0F);
		bipedHead.addChild(hair);
		setRotationAngle(hair, -0.8551F, 0.0F, 0.0F);
		hair.setTextureOffset(0, 39).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 8.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}