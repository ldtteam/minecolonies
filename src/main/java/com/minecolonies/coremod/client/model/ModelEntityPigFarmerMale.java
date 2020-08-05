// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityPigFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityPigFarmerMale()
    {
        ModelRenderer carrot1;
        ModelRenderer carrot2;
        ModelRenderer carrot3;
        ModelRenderer carrot4;
        ModelRenderer carrotBase;
        ModelRenderer strapL;
        ModelRenderer strapR;
        ModelRenderer headDetail;

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
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        carrot1 = new ModelRenderer(this);
        carrot1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(carrot1);
        setRotationAngle(carrot1, -0.1115F, 0.0F, -0.0175F);
        carrot1.setTextureOffset(0, 33).addBox(-2.5F, 6.0F, -1.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot2 = new ModelRenderer(this);
        carrot2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(carrot2);
        setRotationAngle(carrot2, 0.0F, 0.3346F, 0.1115F);
        carrot2.setTextureOffset(2, 33).addBox(0.5F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot3 = new ModelRenderer(this);
        carrot3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(carrot3);
        setRotationAngle(carrot3, 0.0F, -0.1115F, 0.1487F);
        carrot3.setTextureOffset(4, 33).addBox(1.0F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot4 = new ModelRenderer(this);
        carrot4.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(carrot4);
        setRotationAngle(carrot4, 0.0F, -0.1487F, -0.1859F);
        carrot4.setTextureOffset(6, 33).addBox(0.0F, 6.5F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrotBase = new ModelRenderer(this);
        carrotBase.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(carrotBase);
        carrotBase.setTextureOffset(0, 49).addBox(-3.5F, 8.0F, -3.5F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        strapL = new ModelRenderer(this);
        strapL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(strapL);
        setRotationAngle(strapL, -0.0698F, 0.0F, 0.0F);
        strapL.setTextureOffset(10, 36).addBox(2.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F, 0.0F, true);

        strapR = new ModelRenderer(this);
        strapR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(strapR);
        setRotationAngle(strapR, -0.0698F, 0.0F, 0.0F);
        strapR.setTextureOffset(0, 36).addBox(-3.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
