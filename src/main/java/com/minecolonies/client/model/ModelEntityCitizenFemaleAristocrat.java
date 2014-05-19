package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEntityCitizenFemaleAristocrat extends ModelBiped
{
    // fields
    ModelRenderer Breast;

    ModelRenderer leftArm2;
    ModelRenderer leftArm1;
    ModelRenderer Hairs1;
    ModelRenderer Hairs2;
    ModelRenderer Hairs3;
    ModelRenderer UmbrellaHand;
    ModelRenderer Umbrella;
    ModelRenderer Dresspart1;
    ModelRenderer Dresspart2;
    ModelRenderer Dresspart3;
    ModelRenderer Dresspart4;
    ModelRenderer Dresspart21;
    ModelRenderer Dresspart22;
    ModelRenderer Dresspart23;
    ModelRenderer Dresspart24;
    ModelRenderer Dresspart25;
    ModelRenderer Dresspart26;
    ModelRenderer Dresspart27;
    ModelRenderer Dresspart28;

    public ModelEntityCitizenFemaleAristocrat()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 12, 17);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 1F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 34, 17);
        bipedRightArm.addBox(-3F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-4F, 0F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        leftArm2 = new ModelRenderer(this, 46, 22);
        leftArm2.addBox(-1F, -1F, -1F, 3, 7, 3);
        leftArm2.setRotationPoint(5F, 6F, 1F);
        leftArm2.setTextureSize(64, 64);
        setRotation(leftArm2, -1.570796F, 0F, 0F);

        leftArm1 = new ModelRenderer(this, 34, 17);
        leftArm1.addBox(0F, 0F, -1F, 3, 6, 3);
        leftArm1.mirror = true;
        leftArm1.setRotationPoint(4F, 0F, 0F);
        leftArm1.setTextureSize(64, 64);
        setRotation(leftArm1, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 17);
        bipedRightLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 17);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        Breast = new ModelRenderer(this, 0, 33);
        Breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        Breast.setRotationPoint(-1F, 3F, 1F);
        Breast.setTextureSize(64, 64);
        setRotation(Breast, -0.5235988F, 0F, 0F);

        Hairs1 = new ModelRenderer(this, 32, 0);
        Hairs1.addBox(-3F, -3F, -3F, 6, 3, 6);
        Hairs1.setRotationPoint(0F, -8F, 1F);
        Hairs1.setTextureSize(64, 64);
        setRotation(Hairs1, 0F, 0F, 0F);

        Hairs2 = new ModelRenderer(this, 56, 0);
        Hairs2.addBox(-1F, -1F, -1F, 2, 1, 2);
        Hairs2.setRotationPoint(0F, -11F, 1F);
        Hairs2.setTextureSize(64, 64);
        setRotation(Hairs2, 0F, 0F, 0F);

        Hairs3 = new ModelRenderer(this, 32, 10);
        Hairs3.addBox(-2F, -1F, -2F, 4, 2, 4);
        Hairs3.setRotationPoint(0F, -13F, 1F);
        Hairs3.setTextureSize(64, 64);
        setRotation(Hairs3, 0F, 0F, 0F);

        UmbrellaHand = new ModelRenderer(this, 60, 10);
        UmbrellaHand.addBox(0F, -2F, 0F, 1, 21, 1);
        UmbrellaHand.setRotationPoint(5F, 6F, -5F);
        UmbrellaHand.setTextureSize(64, 64);
        setRotation(UmbrellaHand, 2.565634F, 0F, 0F);

		/*
         * Umbrella = new ModelRenderer(this, 0, 54); Umbrella.addBox(-4F, 0F,
		 * -4F, 9, 1, 9); Umbrella.setRotationPoint(5F, -10F, 5F);
		 * Umbrella.setTextureSize(64, 64); setRotation(Umbrella, -0.2839724F,
		 * 0.8108652F, -0.1792527F);
		 */
        Umbrella = new ModelRenderer(this, 0, 54);
        Umbrella.addBox(-4F, 0F, -4F, 9, 1, 9);
        Umbrella.setRotationPoint(5F, -10F, 5F);
        Umbrella.setTextureSize(64, 64);
        setRotation(Umbrella, -0.4712389F, 0F, 0F);

        Dresspart1 = new ModelRenderer(this, 18, 33);
        Dresspart1.addBox(-6F, 0F, -6F, 12, 6, 11, 0.01F);
        Dresspart1.setRotationPoint(0F, 16F, 1F);
        Dresspart1.setTextureSize(64, 64);
        setRotation(Dresspart1, 0F, 0.418879F, 0F);

        Dresspart2 = new ModelRenderer(this, 18, 33);
        Dresspart2.addBox(-6F, 0F, -5F, 12, 6, 11);
        Dresspart2.setRotationPoint(0F, 16F, 0F);
        Dresspart2.setTextureSize(64, 64);
        setRotation(Dresspart2, 0F, -0.3839724F, 0F);

        Dresspart3 = new ModelRenderer(this, 30, 50);
        Dresspart3.addBox(-5F, 0F, -3F, 10, 4, 7, 0.01F);
        Dresspart3.setRotationPoint(0F, 9F, 0F);
        Dresspart3.setTextureSize(64, 64);
        setRotation(Dresspart3, 0F, -0.3316126F, 0F);

        Dresspart4 = new ModelRenderer(this, 30, 50);
        Dresspart4.addBox(-6F, 0F, -3F, 10, 4, 7);
        Dresspart4.setRotationPoint(0F, 9F, 0F);
        Dresspart4.setTextureSize(64, 64);
        setRotation(Dresspart4, 0F, 0.4363323F, 0F);

        Dresspart21 = new ModelRenderer(this, 0, 40);
        Dresspart21.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart21.setRotationPoint(2F, 18F, -4F);
        Dresspart21.setTextureSize(64, 64);
        setRotation(Dresspart21, 2.503836F, 0.3210144F, -0.3592861F);

        Dresspart22 = new ModelRenderer(this, 0, 40);
        Dresspart22.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart22.setRotationPoint(-2F, 18F, -4F);
        Dresspart22.setTextureSize(64, 64);
        setRotation(Dresspart22, 2.070064F, 0.797036F, -0.6991393F);

        Dresspart23 = new ModelRenderer(this, 0, 40);
        Dresspart23.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart23.setRotationPoint(-5F, 18F, -1F);
        Dresspart23.setTextureSize(64, 64);
        setRotation(Dresspart23, 2.740167F, 0.6363323F, 0.4537856F);

        Dresspart24 = new ModelRenderer(this, 0, 40);
        Dresspart24.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart24.setRotationPoint(-5F, 18F, 2F);
        Dresspart24.setTextureSize(64, 64);
        setRotation(Dresspart24, 3.010485F, 1.313108F, 0.2890419F);

        Dresspart25 = new ModelRenderer(this, 0, 40);
        Dresspart25.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart25.setRotationPoint(-2F, 18F, 5F);
        Dresspart25.setTextureSize(64, 64);
        setRotation(Dresspart25, -2.563121F, 0.3303337F, 0.4129171F);

        Dresspart26 = new ModelRenderer(this, 0, 40);
        Dresspart26.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart26.setRotationPoint(2F, 18F, 5F);
        Dresspart26.setTextureSize(64, 64);
        setRotation(Dresspart26, -2.458432F, -0.2216398F, -0.3303337F);

        Dresspart27 = new ModelRenderer(this, 0, 40);
        Dresspart27.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart27.setRotationPoint(4F, 18F, 2F);
        Dresspart27.setTextureSize(64, 64);
        setRotation(Dresspart27, -2.852551F, 0.5367922F, -0.2064585F);

        Dresspart28 = new ModelRenderer(this, 0, 40);
        Dresspart28.addBox(-2F, 0F, -2F, 4, 7, 4);
        Dresspart28.setRotationPoint(4F, 18F, -1F);
        Dresspart28.setTextureSize(64, 64);
        setRotation(Dresspart28, 2.778193F, -0.5512723F, -0.2477502F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        bipedHead.render(f5);
        bipedBody.render(f5);
        bipedRightArm.render(f5);
        leftArm2.render(f5);
        leftArm1.render(f5);
        bipedRightLeg.render(f5);
        bipedLeftLeg.render(f5);
        Breast.render(f5);
        Hairs1.render(f5);
        Hairs2.render(f5);
        Hairs3.render(f5);
        UmbrellaHand.render(f5);
        Umbrella.render(f5);
        Dresspart1.render(f5);
        Dresspart2.render(f5);
        Dresspart3.render(f5);
        Dresspart4.render(f5);
        Dresspart21.render(f5);
        Dresspart22.render(f5);
        Dresspart23.render(f5);
        Dresspart24.render(f5);
        Dresspart25.render(f5);
        Dresspart26.render(f5);
        Dresspart27.render(f5);
        Dresspart28.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        bipedHead.rotateAngleY = f3 / 57.29578F;
        bipedHead.rotateAngleX = f4 / 57.29578F;
        Hairs1.rotateAngleY = bipedHead.rotateAngleY;
        Hairs1.rotateAngleX = bipedHead.rotateAngleX;
        Hairs2.rotateAngleY = bipedHead.rotateAngleY;
        Hairs2.rotateAngleX = bipedHead.rotateAngleX;
        Hairs3.rotateAngleY = bipedHead.rotateAngleY;
        Hairs3.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos((float) (f * 0.6662F + Math.PI)) * 2.0F * f1 * 0.5F;

        bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.1F * f1;
        bipedLeftLeg.rotateAngleX = MathHelper.cos((float) (f * 0.6662F + Math.PI)) * 1.1F * f1;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;

        bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;

        if(onGround > -9990F)
        {
            float f6 = onGround;
            // Body.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) *
            // 3.141593F * 2.0F) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            // RightArm.rotationPointX = -MathHelper.cos(Body.rotateAngleY) *
            // 5F;
            // LeftArm.rotationPointZ = -MathHelper.sin(Body.rotateAngleY) * 5F;
            // LeftArm.rotationPointX = MathHelper.cos(Body.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            // LeftArm.rotateAngleY += Body.rotateAngleY;
            // LeftArm.rotateAngleX += Body.rotateAngleY;
            f6 = 1.0F - onGround;
            f6 *= f6;
            f6 *= f6;
            f6 = 1.0F - f6;
            float f7 = MathHelper.sin((float) (f6 * Math.PI));
            float f8 = MathHelper.sin((float) (onGround * Math.PI)) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
            bipedRightArm.rotateAngleX -= f7 * 1.2D + f8;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
            bipedRightArm.rotateAngleZ = MathHelper.sin((float) (onGround * Math.PI)) * -0.4F;
        }
    }

}