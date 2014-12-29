package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEntityLumberjackMale extends ModelBiped
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

        public ModelEntityLumberjackMale() {
            textureWidth = 64;
            textureHeight = 64;

            bipedHead = new ModelRenderer(this, 0, 0);
            bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
            bipedHead.setRotationPoint(0F, 0F, 0F);
            bipedHead.setTextureSize(64, 64);
            setRotation(bipedHead, 0F, 0F, 0F);

            bipedHeadwear = new ModelRenderer(this, 32, 0);
            bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
            bipedHeadwear.setRotationPoint(0F, 0F, 0F);
            bipedHeadwear.setTextureSize(64, 64);
            setRotation(bipedHeadwear, 0F, 0F, 0F);

            bipedBody = new ModelRenderer(this, 16, 16);
            bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
            bipedBody.setRotationPoint(0F, 0F, 0F);
            bipedBody.setTextureSize(64, 64);
            setRotation(bipedBody, 0F, 0F, 0F);

            bipedRightArm = new ModelRenderer(this, 40, 16);
            bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
            bipedRightArm.setRotationPoint(-5F, 2F, 0F);
            bipedRightArm.setTextureSize(64, 64);
            setRotation(bipedRightArm, 0F, 0F, 0F);

            bipedLeftArm = new ModelRenderer(this, 40, 16);
            bipedLeftArm.mirror = true;
            bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
            bipedLeftArm.setRotationPoint(5F, 2F, 0F);
            bipedLeftArm.setTextureSize(64, 64);
            setRotation(bipedLeftArm, 0F, 0F, 0F);

            bipedRightLeg = new ModelRenderer(this, 0, 16);
            bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
            bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
            bipedRightLeg.setTextureSize(64, 64);
            setRotation(bipedRightLeg, 0F, 0F, 0F);

            bipedLeftLeg = new ModelRenderer(this, 0, 16);
            bipedLeftLeg.mirror = true;
            bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
            bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
            bipedLeftLeg.setTextureSize(64, 64);
            setRotation(bipedLeftLeg, 0F, 0F, 0F);

            Log1 = new ModelRenderer(this, 25, 33);
            Log1.addBox(-5F, 8F, 3F, 10, 3, 3);
            Log1.setRotationPoint(0F, 0F, 0F);
            Log1.setTextureSize(64, 64);
            setRotation(Log1, 0F, 0F, 0F);

            Log2 = new ModelRenderer(this, 28, 39);
            Log2.addBox(-5F, 6F, -1F, 5, 3, 3);
            Log2.setRotationPoint(0F, 0F, 0F);
            Log2.setTextureSize(64, 64);
            setRotation(Log2, 0.5585054F, 0F, 0F);

		/*
         * Log3 = new ModelRenderer(this, 28, 33); Log3.addBox(-2F, 0F, 3F, 10,
		 * 3, 3); Log3.setRotationPoint(0F, 0F, 0F); Log3.setTextureSize(64,
		 * 64); setRotation(Log3, 0F, 0F, 1.099557F);
		 */

            Log3 = new ModelRenderer(this, 52, 33);
            Log3.addBox(-3F, -2F, 3F, 3, 10, 3);
            Log3.setRotationPoint(0F, 0F, 0F);
            Log3.setTextureSize(64, 64);
            setRotation(Log3, 0F, 0F, -0.5061455F);

            Basket1 = new ModelRenderer(this, 1, 33);
            Basket1.addBox(-3F, 0F, 2F, 1, 12, 1);
            Basket1.setRotationPoint(0F, 0F, 0F);
            Basket1.setTextureSize(64, 64);
            setRotation(Basket1, 0F, 0F, 0F);

            Basket2 = new ModelRenderer(this, 1, 33);
            Basket2.addBox(2F, 0F, 2F, 1, 12, 1);
            Basket2.setRotationPoint(0F, 0F, 0F);
            Basket2.setTextureSize(64, 64);
            setRotation(Basket2, 0F, 0F, 0F);

            Basket3 = new ModelRenderer(this, 12, 33);
            Basket3.addBox(-3F, 4F, 6F, 1, 8, 1);
            Basket3.setRotationPoint(0F, 0F, 0F);
            Basket3.setTextureSize(64, 64);
            setRotation(Basket3, 0F, 0F, 0F);

            Basket4 = new ModelRenderer(this, 12, 33);
            Basket4.addBox(2F, 4F, 6F, 1, 8, 1);
            Basket4.setRotationPoint(0F, 0F, 0F);
            Basket4.setTextureSize(64, 64);
            setRotation(Basket4, 0F, 0F, 0F);

            Basket5 = new ModelRenderer(this, 1, 33);
            Basket5.addBox(2F, 11F, 3F, 1, 1, 3);
            Basket5.setRotationPoint(0F, 0F, 0F);
            Basket5.setTextureSize(64, 64);
            setRotation(Basket5, 0F, 0F, 0F);

            Basket6 = new ModelRenderer(this, 1, 33);
            Basket6.addBox(-3F, 11F, 3F, 1, 1, 3);
            Basket6.setRotationPoint(0F, 0F, 0F);
            Basket6.setTextureSize(64, 64);
            setRotation(Basket6, 0F, 0F, 0F);

            Basket7 = new ModelRenderer(this, 17, 33);
            Basket7.addBox(2F, 2F, 1F, 1, 6, 1);
            Basket7.setRotationPoint(0F, 0F, 0F);
            Basket7.setTextureSize(64, 64);
            setRotation(Basket7, 0.8080874F, 0F, 0F);

            Basket8 = new ModelRenderer(this, 17, 33);
            Basket8.addBox(-3F, 2F, 1F, 1, 6, 1);
            Basket8.setRotationPoint(0F, 0F, 0F);
            Basket8.setTextureSize(64, 64);
            setRotation(Basket8, 0.8080874F, 0F, 0F);

            Basket9 = new ModelRenderer(this, 12, 43);
            Basket9.addBox(-2F, 4F, 6F, 4, 1, 1);
            Basket9.setRotationPoint(0F, 0F, 0F);
            Basket9.setTextureSize(64, 64);
            setRotation(Basket9, 0F, 0F, 0F);

            Basket10 = new ModelRenderer(this, 1, 33);
            Basket10.addBox(-2F, 11F, 6F, 4, 1, 1);
            Basket10.setRotationPoint(0F, 0F, 0F);
            Basket10.setTextureSize(64, 64);
            setRotation(Basket10, 0F, 0F, 0F);

            Basket11 = new ModelRenderer(this, 1, 33);
            Basket11.addBox(-2F, 11F, 2F, 4, 1, 1);
            Basket11.setRotationPoint(0F, 0F, 0F);
            Basket11.setTextureSize(64, 64);
            setRotation(Basket11, 0F, 0F, 0F);

            BasketE1 = new ModelRenderer(this, 1, 47);
            BasketE1.addBox(2F, 1F, 1F, 1, 12, 1);
            BasketE1.setRotationPoint(0F, 0F, 0F);
            BasketE1.setTextureSize(64, 64);
            setRotation(BasketE1, 0.413643F, 0F, 0F);

            BasketE2 = new ModelRenderer(this, 1, 47);
            BasketE2.addBox(-3F, 1F, 1F, 1, 12, 1);
            BasketE2.setRotationPoint(0F, 0F, 0F);
            BasketE2.setTextureSize(64, 64);
            setRotation(BasketE2, 0.413643F, 0F, 0F);
        }

        @Override
        public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
            setRotationAngles(f, f1, f2, f3, f4, f5, entity);
            bipedHead.render(f5);
            bipedHeadwear.render(f5);
            bipedBody.render(f5);
            bipedRightArm.render(f5);
            bipedLeftArm.render(f5);
            bipedRightLeg.render(f5);
            bipedLeftLeg.render(f5);
            Log1.render(f5);
            Log2.render(f5);
            Log3.render(f5);
            Basket1.render(f5);
            Basket2.render(f5);
            Basket3.render(f5);
            Basket4.render(f5);
            Basket5.render(f5);
            Basket6.render(f5);
            Basket7.render(f5);
            Basket8.render(f5);
            Basket9.render(f5);
            Basket10.render(f5);
            Basket11.render(f5);
            BasketE1.render(f5);
            BasketE2.render(f5);
        }

        @Override
        public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
            bipedHead.rotateAngleY = f3 / 57.29578F;
            bipedHead.rotateAngleX = f4 / 57.29578F;
            bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
            bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
            bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
            bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1;
            bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 2.0F * f1 * 0.5F;
            bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
            bipedRightArm.rotateAngleY = 0F;
            bipedLeftArm.rotateAngleY = 0F;

            if (onGround > -9990F) {
                float f6 = onGround;
                bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * 3.141593F * 2.0F) * 0.2F;
                Log1.rotateAngleY = bipedBody.rotateAngleY;
                Log2.rotateAngleY = bipedBody.rotateAngleY;
                Log3.rotateAngleY = bipedBody.rotateAngleY;
                Basket1.rotateAngleY = bipedBody.rotateAngleY;
                Basket2.rotateAngleY = bipedBody.rotateAngleY;
                Basket3.rotateAngleY = bipedBody.rotateAngleY;
                Basket4.rotateAngleY = bipedBody.rotateAngleY;
                Basket5.rotateAngleY = bipedBody.rotateAngleY;
                Basket6.rotateAngleY = bipedBody.rotateAngleY;
                Basket7.rotateAngleY = bipedBody.rotateAngleY;
                Basket8.rotateAngleY = bipedBody.rotateAngleY;
                Basket9.rotateAngleY = bipedBody.rotateAngleY;
                Basket10.rotateAngleY = bipedBody.rotateAngleY;
                Basket11.rotateAngleY = bipedBody.rotateAngleY;
                BasketE1.rotateAngleY = bipedBody.rotateAngleY;
                BasketE2.rotateAngleY = bipedBody.rotateAngleY;

                bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
                bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
                bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
                bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5F;
                bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
                bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
                bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
                f6 = 1.0F - onGround;
                f6 *= f6;
                f6 *= f6;
                f6 = 1.0F - f6;
                float f8 = MathHelper.sin(f6 * 3.141593F);
                float f10 = MathHelper.sin(onGround * 3.141593F) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
                bipedRightArm.rotateAngleX -= f8 * 1.2D + f10;
                bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
                bipedRightArm.rotateAngleZ = MathHelper.sin(onGround * 3.141593F) * -0.4F;
                bipedLeftArm.rotateAngleZ = 0F;
            }

            bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
            bipedLeftArm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
            bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;
        }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
