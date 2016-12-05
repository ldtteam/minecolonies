package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleAristocrat extends ModelBiped
{
    ModelRenderer breast;
    ModelRenderer leftArm2;
    ModelRenderer leftArm1;
    ModelRenderer hair1;
    ModelRenderer hair2;
    ModelRenderer hair3;
    ModelRenderer umbrellaHand;
    ModelRenderer umbrella;
    ModelRenderer dressPart1;
    ModelRenderer dressPart2;
    ModelRenderer dressPart3;
    ModelRenderer dressPart5;
    ModelRenderer dressPart6;
    ModelRenderer dressPart7;
    ModelRenderer dressPart8;
    ModelRenderer dressPart9;
    ModelRenderer dressPart10;
    ModelRenderer dressPart11;
    ModelRenderer dressPart12;
    ModelRenderer dressPart13;

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

        breast = new ModelRenderer(this, 0, 33);
        breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(64, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair1 = new ModelRenderer(this, 32, 0);
        hair1.addBox(-3F, -3F, -3F, 6, 3, 6);
        hair1.setRotationPoint(0F, -8F, 1F);
        hair1.setTextureSize(64, 64);
        setRotation(hair1, 0F, 0F, 0F);

        hair2 = new ModelRenderer(this, 56, 0);
        hair2.addBox(-1F, -1F, -1F, 2, 1, 2);
        hair2.setRotationPoint(0F, -11F, 1F);
        hair2.setTextureSize(64, 64);
        setRotation(hair2, 0F, 0F, 0F);

        hair3 = new ModelRenderer(this, 32, 10);
        hair3.addBox(-2F, -1F, -2F, 4, 2, 4);
        hair3.setRotationPoint(0F, -13F, 1F);
        hair3.setTextureSize(64, 64);
        setRotation(hair3, 0F, 0F, 0F);

        umbrellaHand = new ModelRenderer(this, 60, 10);
        umbrellaHand.addBox(0F, -2F, 0F, 1, 21, 1);
        umbrellaHand.setRotationPoint(5F, 6F, -5F);
        umbrellaHand.setTextureSize(64, 64);
        setRotation(umbrellaHand, 2.565634F, 0F, 0F);

        umbrella = new ModelRenderer(this, 0, 54);
        umbrella.addBox(-4F, 0F, -4F, 9, 1, 9);
        umbrella.setRotationPoint(5F, -10F, 5F);
        umbrella.setTextureSize(64, 64);
        setRotation(umbrella, -0.4712389F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 18, 33);
        dressPart1.addBox(-6F, 0F, -6F, 12, 6, 11, 0.01F);
        dressPart1.setRotationPoint(0F, 16F, 1F);
        dressPart1.setTextureSize(64, 64);
        setRotation(dressPart1, 0F, 0.418879F, 0F);

        dressPart2 = new ModelRenderer(this, 18, 33);
        dressPart2.addBox(-6F, 0F, -5F, 12, 6, 11);
        dressPart2.setRotationPoint(0F, 16F, 0F);
        dressPart2.setTextureSize(64, 64);
        setRotation(dressPart2, 0F, -0.3839724F, 0F);

        dressPart3 = new ModelRenderer(this, 30, 50);
        dressPart3.addBox(-5F, 0F, -3F, 10, 4, 7, 0.01F);
        dressPart3.setRotationPoint(0F, 9F, 0F);
        dressPart3.setTextureSize(64, 64);
        setRotation(dressPart3, 0F, -0.3316126F, 0F);

        dressPart5 = new ModelRenderer(this, 30, 50);
        dressPart5.addBox(-6F, 0F, -3F, 10, 4, 7);
        dressPart5.setRotationPoint(0F, 9F, 0F);
        dressPart5.setTextureSize(64, 64);
        setRotation(dressPart5, 0F, 0.4363323F, 0F);

        dressPart6 = new ModelRenderer(this, 0, 40);
        dressPart6.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart6.setRotationPoint(2F, 18F, -4F);
        dressPart6.setTextureSize(64, 64);
        setRotation(dressPart6, 2.503836F, 0.3210144F, -0.3592861F);

        dressPart7 = new ModelRenderer(this, 0, 40);
        dressPart7.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart7.setRotationPoint(-2F, 18F, -4F);
        dressPart7.setTextureSize(64, 64);
        setRotation(dressPart7, 2.070064F, 0.797036F, -0.6991393F);

        dressPart8 = new ModelRenderer(this, 0, 40);
        dressPart8.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart8.setRotationPoint(-5F, 18F, -1F);
        dressPart8.setTextureSize(64, 64);
        setRotation(dressPart8, 2.740167F, 0.6363323F, 0.4537856F);

        dressPart9 = new ModelRenderer(this, 0, 40);
        dressPart9.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart9.setRotationPoint(-5F, 18F, 2F);
        dressPart9.setTextureSize(64, 64);
        setRotation(dressPart9, 3.010485F, 1.313108F, 0.2890419F);

        dressPart10 = new ModelRenderer(this, 0, 40);
        dressPart10.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart10.setRotationPoint(-2F, 18F, 5F);
        dressPart10.setTextureSize(64, 64);
        setRotation(dressPart10, -2.563121F, 0.3303337F, 0.4129171F);

        dressPart11 = new ModelRenderer(this, 0, 40);
        dressPart11.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart11.setRotationPoint(2F, 18F, 5F);
        dressPart11.setTextureSize(64, 64);
        setRotation(dressPart11, -2.458432F, -0.2216398F, -0.3303337F);

        dressPart12 = new ModelRenderer(this, 0, 40);
        dressPart12.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart12.setRotationPoint(4F, 18F, 2F);
        dressPart12.setTextureSize(64, 64);
        setRotation(dressPart12, -2.852551F, 0.5367922F, -0.2064585F);

        dressPart13 = new ModelRenderer(this, 0, 40);
        dressPart13.addBox(-2F, 0F, -2F, 4, 7, 4);
        dressPart13.setRotationPoint(4F, 18F, -1F);
        dressPart13.setTextureSize(64, 64);
        setRotation(dressPart13, 2.778193F, -0.5512723F, -0.2477502F);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void render(
                        final Entity entity,
                        final float limbSwing,
                        final float limbSwingAmount,
                        final float ageInTicks,
                        final float netHeadYaw,
                        final float headPitch,
                        final float scaleFactor)
    {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        bipedHead.render(scaleFactor);
        bipedBody.render(scaleFactor);
        bipedRightArm.render(scaleFactor);
        leftArm2.render(scaleFactor);
        leftArm1.render(scaleFactor);
        bipedRightLeg.render(scaleFactor);
        bipedLeftLeg.render(scaleFactor);
        breast.render(scaleFactor);
        hair1.render(scaleFactor);
        hair2.render(scaleFactor);
        hair3.render(scaleFactor);
        umbrellaHand.render(scaleFactor);
        umbrella.render(scaleFactor);
        dressPart1.render(scaleFactor);
        dressPart2.render(scaleFactor);
        dressPart3.render(scaleFactor);
        dressPart5.render(scaleFactor);
        dressPart6.render(scaleFactor);
        dressPart7.render(scaleFactor);
        dressPart8.render(scaleFactor);
        dressPart9.render(scaleFactor);
        dressPart10.render(scaleFactor);
        dressPart11.render(scaleFactor);
        dressPart12.render(scaleFactor);
        dressPart13.render(scaleFactor);
    }

    @Override
    public void setRotationAngles(
                                   final float limbSwing,
                                   final float limbSwingAmount,
                                   final float ageInTicks,
                                   final float netHeadYaw,
                                   final float headPitch,
                                   final float scaleFactor,
                                   final Entity entityIn)
    {
        bipedHead.rotateAngleY = netHeadYaw / 57.29578F;
        bipedHead.rotateAngleX = headPitch / 57.29578F;
        hair1.rotateAngleY = bipedHead.rotateAngleY;
        hair1.rotateAngleX = bipedHead.rotateAngleX;
        hair2.rotateAngleY = bipedHead.rotateAngleY;
        hair2.rotateAngleX = bipedHead.rotateAngleX;
        hair3.rotateAngleY = bipedHead.rotateAngleY;
        hair3.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos((float) (limbSwing * 0.6662F + Math.PI)) * 2.0F * limbSwingAmount * 0.5F;

        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos((float) (limbSwing * 0.6662F + Math.PI)) * 1.1F * limbSwingAmount;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;

        bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

        if (swingProgress > -9990F)
        {
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;

            float f6 = 1.0F - swingProgress;
            f6 *= f6;
            f6 *= f6;
            f6 = 1.0F - f6;
            final float f7 = MathHelper.sin((float) (f6 * Math.PI));
            final float f8 = MathHelper.sin((float) (swingProgress * Math.PI)) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
            bipedRightArm.rotateAngleX -= f7 * 1.2D + f8;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
            bipedRightArm.rotateAngleZ = MathHelper.sin((float) (swingProgress * Math.PI)) * -0.4F;
        }
    }
}