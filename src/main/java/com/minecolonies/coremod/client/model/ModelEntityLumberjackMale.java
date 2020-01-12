package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityLumberjackMale extends CitizenModel
{
    // fields
    ModelRenderer Log1;
    ModelRenderer Log2;
    ModelRenderer Log3;
    ModelRenderer Basket1;
    ModelRenderer Basket2;
    ModelRenderer Basket3;
    ModelRenderer Basket4;
    ModelRenderer Basket5;
    ModelRenderer Basket6;
    ModelRenderer Basket7;
    ModelRenderer Basket8;
    ModelRenderer Basket9;
    ModelRenderer Basket10;
    ModelRenderer Basket11;
    ModelRenderer BasketE1;
    ModelRenderer BasketE2;

    public ModelEntityLumberjackMale()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addCuboid(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 0F);
        bipedHeadwear.setTextureSize(64, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(64, 64);
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        Log1 = new ModelRenderer(this, 25, 33);
        Log1.addCuboid(-5F, 8F, 3F, 10, 3, 3);
        Log1.setRotationPoint(0F, 0F, 0F);
        Log1.setTextureSize(64, 64);
        setRotation(Log1, 0F, 0F, 0F);

        Log2 = new ModelRenderer(this, 28, 39);
        Log2.addCuboid(-5F, 6F, -1F, 5, 3, 3);
        Log2.setRotationPoint(0F, 0F, 0F);
        Log2.setTextureSize(64, 64);
        setRotation(Log2, 0.5585054F, 0F, 0F);

		/*
         * Log3 = new ModelRenderer(this, 28, 33); Log3.addCuboid(-2F, 0F, 3F, 10,
		 * 3, 3); Log3.setRotationPoint(0F, 0F, 0F); Log3.setTextureSize(64,
		 * 64); setRotation(Log3, 0F, 0F, 1.099557F);
		 */

        Log3 = new ModelRenderer(this, 52, 33);
        Log3.addCuboid(-3F, -2F, 3F, 3, 10, 3);
        Log3.setRotationPoint(0F, 0F, 0F);
        Log3.setTextureSize(64, 64);
        setRotation(Log3, 0F, 0F, -0.5061455F);

        Basket1 = new ModelRenderer(this, 1, 33);
        Basket1.addCuboid(-3F, 0F, 2F, 1, 12, 1);
        Basket1.setRotationPoint(0F, 0F, 0F);
        Basket1.setTextureSize(64, 64);
        setRotation(Basket1, 0F, 0F, 0F);

        Basket2 = new ModelRenderer(this, 1, 33);
        Basket2.addCuboid(2F, 0F, 2F, 1, 12, 1);
        Basket2.setRotationPoint(0F, 0F, 0F);
        Basket2.setTextureSize(64, 64);
        setRotation(Basket2, 0F, 0F, 0F);

        Basket3 = new ModelRenderer(this, 12, 33);
        Basket3.addCuboid(-3F, 4F, 6F, 1, 8, 1);
        Basket3.setRotationPoint(0F, 0F, 0F);
        Basket3.setTextureSize(64, 64);
        setRotation(Basket3, 0F, 0F, 0F);

        Basket4 = new ModelRenderer(this, 12, 33);
        Basket4.addCuboid(2F, 4F, 6F, 1, 8, 1);
        Basket4.setRotationPoint(0F, 0F, 0F);
        Basket4.setTextureSize(64, 64);
        setRotation(Basket4, 0F, 0F, 0F);

        Basket5 = new ModelRenderer(this, 1, 33);
        Basket5.addCuboid(2F, 11F, 3F, 1, 1, 3);
        Basket5.setRotationPoint(0F, 0F, 0F);
        Basket5.setTextureSize(64, 64);
        setRotation(Basket5, 0F, 0F, 0F);

        Basket6 = new ModelRenderer(this, 1, 33);
        Basket6.addCuboid(-3F, 11F, 3F, 1, 1, 3);
        Basket6.setRotationPoint(0F, 0F, 0F);
        Basket6.setTextureSize(64, 64);
        setRotation(Basket6, 0F, 0F, 0F);

        Basket7 = new ModelRenderer(this, 17, 33);
        Basket7.addCuboid(2F, 2F, 1F, 1, 6, 1);
        Basket7.setRotationPoint(0F, 0F, 0F);
        Basket7.setTextureSize(64, 64);
        setRotation(Basket7, 0.8080874F, 0F, 0F);

        Basket8 = new ModelRenderer(this, 17, 33);
        Basket8.addCuboid(-3F, 2F, 1F, 1, 6, 1);
        Basket8.setRotationPoint(0F, 0F, 0F);
        Basket8.setTextureSize(64, 64);
        setRotation(Basket8, 0.8080874F, 0F, 0F);

        Basket9 = new ModelRenderer(this, 12, 43);
        Basket9.addCuboid(-2F, 4F, 6F, 4, 1, 1);
        Basket9.setRotationPoint(0F, 0F, 0F);
        Basket9.setTextureSize(64, 64);
        setRotation(Basket9, 0F, 0F, 0F);

        Basket10 = new ModelRenderer(this, 1, 33);
        Basket10.addCuboid(-2F, 11F, 6F, 4, 1, 1);
        Basket10.setRotationPoint(0F, 0F, 0F);
        Basket10.setTextureSize(64, 64);
        setRotation(Basket10, 0F, 0F, 0F);

        Basket11 = new ModelRenderer(this, 1, 33);
        Basket11.addCuboid(-2F, 11F, 2F, 4, 1, 1);
        Basket11.setRotationPoint(0F, 0F, 0F);
        Basket11.setTextureSize(64, 64);
        setRotation(Basket11, 0F, 0F, 0F);

        BasketE1 = new ModelRenderer(this, 1, 47);
        BasketE1.addCuboid(2F, 1F, 1F, 1, 12, 1);
        BasketE1.setRotationPoint(0F, 0F, 0F);
        BasketE1.setTextureSize(64, 64);
        setRotation(BasketE1, 0.413643F, 0F, 0F);

        BasketE2 = new ModelRenderer(this, 1, 47);
        BasketE2.addCuboid(-3F, 1F, 1F, 1, 12, 1);
        BasketE2.setRotationPoint(0F, 0F, 0F);
        BasketE2.setTextureSize(64, 64);
        setRotation(BasketE2, 0.413643F, 0F, 0F);

        bipedBody.addChild(Log1);
        bipedBody.addChild(Log1);
        bipedBody.addChild(Log2);
        bipedBody.addChild(Log3);
        bipedBody.addChild(Basket1);
        bipedBody.addChild(Basket2);
        bipedBody.addChild(Basket3);
        bipedBody.addChild(Basket4);
        bipedBody.addChild(Basket5);
        bipedBody.addChild(Basket6);
        bipedBody.addChild(Basket7);
        bipedBody.addChild(Basket8);
        bipedBody.addChild(Basket9);
        bipedBody.addChild(Basket10);
        bipedBody.addChild(Basket11);
        bipedBody.addChild(BasketE1);
        bipedBody.addChild(BasketE2);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
