package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModelEntitySheepFarmerMale extends BipedModel
{
    public ModelEntitySheepFarmerMale()
    {
        RendererModel bagR;
        RendererModel bagL;
        RendererModel bagBack;
        RendererModel bagFront;
        RendererModel bagWheat;
        RendererModel bagBot;

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

        bagR = new RendererModel(this, 0, 34);
        bagR.addBox(3F, 0F, 3F, 1, 9, 3);
        bagR.setRotationPoint(0F, 0F, 0F);
        bagR.setTextureSize(128, 64);
        bagR.mirror = true;
        setRotation(bagR, 0F, 0F, 0F);

        bagL = new RendererModel(this, 1, 38);
        bagL.addBox(-4F, 0F, 3F, 1, 9, 3);
        bagL.setRotationPoint(0F, 0F, 0F);
        bagL.setTextureSize(128, 64);
        bagL.mirror = true;
        setRotation(bagL, 0F, 0F, 0F);

        bagBack = new RendererModel(this, 2, 34);
        bagBack.addBox(-3F, 0F, 2F, 6, 9, 1);
        bagBack.setRotationPoint(0F, 0F, 0F);
        bagBack.setTextureSize(128, 64);
        bagBack.mirror = true;
        setRotation(bagBack, 0F, 0F, 0F);

        bagFront = new RendererModel(this, 2, 39);
        bagFront.addBox(-3F, 1F, 6F, 6, 8, 1);
        bagFront.setRotationPoint(0F, 0F, 0F);
        bagFront.setTextureSize(128, 64);
        bagFront.mirror = true;
        setRotation(bagFront, 0F, 0F, 0F);

        bagWheat = new RendererModel(this, 19, 37);
        bagWheat.addBox(-3F, 1.5F, 3F, 6, 1, 3);
        bagWheat.setRotationPoint(0F, 0F, 0F);
        bagWheat.setTextureSize(128, 64);
        bagWheat.mirror = true;
        setRotation(bagWheat, 0F, 0F, 0F);

        bagBot = new RendererModel(this, 0, 46);
        bagBot.addBox(-3F, 9F, 3F, 6, 1, 3);
        bagBot.setRotationPoint(0F, 0F, 0F);
        bagBot.setTextureSize(128, 64);
        bagBot.mirror = true;
        setRotation(bagBot, 0F, 0F, 0F);

        this.bipedBody.addChild(bagR);
        this.bipedBody.addChild(bagL);
        this.bipedBody.addChild(bagBack);
        this.bipedBody.addChild(bagFront);
        this.bipedBody.addChild(bagWheat);
        this.bipedBody.addChild(bagBot);
    }

    @Override
    public void render(
      @NotNull final LivingEntity entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
