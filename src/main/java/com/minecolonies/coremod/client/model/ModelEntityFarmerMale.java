// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFarmerMale()
    {
        ModelRenderer boxBottom;
        ModelRenderer boxBack;
        ModelRenderer boxFront;
        ModelRenderer boxLeft;
        ModelRenderer boxRight;
        ModelRenderer seeds;
        ModelRenderer strapLeft;
        ModelRenderer strapRight;
        ModelRenderer headDetail;
        ModelRenderer hatStrap;
        ModelRenderer hatBottom;
        ModelRenderer hatTop;
        ModelRenderer hatFrillBottom;
        ModelRenderer hatFrillBack;
        ModelRenderer hatFrillFront;
        ModelRenderer hatFrillLeft;
        ModelRenderer hatFrillRight;

        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        boxBottom = new ModelRenderer(this);
        boxBottom.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxBottom);
        boxBottom.setTextureOffset(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        boxBack = new ModelRenderer(this);
        boxBack.setRotationPoint(0.0F, 9.0F, 0.0F);
        bipedBody.addChild(boxBack);
        boxBack.setTextureOffset(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxFront = new ModelRenderer(this);
        boxFront.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxFront);
        boxFront.setTextureOffset(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxLeft = new ModelRenderer(this);
        boxLeft.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxLeft);
        boxLeft.setTextureOffset(42, 43).addBox(3.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, true);

        boxRight = new ModelRenderer(this);
        boxRight.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxRight);
        boxRight.setTextureOffset(0, 43).addBox(-4.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, true);

        seeds = new ModelRenderer(this);
        seeds.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(seeds);
        seeds.setTextureOffset(19, 45).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        strapLeft = new ModelRenderer(this);
        strapLeft.setRotationPoint(3.0F, 4.0F, -4.0F);
        bipedBody.addChild(strapLeft);
        setRotationAngle(strapLeft, 1.0472F, 0.0F, 0.0F);
        strapLeft.setTextureOffset(92, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        strapRight = new ModelRenderer(this);
        strapRight.setRotationPoint(-4.0F, 4.0F, -4.0F);
        bipedBody.addChild(strapRight);
        setRotationAngle(strapRight, 1.0472F, 0.0F, 0.0F);
        strapRight.setTextureOffset(110, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatStrap = new ModelRenderer(this);
        hatStrap.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatStrap);
        setRotationAngle(hatStrap, -0.3491F, 0.0F, 0.0F);
        hatStrap.setTextureOffset(98, 14).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F, 0.0F, true);

        hatBottom = new ModelRenderer(this);
        hatBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.2094F, 0.0F, 0.0F);
        hatBottom.setTextureOffset(57, 11).addBox(-5.0F, -9.8F, -6.0F, 10.0F, 3.0F, 9.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatBottom.addChild(hatTop);
        hatTop.setTextureOffset(64, 2).addBox(-4.5F, -10.5F, -5.0F, 9.0F, 1.0F, 7.0F, 0.0F, true);

        hatFrillBottom = new ModelRenderer(this);
        hatFrillBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatBottom.addChild(hatFrillBottom);
        hatFrillBottom.setTextureOffset(57, 44).addBox(-7.5F, -6.7F, -8.5F, 15.0F, 1.0F, 14.0F, 0.0F, true);

        hatFrillBack = new ModelRenderer(this);
        hatFrillBack.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillBack);
        hatFrillBack.setTextureOffset(87, 40).addBox(-6.5F, -7.7F, 4.5F, 13.0F, 1.0F, 1.0F, 0.0F, true);

        hatFrillFront = new ModelRenderer(this);
        hatFrillFront.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillFront);
        hatFrillFront.setTextureOffset(57, 40).addBox(-6.5F, -7.7F, -8.5F, 13.0F, 1.0F, 1.0F, 0.0F, true);

        hatFrillLeft = new ModelRenderer(this);
        hatFrillLeft.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillLeft);
        hatFrillLeft.setTextureOffset(57, 24).addBox(6.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F, 0.0F, true);

        hatFrillRight = new ModelRenderer(this);
        hatFrillRight.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillRight);
        hatFrillRight.setTextureOffset(88, 24).addBox(-7.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F, 0.0F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
