package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityLumberjackMale extends BipedModel
{
    // fields
    RendererModel Log1;
    RendererModel Log2;
    RendererModel Log3;
    RendererModel Basket1;
    RendererModel Basket2;
    RendererModel Basket3;
    RendererModel Basket4;
    RendererModel Basket5;
    RendererModel Basket6;
    RendererModel Basket7;
    RendererModel Basket8;
    RendererModel Basket9;
    RendererModel Basket10;
    RendererModel Basket11;
    RendererModel BasketE1;
    RendererModel BasketE2;

    public ModelEntityLumberjackMale()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new RendererModel(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 0F);
        bipedHeadwear.setTextureSize(64, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(64, 64);
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        Log1 = new RendererModel(this, 25, 33);
        Log1.addBox(-5F, 8F, 3F, 10, 3, 3);
        Log1.setRotationPoint(0F, 0F, 0F);
        Log1.setTextureSize(64, 64);
        setRotation(Log1, 0F, 0F, 0F);

        Log2 = new RendererModel(this, 28, 39);
        Log2.addBox(-5F, 6F, -1F, 5, 3, 3);
        Log2.setRotationPoint(0F, 0F, 0F);
        Log2.setTextureSize(64, 64);
        setRotation(Log2, 0.5585054F, 0F, 0F);

		/*
         * Log3 = new RendererModel(this, 28, 33); Log3.addBox(-2F, 0F, 3F, 10,
		 * 3, 3); Log3.setRotationPoint(0F, 0F, 0F); Log3.setTextureSize(64,
		 * 64); setRotation(Log3, 0F, 0F, 1.099557F);
		 */

        Log3 = new RendererModel(this, 52, 33);
        Log3.addBox(-3F, -2F, 3F, 3, 10, 3);
        Log3.setRotationPoint(0F, 0F, 0F);
        Log3.setTextureSize(64, 64);
        setRotation(Log3, 0F, 0F, -0.5061455F);

        Basket1 = new RendererModel(this, 1, 33);
        Basket1.addBox(-3F, 0F, 2F, 1, 12, 1);
        Basket1.setRotationPoint(0F, 0F, 0F);
        Basket1.setTextureSize(64, 64);
        setRotation(Basket1, 0F, 0F, 0F);

        Basket2 = new RendererModel(this, 1, 33);
        Basket2.addBox(2F, 0F, 2F, 1, 12, 1);
        Basket2.setRotationPoint(0F, 0F, 0F);
        Basket2.setTextureSize(64, 64);
        setRotation(Basket2, 0F, 0F, 0F);

        Basket3 = new RendererModel(this, 12, 33);
        Basket3.addBox(-3F, 4F, 6F, 1, 8, 1);
        Basket3.setRotationPoint(0F, 0F, 0F);
        Basket3.setTextureSize(64, 64);
        setRotation(Basket3, 0F, 0F, 0F);

        Basket4 = new RendererModel(this, 12, 33);
        Basket4.addBox(2F, 4F, 6F, 1, 8, 1);
        Basket4.setRotationPoint(0F, 0F, 0F);
        Basket4.setTextureSize(64, 64);
        setRotation(Basket4, 0F, 0F, 0F);

        Basket5 = new RendererModel(this, 1, 33);
        Basket5.addBox(2F, 11F, 3F, 1, 1, 3);
        Basket5.setRotationPoint(0F, 0F, 0F);
        Basket5.setTextureSize(64, 64);
        setRotation(Basket5, 0F, 0F, 0F);

        Basket6 = new RendererModel(this, 1, 33);
        Basket6.addBox(-3F, 11F, 3F, 1, 1, 3);
        Basket6.setRotationPoint(0F, 0F, 0F);
        Basket6.setTextureSize(64, 64);
        setRotation(Basket6, 0F, 0F, 0F);

        Basket7 = new RendererModel(this, 17, 33);
        Basket7.addBox(2F, 2F, 1F, 1, 6, 1);
        Basket7.setRotationPoint(0F, 0F, 0F);
        Basket7.setTextureSize(64, 64);
        setRotation(Basket7, 0.8080874F, 0F, 0F);

        Basket8 = new RendererModel(this, 17, 33);
        Basket8.addBox(-3F, 2F, 1F, 1, 6, 1);
        Basket8.setRotationPoint(0F, 0F, 0F);
        Basket8.setTextureSize(64, 64);
        setRotation(Basket8, 0.8080874F, 0F, 0F);

        Basket9 = new RendererModel(this, 12, 43);
        Basket9.addBox(-2F, 4F, 6F, 4, 1, 1);
        Basket9.setRotationPoint(0F, 0F, 0F);
        Basket9.setTextureSize(64, 64);
        setRotation(Basket9, 0F, 0F, 0F);

        Basket10 = new RendererModel(this, 1, 33);
        Basket10.addBox(-2F, 11F, 6F, 4, 1, 1);
        Basket10.setRotationPoint(0F, 0F, 0F);
        Basket10.setTextureSize(64, 64);
        setRotation(Basket10, 0F, 0F, 0F);

        Basket11 = new RendererModel(this, 1, 33);
        Basket11.addBox(-2F, 11F, 2F, 4, 1, 1);
        Basket11.setRotationPoint(0F, 0F, 0F);
        Basket11.setTextureSize(64, 64);
        setRotation(Basket11, 0F, 0F, 0F);

        BasketE1 = new RendererModel(this, 1, 47);
        BasketE1.addBox(2F, 1F, 1F, 1, 12, 1);
        BasketE1.setRotationPoint(0F, 0F, 0F);
        BasketE1.setTextureSize(64, 64);
        setRotation(BasketE1, 0.413643F, 0F, 0F);

        BasketE2 = new RendererModel(this, 1, 47);
        BasketE2.addBox(-3F, 1F, 1F, 1, 12, 1);
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

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
