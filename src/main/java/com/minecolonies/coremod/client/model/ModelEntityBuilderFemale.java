// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBuilderFemale extends CitizenModel<AbstractEntityCitizen> {
	ModelRenderer chest;
	ModelRenderer belt;
	ModelRenderer ruler;
	ModelRenderer hammerHandle;
	ModelRenderer hammerHead;
	ModelRenderer headDetail;
	ModelRenderer hatBase;
	ModelRenderer hatBottomMiddle;
	ModelRenderer hatBack;
	ModelRenderer hatFront;
	ModelRenderer hatTopMiddle;
	ModelRenderer hatBrimBase;
	ModelRenderer hatBrimFront;
	ModelRenderer hatBrimFrontTip;
	ModelRenderer ponytailBase;
	ModelRenderer ponytailTail;

	public ModelEntityBuilderFemale() {
		textureWidth = 128;
		textureHeight = 64;

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
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

		chest = new ModelRenderer(this);
		chest.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(chest);
		setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
		chest.setTextureOffset(17, 32).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 3.0F, 4.0F, 0.0F, true);

		belt = new ModelRenderer(this);
		belt.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(belt);
		belt.setTextureOffset(0, 40).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, true);

		ruler = new ModelRenderer(this);
		ruler.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(ruler);
		ruler.setTextureOffset(17, 47).addBox(2.0F, 7.3F, -2.2F, 1.0F, 4.0F, 1.0F, 0.0F, true);

		hammerHandle = new ModelRenderer(this);
		hammerHandle.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(hammerHandle);
		setRotationAngle(hammerHandle, 0.0F, 0.0F, 0.3142F);
		hammerHandle.setTextureOffset(2, 49).addBox(1.0F, 7.3F, -2.4F, 1.0F, 4.0F, 1.0F, 0.0F, true);

		hammerHead = new ModelRenderer(this);
		hammerHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		hammerHandle.addChild(hammerHead);
		hammerHead.setTextureOffset(0, 47).addBox(0.0F, 7.5F, -2.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

		headDetail = new ModelRenderer(this);
		headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
		bipedHead.addChild(headDetail);
		headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		hatBase = new ModelRenderer(this);
		hatBase.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(hatBase);
		setRotationAngle(hatBase, -0.1396F, 0.0F, 0.0F);
		hatBase.setTextureOffset(57, 19).addBox(-4.0F, -9.7F, -4.0F, 8.0F, 2.0F, 7.0F, 0.0F, true);

		hatBottomMiddle = new ModelRenderer(this);
		hatBottomMiddle.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBase.addChild(hatBottomMiddle);
		hatBottomMiddle.setTextureOffset(57, 8).addBox(-3.0F, -10.0F, -5.0F, 6.0F, 2.0F, 9.0F, 0.0F, true);

		hatBack = new ModelRenderer(this);
		hatBack.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBottomMiddle.addChild(hatBack);
		hatBack.setTextureOffset(64, 31).addBox(-3.5F, -8.0F, 4.0F, 7.0F, 1.0F, 1.0F, 0.0F, true);

		hatFront = new ModelRenderer(this);
		hatFront.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBottomMiddle.addChild(hatFront);
		hatFront.setTextureOffset(66, 28).addBox(-2.5F, -9.0F, -6.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

		hatTopMiddle = new ModelRenderer(this);
		hatTopMiddle.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBottomMiddle.addChild(hatTopMiddle);
		hatTopMiddle.setTextureOffset(61, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 1.0F, 7.0F, 0.0F, true);

		hatBrimBase = new ModelRenderer(this);
		hatBrimBase.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBase.addChild(hatBrimBase);
		hatBrimBase.setTextureOffset(53, 33).addBox(-4.5F, -8.0F, -6.0F, 9.0F, 1.0F, 10.0F, 0.0F, true);

		hatBrimFront = new ModelRenderer(this);
		hatBrimFront.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBrimBase.addChild(hatBrimFront);
		hatBrimFront.setTextureOffset(64, 44).addBox(-3.5F, -8.0F, -7.0F, 7.0F, 1.0F, 1.0F, 0.0F, true);

		hatBrimFrontTip = new ModelRenderer(this);
		hatBrimFrontTip.setRotationPoint(0.0F, 0.0F, 0.0F);
		hatBrimFront.addChild(hatBrimFrontTip);
		hatBrimFrontTip.setTextureOffset(66, 46).addBox(-2.5F, -8.0F, -8.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

		ponytailBase = new ModelRenderer(this);
		ponytailBase.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(ponytailBase);
		setRotationAngle(ponytailBase, 0.2269F, 0.0F, 0.0F);
		ponytailBase.setTextureOffset(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

		ponytailTail = new ModelRenderer(this);
		ponytailTail.setRotationPoint(0.0F, 0.0F, 0.0F);
		ponytailBase.addChild(ponytailTail);
		setRotationAngle(ponytailTail, -0.1222F, 0.0F, 0.0F);
		ponytailTail.setTextureOffset(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}