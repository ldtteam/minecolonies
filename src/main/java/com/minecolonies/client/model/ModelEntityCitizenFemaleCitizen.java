package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEntityCitizenFemaleCitizen extends ModelBiped
{
    // fields
    ModelRenderer Breast;
    ModelRenderer Hairs;
    ModelRenderer DressPart3;
    ModelRenderer DressPart2;
    ModelRenderer DressPart1;

    public ModelEntityCitizenFemaleCitizen()
    {

        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 1F);
        bipedHeadwear.setTextureSize(64, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 12, 17);
        bipedBody.addBox(-4F, 0F, -4F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 3F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 34, 17);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(0F, 0F, -1F, 3, 12, 3);
        bipedLeftArm.setRotationPoint(4F, 0F, 0F);
        bipedLeftArm.setTextureSize(64, 64);
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 34, 17);
        bipedRightArm.addBox(-2F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-5F, 0F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 17);
        bipedRightLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 17);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        Breast = new ModelRenderer(this, 0, 33);
        Breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        Breast.setRotationPoint(-1F, 3F, 1F);
        Breast.setTextureSize(64, 64);
        setRotation(Breast, -0.5235988F, 0F, 0F);

        Hairs = new ModelRenderer(this, 46, 17);
        Hairs.addBox(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        Hairs.setRotationPoint(0F, 0F, 1F);
        Hairs.setTextureSize(64, 64);
        setRotation(Hairs, 0F, 0F, 0F);

        DressPart3 = new ModelRenderer(this, 26, 46);
        DressPart3.addBox(-5F, 2F, -4F, 10, 9, 9);
        DressPart3.setRotationPoint(0F, 11F, 0F);
        DressPart3.setTextureSize(64, 64);
        setRotation(DressPart3, 0F, 0F, 0F);

        DressPart2 = new ModelRenderer(this, 28, 38);
        DressPart2.addBox(-5F, 1F, -3F, 10, 1, 7);
        DressPart2.setRotationPoint(0F, 11F, 0F);
        DressPart2.setTextureSize(64, 64);
        setRotation(DressPart2, 0F, 0F, 0F);

        DressPart1 = new ModelRenderer(this, 32, 32);
        DressPart1.addBox(-4F, 0F, -2F, 8, 1, 5);
        DressPart1.setRotationPoint(0F, 11F, 0F);
        DressPart1.setTextureSize(64, 64);
        setRotation(DressPart1, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        bipedHead.render(f5);
        bipedHeadwear.render(f5);
        bipedBody.render(f5);
        bipedLeftArm.render(f5);
        bipedRightArm.render(f5);
        bipedRightLeg.render(f5);
        bipedLeftLeg.render(f5);
        Breast.render(f5);
        Hairs.render(f5);
        DressPart3.render(f5);
        DressPart2.render(f5);
        DressPart1.render(f5);
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
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
        Hairs.rotateAngleY = bipedHead.rotateAngleY;
        Hairs.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos((float) (f * 0.6662F + Math.PI)) * 2.0F * f1 * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;

        bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 0.53F * f1;
        bipedLeftLeg.rotateAngleX = MathHelper.cos((float) (f * 0.6662F + Math.PI)) * 0.53F * f1;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;

        bipedRightArm.rotateAngleY = 0.0F;
        bipedLeftArm.rotateAngleY = 0.0F;

        // free stand rotation
        bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;

        if(onGround > -9990F)
        {
            float f6 = onGround;
            bipedBody.rotateAngleY = MathHelper.sin((float) (MathHelper.sqrt_float(f6) * Math.PI * 2.0F)) * 0.2F;
            Breast.rotateAngleY = MathHelper.sin((float) (MathHelper.sqrt_float(f6) * Math.PI * 2.0F)) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            // LeftArm.rotationPointX = MathHelper.cos(Body.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;//TODO rotateAngleX? MW: No idea, if models dont look weird, leave it
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

    public ModelRenderer toolArm()
    {
        return bipedRightArm;
    }

}