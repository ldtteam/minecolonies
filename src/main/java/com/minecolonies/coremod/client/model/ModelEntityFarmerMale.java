package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityFarmerMale extends BipedModel
{
    RendererModel boxBottom;
    RendererModel boxFront;
    RendererModel seeds;
    RendererModel boxLeft;
    RendererModel strapLeft;
    RendererModel boxRight;
    RendererModel strapRight;
    RendererModel boxBack;
    RendererModel hatStrap;
    RendererModel hatFrillBottom;
    RendererModel hatBottom;
    RendererModel hatTop;
    RendererModel hatFrillBack;
    RendererModel hatFrillRight;
    RendererModel hatFrillLeft;
    RendererModel hatFrillFront;

    public ModelEntityFarmerMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);
        bipedLeftLeg.mirror = false;

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        boxBottom = new RendererModel(this, 19, 50);
        boxBottom.addBox(-3F, 0F, -2F, 6, 1, 3);
        boxBottom.setRotationPoint(0F, 9F, -4F);
        boxBottom.setTextureSize(128, 64);
        boxBottom.mirror = true;
        setRotation(boxBottom, 0F, 0F, 0F);

        boxFront = new RendererModel(this, 21, 55);
        boxFront.addBox(-3F, -2F, -3F, 6, 3, 1);
        boxFront.setRotationPoint(0F, 9F, -4F);
        boxFront.setTextureSize(128, 64);
        boxFront.mirror = true;
        setRotation(boxFront, 0F, 0F, 0F);

        seeds = new RendererModel(this, 19, 45);
        seeds.addBox(-3F, -2F, -2F, 6, 1, 3);
        seeds.setRotationPoint(0F, 9F, -4F);
        seeds.setTextureSize(128, 64);
        seeds.mirror = true;
        setRotation(seeds, 0F, 0F, 0F);

        boxLeft = new RendererModel(this, 42, 43);
        boxLeft.addBox(3F, -2F, -3F, 1, 3, 6);
        boxLeft.setRotationPoint(0F, 9F, -4F);
        boxLeft.setTextureSize(128, 64);
        boxLeft.mirror = true;
        setRotation(boxLeft, 0F, 0F, 0F);

        strapLeft = new RendererModel(this, 32, 0);
        strapLeft.addBox(0F, 0F, -4F, 1, 1, 8);
        strapLeft.setRotationPoint(3F, 4F, -4F);
        strapLeft.setTextureSize(128, 64);
        strapLeft.mirror = true;
        setRotation(strapLeft, 1.047198F, 0F, 0F);

        boxRight = new RendererModel(this, 0, 43);
        boxRight.addBox(-4F, -2F, -3F, 1, 3, 6);
        boxRight.setRotationPoint(0F, 9F, -4F);
        boxRight.setTextureSize(128, 64);
        boxRight.mirror = true;
        setRotation(boxRight, 0F, 0F, 0F);

        strapRight = new RendererModel(this, 32, 0);
        strapRight.addBox(0F, 0F, -4F, 1, 1, 8);
        strapRight.setRotationPoint(-4F, 4F, -4F);
        strapRight.setTextureSize(128, 64);
        strapRight.mirror = true;
        setRotation(strapRight, 1.047198F, 0F, 0F);

        boxBack = new RendererModel(this, 21, 40);
        boxBack.addBox(-3F, -2F, -3F, 6, 3, 1);
        boxBack.setRotationPoint(0F, 9F, 0F);
        boxBack.setTextureSize(128, 64);
        boxBack.mirror = true;
        setRotation(boxBack, 0F, 0F, 0F);

        hatBottom = new RendererModel(this, 57, 11);
        hatBottom.addBox(-5F, -9.8F, -6F, 10, 3, 9);
        hatBottom.setRotationPoint(0F, 0F, 0F);
        hatBottom.setTextureSize(128, 64);
        hatBottom.mirror = true;
        setRotation(hatBottom, -0.2094395F, 0F, 0F);

        hatStrap = new RendererModel(this, 98, 14);
        hatStrap.addBox(-4.5F, -6.7F, -2.7F, 9, 8, 1);
        hatStrap.setRotationPoint(0F, 0F, 0F);
        hatStrap.setTextureSize(128, 64);
        hatStrap.mirror = true;
        setRotation(hatStrap, -0.3490659F, 0F, 0F);

        hatFrillBottom = new RendererModel(this, 57, 44);
        hatFrillBottom.addBox(-7.5F, -6.7F, -8.5F, 15, 1, 14);
        hatFrillBottom.setRotationPoint(0F, 0F, 0F);
        hatFrillBottom.setTextureSize(128, 64);
        hatFrillBottom.mirror = true;
        setRotation(hatFrillBottom, 0F, 0F, 0F);

        hatTop = new RendererModel(this, 60, 2);
        hatTop.addBox(-4.5F, -10.5F, -5F, 9, 1, 7);
        hatTop.setRotationPoint(0F, 0F, 0F);
        hatTop.setTextureSize(128, 64);
        hatTop.mirror = true;
        setRotation(hatTop, 0F, 0F, 0F);

        hatFrillBack = new RendererModel(this, 87, 40);
        hatFrillBack.addBox(-6.5F, -7.7F, 4.5F, 13, 1, 1);
        hatFrillBack.setRotationPoint(0F, 0F, 0F);
        hatFrillBack.setTextureSize(128, 64);
        hatFrillBack.mirror = true;
        setRotation(hatFrillBack, 0F, 0F, 0F);

        hatFrillRight = new RendererModel(this, 88, 24);
        hatFrillRight.addBox(-7.5F, -7.7F, -8.5F, 1, 1, 14);
        hatFrillRight.setRotationPoint(0F, 0F, 0F);
        hatFrillRight.setTextureSize(128, 64);
        hatFrillRight.mirror = true;
        setRotation(hatFrillRight, 0F, 0F, 0F);

        hatFrillLeft = new RendererModel(this, 57, 24);
        hatFrillLeft.addBox(6.5F, -7.7F, -8.5F, 1, 1, 14);
        hatFrillLeft.setRotationPoint(0F, 0F, 0F);
        hatFrillLeft.setTextureSize(128, 64);
        hatFrillLeft.mirror = true;
        setRotation(hatFrillLeft, 0F, 0F, 0F);

        hatFrillFront = new RendererModel(this, 57, 40);
        hatFrillFront.addBox(-6.5F, -7.7F, -8.5F, 13, 1, 1);
        hatFrillFront.setRotationPoint(0F, 0F, 0F);
        hatFrillFront.setTextureSize(128, 64);
        hatFrillFront.mirror = true;
        setRotation(hatFrillFront, 0F, 0F, 0F);

        bipedBody.addChild(boxBottom);
        bipedBody.addChild(boxBack);
        bipedBody.addChild(boxFront);
        bipedBody.addChild(boxLeft);
        bipedBody.addChild(boxRight);
        bipedBody.addChild(seeds);

        bipedBody.addChild(strapLeft);
        bipedBody.addChild(strapRight);

        bipedHead.addChild(hatStrap);

        bipedHead.addChild(hatBottom);
        hatBottom.addChild(hatTop);
        hatBottom.addChild(hatFrillBottom);
        hatFrillBottom.addChild(hatFrillBack);
        hatFrillBottom.addChild(hatFrillFront);
        hatFrillBottom.addChild(hatFrillLeft);
        hatFrillBottom.addChild(hatFrillRight);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(final LivingEntity entityIn,
                                   final float limbSwing,
                                   final float limbSwingAmount,
                                   final float ageInTicks,
                                   final float netHeadYaw,
                                   final float headPitch,
                                   final float scaleFactor)
    {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }
}
