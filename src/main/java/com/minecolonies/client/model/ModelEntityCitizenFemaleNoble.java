package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEntityCitizenFemaleNoble extends ModelBiped
{
    // fields
    ModelRenderer Breast;
    ModelRenderer Hairs;
    ModelRenderer DressPart5;
    ModelRenderer DressPart4;
    ModelRenderer DressPart3;
    ModelRenderer DressPart2;
    ModelRenderer DressPart1;
    ModelRenderer Hat1;
    ModelRenderer Hat2;
    ModelRenderer Bag;
    ModelRenderer BagHand1;
    ModelRenderer BagHand2;

    public ModelEntityCitizenFemaleNoble()
    {
        this(0.0F);
    }

    public ModelEntityCitizenFemaleNoble(float f)
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(128, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, f + 0.5F);
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

        Breast = new ModelRenderer(this, 0, 33);
        Breast.addBox(-3F, 0F, -2F, 8, 4, 3);
        Breast.setRotationPoint(-1F, 3F, 1F);
        Breast.setTextureSize(128, 64);
        setRotation(Breast, -0.5235988F, 0F, 0F);

        Hairs = new ModelRenderer(this, 46, 17);
        Hairs.addBox(-4F, 0F, 3F, 8, 7, 1, f + 0.5F);
        Hairs.setRotationPoint(0F, 0F, 1F);
        Hairs.setTextureSize(128, 64);
        setRotation(Hairs, 0F, 0F, 0F);

        DressPart5 = new ModelRenderer(this, 65, 48);
        DressPart5.addBox(-8F, 9F, -6F, 16, 3, 13);
        DressPart5.setRotationPoint(0F, 11F, 0F);
        DressPart5.setTextureSize(128, 64);
        setRotation(DressPart5, 0F, 0F, 0F);

        DressPart4 = new ModelRenderer(this, 65, 34);
        DressPart4.addBox(-7F, 6F, -5F, 14, 3, 11);
        DressPart4.setRotationPoint(0F, 11F, 0F);
        DressPart4.setTextureSize(128, 64);
        setRotation(DressPart4, 0F, 0F, 0F);

        DressPart3 = new ModelRenderer(this, 65, 23);
        DressPart3.addBox(-6F, 4F, -4F, 12, 2, 9);
        DressPart3.setRotationPoint(0F, 11F, 0F);
        DressPart3.setTextureSize(128, 64);
        setRotation(DressPart3, 0F, 0F, 0F);

        DressPart2 = new ModelRenderer(this, 65, 14);
        DressPart2.addBox(-5F, 2F, -3F, 10, 2, 7);
        DressPart2.setRotationPoint(0F, 11F, 0F);
        DressPart2.setTextureSize(128, 64);
        setRotation(DressPart2, 0F, 0F, 0F);

        DressPart1 = new ModelRenderer(this, 65, 7);
        DressPart1.addBox(-4F, 0F, -2F, 8, 2, 5);
        DressPart1.setRotationPoint(0F, 11F, 0F);
        DressPart1.setTextureSize(128, 64);
        setRotation(DressPart1, 0F, 0F, 0F);

        Hat1 = new ModelRenderer(this, 0, 48);
        Hat1.addBox(-5F, -8F, -5F, 10, 2, 10, f + 0.1F);
        Hat1.setRotationPoint(0F, 0F, 1F);
        Hat1.setTextureSize(128, 64);
        setRotation(Hat1, 0F, 0F, 0F);

        Hat2 = new ModelRenderer(this, 0, 40);
        Hat2.addBox(-3F, -10F, -3F, 6, 2, 6, f + 0.3F);
        Hat2.setRotationPoint(0F, 0F, 1F);
        Hat2.setTextureSize(128, 64);
        setRotation(Hat2, 0F, 0F, 0F);

        Bag = new ModelRenderer(this, 24, 32);
        Bag.addBox(0F, 6F, -3F, 1, 4, 7);
        Bag.setRotationPoint(4F, 0F, 0F);
        Bag.setTextureSize(128, 64);
        setRotation(Bag, 0F, 0F, 0F);

        BagHand1 = new ModelRenderer(this, 40, 32);
        BagHand1.addBox(0F, 0F, 0F, 1, 7, 0);
        BagHand1.setRotationPoint(4F, 0F, 0F);
        BagHand1.setTextureSize(128, 64);
        setRotation(BagHand1, -0.4014257F, 0F, 0F);

        BagHand2 = new ModelRenderer(this, 40, 32);
        BagHand2.addBox(0F, 0F, 1F, 1, 7, 0);
        BagHand2.setRotationPoint(4F, 0F, 0F);
        BagHand2.setTextureSize(128, 64);
        setRotation(BagHand2, 0.4014257F, 0F, 0F);
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
        DressPart5.render(f5);
        DressPart4.render(f5);
        DressPart3.render(f5);
        DressPart2.render(f5);
        DressPart1.render(f5);
        Hat1.render(f5);
        Hat2.render(f5);
        Bag.render(f5);
        BagHand1.render(f5);
        BagHand2.render(f5);
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
        Hat1.rotateAngleY = bipedHead.rotateAngleY;
        Hat1.rotateAngleX = bipedHead.rotateAngleX;
        Hat2.rotateAngleY = bipedHead.rotateAngleY;
        Hat2.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 2.0F * f1 * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 0.73F * f1;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 0.73F * f1;

        bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;

    }

    public ModelRenderer toolArm()
    {
        return bipedRightArm;
    }

}