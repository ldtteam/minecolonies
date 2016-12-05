package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleNoble extends ModelBiped
{
    ModelRenderer breast;
    ModelRenderer hair;
    ModelRenderer dressPart1;
    ModelRenderer dressPart2;
    ModelRenderer dressPart3;
    ModelRenderer dressPart4;
    ModelRenderer dressPart5;
    ModelRenderer hat1;
    ModelRenderer hat2;
    ModelRenderer bag;
    ModelRenderer bagHand1;
    ModelRenderer bagHand2;

    public ModelEntityCitizenFemaleNoble()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(128, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 1F);
        bipedHeadwear.setTextureSize(128, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 12, 17);
        bipedBody.addBox(-4F, 0F, -4F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 3F);
        bipedBody.setTextureSize(128, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 34, 17);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(0F, 0F, -1F, 3, 12, 3);
        bipedLeftArm.setRotationPoint(4F, 0F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        setRotation(bipedLeftArm, 0F, 0F, -0.1396263F);

        bipedRightArm = new ModelRenderer(this, 34, 17);
        bipedRightArm.addBox(-2F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-5F, 0F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 17);
        bipedRightLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(128, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 17);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(128, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        breast = new ModelRenderer(this, 0, 33);
        breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(128, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair = new ModelRenderer(this, 46, 17);
        hair.addBox(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        hair.setRotationPoint(0F, 0F, 1F);
        hair.setTextureSize(128, 64);
        setRotation(hair, 0F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 65, 48);
        dressPart1.addBox(-8F, 9F, -6F, 16, 3, 13);
        dressPart1.setRotationPoint(0F, 11F, 0F);
        dressPart1.setTextureSize(128, 64);
        setRotation(dressPart1, 0F, 0F, 0F);

        dressPart2 = new ModelRenderer(this, 65, 34);
        dressPart2.addBox(-7F, 6F, -5F, 14, 3, 11);
        dressPart2.setRotationPoint(0F, 11F, 0F);
        dressPart2.setTextureSize(128, 64);
        setRotation(dressPart2, 0F, 0F, 0F);

        dressPart3 = new ModelRenderer(this, 65, 23);
        dressPart3.addBox(-6F, 4F, -4F, 12, 2, 9);
        dressPart3.setRotationPoint(0F, 11F, 0F);
        dressPart3.setTextureSize(128, 64);
        setRotation(dressPart3, 0F, 0F, 0F);

        dressPart4 = new ModelRenderer(this, 65, 14);
        dressPart4.addBox(-5F, 2F, -3F, 10, 2, 7);
        dressPart4.setRotationPoint(0F, 11F, 0F);
        dressPart4.setTextureSize(128, 64);
        setRotation(dressPart4, 0F, 0F, 0F);

        dressPart5 = new ModelRenderer(this, 65, 7);
        dressPart5.addBox(-4F, 0F, -2F, 8, 2, 5);
        dressPart5.setRotationPoint(0F, 11F, 0F);
        dressPart5.setTextureSize(128, 64);
        setRotation(dressPart5, 0F, 0F, 0F);

        hat1 = new ModelRenderer(this, 0, 48);
        hat1.addBox(-5F, -8F, -5F, 10, 2, 10, 0.1F);
        hat1.setRotationPoint(0F, 0F, 1F);
        hat1.setTextureSize(128, 64);
        setRotation(hat1, 0F, 0F, 0F);

        hat2 = new ModelRenderer(this, 0, 40);
        hat2.addBox(-3F, -10F, -3F, 6, 2, 6, 0.3F);
        hat2.setRotationPoint(0F, 0F, 1F);
        hat2.setTextureSize(128, 64);
        setRotation(hat2, 0F, 0F, 0F);

        bag = new ModelRenderer(this, 24, 32);
        bag.addBox(0F, 6F, -3F, 1, 4, 7);
        bag.setRotationPoint(4F, 0F, 0F);
        bag.setTextureSize(128, 64);
        setRotation(bag, 0F, 0F, 0F);

        bagHand1 = new ModelRenderer(this, 40, 32);
        bagHand1.addBox(0F, 0F, 0F, 1, 7, 0);
        bagHand1.setRotationPoint(4F, 0F, 0F);
        bagHand1.setTextureSize(128, 64);
        setRotation(bagHand1, -0.4014257F, 0F, 0F);

        bagHand2 = new ModelRenderer(this, 40, 32);
        bagHand2.addBox(0F, 0F, 1F, 1, 7, 0);
        bagHand2.setRotationPoint(4F, 0F, 0F);
        bagHand2.setTextureSize(128, 64);
        setRotation(bagHand2, 0.4014257F, 0F, 0F);
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
        dressPart4.render(scaleFactor);
        dressPart5.render(scaleFactor);
        hat1.render(scaleFactor);
        hat2.render(scaleFactor);
        bag.render(scaleFactor);
        bagHand1.render(scaleFactor);
        bagHand2.render(scaleFactor);
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
        hat1.rotateAngleY = bipedHead.rotateAngleY;
        hat1.rotateAngleX = bipedHead.rotateAngleX;
        hat2.rotateAngleY = bipedHead.rotateAngleY;
        hat2.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 2.0F * limbSwingAmount * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.73F * limbSwingAmount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 0.73F * limbSwingAmount;

        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}