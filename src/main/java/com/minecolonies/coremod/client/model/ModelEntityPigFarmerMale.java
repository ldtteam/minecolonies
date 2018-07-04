package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityPigFarmerMale extends ModelBiped
{
    ModelRenderer strapR;
    ModelRenderer strapL;
    ModelRenderer carrotBase;
    ModelRenderer carrot1;
    ModelRenderer carrot2;
    ModelRenderer carrot3;
    ModelRenderer carrot4;

    public ModelEntityPigFarmerMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        strapR = new ModelRenderer(this, 0, 36);
        strapR.addBox(-3.8F, 0.01F, -2.5F, 1, 9, 4);
        strapR.setRotationPoint(0F, 0F, 0F);
        strapR.setTextureSize(128, 64);
        strapR.mirror = true;
        setRotation(strapR, -0.0698132F, 0F, 0F);

        strapL = new ModelRenderer(this, 10, 36);
        strapL.addBox(2.8F, 0.01F, -2.5F, 1, 9, 4);
        strapL.setRotationPoint(0F, 0F, 0F);
        strapL.setTextureSize(128, 64);
        strapL.mirror = true;
        setRotation(strapL, -0.0698132F, 0F, 0F);

        carrotBase = new ModelRenderer(this, 0, 49);
        carrotBase.addBox(-3.5F, 8F, -3.5F, 7, 3, 4);
        carrotBase.setRotationPoint(0F, 0F, 0F);
        carrotBase.setTextureSize(128, 64);
        carrotBase.mirror = true;
        setRotation(carrotBase, 0F, 0F, 0F);

        carrot1 = new ModelRenderer(this, 0, 33);
        carrot1.addBox(-2.5F, 6F, -1.5F, 1, 3, 0);
        carrot1.setRotationPoint(0F, 0F, 0F);
        carrot1.setTextureSize(128, 64);
        carrot1.mirror = true;
        setRotation(carrot1, -0.1115358F, 0F, -0.0174533F);

        carrot2 = new ModelRenderer(this, 2, 33);
        carrot2.addBox(0.5F, 6F, -2.5F, 1, 3, 0);
        carrot2.setRotationPoint(0F, 0F, 0F);
        carrot2.setTextureSize(128, 64);
        carrot2.mirror = true;
        setRotation(carrot2, 0F, 0.3346075F, 0.1115358F);

        carrot3 = new ModelRenderer(this, 4, 33);
        carrot3.addBox(1F, 6F, -2.5F, 1, 3, 0);
        carrot3.setRotationPoint(0F, 0F, 0F);
        carrot3.setTextureSize(128, 64);
        carrot3.mirror = true;
        setRotation(carrot3, 0F, -0.1115358F, 0.1487144F);

        carrot4 = new ModelRenderer(this, 6, 33);
        carrot4.addBox(0F, 6.5F, -2.5F, 1, 3, 0);
        carrot4.setRotationPoint(0F, 0F, 0F);
        carrot4.setTextureSize(128, 64);
        carrot4.mirror = true;
        setRotation(carrot4, 0F, -0.1487144F, -0.1858931F);


        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody.addChild(carrot1);
        bipedBody.addChild(carrot2);
        bipedBody.addChild(carrot3);
        bipedBody.addChild(carrot4);
        bipedBody.addChild(carrotBase);
        bipedBody.addChild(strapL);
        bipedBody.addChild(strapR);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
