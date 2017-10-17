package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleCitizen extends ModelBiped
{
    ModelRenderer breast;
    ModelRenderer hair;
    ModelRenderer dressPart1;
    ModelRenderer dressPart2;
    ModelRenderer dressPart3;

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

        breast = new ModelRenderer(this, 0, 33);
        breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(64, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair = new ModelRenderer(this, 46, 17);
        hair.addBox(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        hair.setRotationPoint(0F, 0F, 1F);
        hair.setTextureSize(64, 64);
        setRotation(hair, 0F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 26, 46);
        dressPart1.addBox(-5F, 2F, -4F, 10, 9, 9);
        dressPart1.setRotationPoint(0F, 11F, 0F);
        dressPart1.setTextureSize(64, 64);
        setRotation(dressPart1, 0F, 0F, 0F);

        dressPart2 = new ModelRenderer(this, 28, 38);
        dressPart2.addBox(-5F, 1F, -3F, 10, 1, 7);
        dressPart2.setRotationPoint(0F, 11F, 0F);
        dressPart2.setTextureSize(64, 64);
        setRotation(dressPart2, 0F, 0F, 0F);

        dressPart3 = new ModelRenderer(this, 32, 32);
        dressPart3.addBox(-4F, 0F, -2F, 8, 1, 5);
        dressPart3.setRotationPoint(0F, 11F, 0F);
        dressPart3.setTextureSize(64, 64);
        setRotation(dressPart3, 0F, 0F, 0F);
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
        bipedHeadwear.render(scaleFactor);
        bipedBody.render(scaleFactor);
        bipedLeftArm.render(scaleFactor);
        bipedRightArm.render(scaleFactor);
        bipedRightLeg.render(scaleFactor);
        bipedLeftLeg.render(scaleFactor);
        breast.render(scaleFactor);
        hair.render(scaleFactor);
        dressPart1.render(scaleFactor);
        dressPart2.render(scaleFactor);
        dressPart3.render(scaleFactor);
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
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
        hair.rotateAngleY = bipedHead.rotateAngleY;
        hair.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos((float) (limbSwing * 0.6662F + Math.PI)) * 2.0F * limbSwingAmount * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;

        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.53F * limbSwingAmount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos((float) (limbSwing * 0.6662F + Math.PI)) * 0.53F * limbSwingAmount;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;

        bipedRightArm.rotateAngleY = 0.0F;
        bipedLeftArm.rotateAngleY = 0.0F;

        // free stand rotation
        bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

        if (swingProgress > -9990F)
        {
            float f6 = swingProgress;
            bipedBody.rotateAngleY = MathHelper.sin((float) (MathHelper.sqrt(f6) * Math.PI * 2.0F)) * 0.2F;
            breast.rotateAngleY = MathHelper.sin((float) (MathHelper.sqrt(f6) * Math.PI * 2.0F)) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
            //noinspection SuspiciousNameCombination
            bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;//TODO If model looks funny try changing to rotateAngleX
            f6 = 1.0F - swingProgress;
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