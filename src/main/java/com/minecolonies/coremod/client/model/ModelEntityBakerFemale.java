// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBakerFemale extends CitizenModel<AbstractEntityCitizen>  {
	ModelRenderer breast;
	ModelRenderer headdetail;
	ModelRenderer ponytail;
	ModelRenderer ponyTailB;
	ModelRenderer ponyTailT;
	ModelRenderer hat;
	ModelRenderer topL;
	ModelRenderer topF;
	ModelRenderer topR;
	ModelRenderer midR;
	ModelRenderer midL;
	ModelRenderer lipR;
	ModelRenderer lipT;
	ModelRenderer lipL;
	ModelRenderer lipB;
	ModelRenderer baseT;
	ModelRenderer baseB;
	ModelRenderer baseM;
	ModelRenderer botL;

	public ModelEntityBakerFemale() {
		textureWidth = 256;
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

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

		breast = new ModelRenderer(this);
		breast.setRotationPoint(-1.0F, 3.0F, 4.0F);
		bipedBody.addChild(breast);
		setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
		breast.setTextureOffset(18, 33).addBox(-2.5F, 2.5F, -5.366F, 7.0F, 3.0F, 3.0F, 0.5F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

		headdetail = new ModelRenderer(this);
		headdetail.setRotationPoint(0.0F, 24.0F, 0.0F);
		bipedHead.addChild(headdetail);
		headdetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		ponytail = new ModelRenderer(this);
		ponytail.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(ponytail);
		

		ponyTailB = new ModelRenderer(this);
		ponyTailB.setRotationPoint(0.0F, 0.0F, 0.0F);
		ponytail.addChild(ponyTailB);
		setRotationAngle(ponyTailB, 0.1047F, 0.0F, 0.0F);
		ponyTailB.setTextureOffset(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);

		ponyTailT = new ModelRenderer(this);
		ponyTailT.setRotationPoint(0.0F, 0.0F, 0.0F);
		ponytail.addChild(ponyTailT);
		setRotationAngle(ponyTailT, 0.2269F, 0.0F, 0.0F);
		ponyTailT.setTextureOffset(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

		hat = new ModelRenderer(this);
		hat.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(hat);
		

		topL = new ModelRenderer(this);
		topL.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(topL);
		topL.setTextureOffset(64, 4).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 5.0F, 0.0F, true);

		topF = new ModelRenderer(this);
		topF.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(topF);
		topF.setTextureOffset(64, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

		topR = new ModelRenderer(this);
		topR.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(topR);
		topR.setTextureOffset(78, 4).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 1.0F, 5.0F, 0.0F, true);

		midR = new ModelRenderer(this);
		midR.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(midR);
		midR.setTextureOffset(76, 10).addBox(-4.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

		midL = new ModelRenderer(this);
		midL.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(midL);
		midL.setTextureOffset(64, 10).addBox(1.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

		lipR = new ModelRenderer(this);
		lipR.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(lipR);
		setRotationAngle(lipR, -0.1842F, -0.8754F, -1.2905F);
		lipR.setTextureOffset(22, 70).addBox(2.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

		lipT = new ModelRenderer(this);
		lipT.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(lipT);
		setRotationAngle(lipT, 0.2231F, 0.0F, 0.0F);
		lipT.setTextureOffset(0, 67).addBox(-5.0F, -9.2F, -1.0F, 10.0F, 1.0F, 2.0F, 0.0F, true);

		lipL = new ModelRenderer(this);
		lipL.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(lipL);
		setRotationAngle(lipL, -0.1844F, 0.8755F, 1.2904F);
		lipL.setTextureOffset(0, 70).addBox(-4.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

		lipB = new ModelRenderer(this);
		lipB.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(lipB);
		setRotationAngle(lipB, -1.3756F, 0.0F, 0.0F);
		lipB.setTextureOffset(0, 80).addBox(-5.0F, -5.1F, -1.5F, 10.0F, 1.0F, 2.0F, 0.0F, true);

		baseT = new ModelRenderer(this);
		baseT.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(baseT);
		setRotationAngle(baseT, -0.8923F, 0.0F, 0.0F);
		baseT.setTextureOffset(0, 40).addBox(-4.5F, -8.2F, -6.5F, 9.0F, 1.0F, 6.0F, 0.0F, true);

		baseB = new ModelRenderer(this);
		baseB.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(baseB);
		setRotationAngle(baseB, -0.8923F, 0.0F, 0.0F);
		baseB.setTextureOffset(0, 57).addBox(-5.0F, -5.2F, -8.0F, 10.0F, 1.0F, 9.0F, 0.0F, true);

		baseM = new ModelRenderer(this);
		baseM.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(baseM);
		setRotationAngle(baseM, -0.8923F, 0.0F, 0.0F);
		baseM.setTextureOffset(0, 47).addBox(-4.5F, -7.2F, -7.5F, 9.0F, 2.0F, 8.0F, 0.0F, true);

		botL = new ModelRenderer(this);
		botL.setRotationPoint(0.0F, 0.0F, 0.0F);
		hat.addChild(botL);
		botL.setTextureOffset(64, 14).addBox(1.5F, -5.5F, -1.5F, 3.0F, 2.0F, 1.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}