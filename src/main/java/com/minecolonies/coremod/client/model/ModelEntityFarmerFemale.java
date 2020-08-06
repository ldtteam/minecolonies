// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFarmerFemale()
    {
        ModelRenderer chest;
        ModelRenderer boxBottom;
        ModelRenderer boxFront;
        ModelRenderer boxBack;
        ModelRenderer boxLeft;
        ModelRenderer boxRight;
        ModelRenderer seeds;
        ModelRenderer strapLeft;
        ModelRenderer strapRight;
        ModelRenderer headDetail;
        ModelRenderer hatFrill;
        ModelRenderer hatBottom;
        ModelRenderer hatTop;
        ModelRenderer hatStrap;
        ModelRenderer ponytailBase;
        ModelRenderer ponytailTail;

        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
        chest.setTextureOffset(17, 32).addBox(-3.5F, 2.7F, -0.6F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        boxBottom = new ModelRenderer(this);
        boxBottom.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxBottom);
        boxBottom.setTextureOffset(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        boxFront = new ModelRenderer(this);
        boxFront.setRotationPoint(0.0F, 9.0F, -4.0F);
        bipedBody.addChild(boxFront);
        boxFront.setTextureOffset(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxBack = new ModelRenderer(this);
        boxBack.setRotationPoint(0.0F, 9.0F, 0.0F);
        bipedBody.addChild(boxBack);
        boxBack.setTextureOffset(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

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
        strapLeft.setTextureOffset(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        strapRight = new ModelRenderer(this);
        strapRight.setRotationPoint(-4.0F, 4.0F, -4.0F);
        bipedBody.addChild(strapRight);
        setRotationAngle(strapRight, 1.0472F, 0.0F, 0.0F);
        strapRight.setTextureOffset(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatFrill = new ModelRenderer(this);
        hatFrill.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatFrill);
        setRotationAngle(hatFrill, -0.6981F, 0.0F, 0.0F);
        hatFrill.setTextureOffset(57, 21).addBox(-5.5F, -5.7F, -8.0F, 11.0F, 1.0F, 10.0F, 0.0F, true);

        hatBottom = new ModelRenderer(this);
        hatBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.7854F, 0.0F, 0.0F);
        hatBottom.setTextureOffset(61, 9).addBox(-5.0F, -7.8F, -7.0F, 10.0F, 3.0F, 8.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatTop);
        setRotationAngle(hatTop, -0.7854F, 0.0F, 0.0F);
        hatTop.setTextureOffset(64, 1).addBox(-4.5F, -8.5F, -6.0F, 9.0F, 1.0F, 6.0F, 0.0F, true);

        hatStrap = new ModelRenderer(this);
        hatStrap.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatStrap);
        setRotationAngle(hatStrap, -0.3491F, 0.0F, 0.0F);
        hatStrap.setTextureOffset(68, 33).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F, 0.0F, true);

        ponytailBase = new ModelRenderer(this);
        ponytailBase.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(ponytailBase);
        setRotationAngle(ponytailBase, 0.1047F, 0.0F, 0.0F);
        ponytailBase.setTextureOffset(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        ponytailTail = new ModelRenderer(this);
        ponytailTail.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(ponytailTail);
        setRotationAngle(ponytailTail, 0.2269F, 0.0F, 0.0F);
        ponytailTail.setTextureOffset(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
