package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityChickenFarmerMale extends BipedModel
{
    public ModelEntityChickenFarmerMale()
    {
        RendererModel beardBot;
        RendererModel beardTop;
        RendererModel baseBag;
        RendererModel strap;
        RendererModel feed;

        textureWidth = 128;
        textureHeight = 64;

        beardBot = new RendererModel(this, 31, 47);
        beardBot.addBox(-1F, 1F, -4F, 2, 1, 0);
        beardBot.setRotationPoint(0F, 0F, 0F);
        beardBot.setTextureSize(128, 64);
        beardBot.mirror = true;
        setRotation(beardBot, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

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

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        beardTop = new RendererModel(this, 31, 52);
        beardTop.addBox(-1.5F, 0F, -4F, 3, 1, 0);
        beardTop.setRotationPoint(0F, 0F, 0F);
        beardTop.setTextureSize(128, 64);
        beardTop.mirror = true;
        setRotation(beardTop, 0F, 0F, 0F);

        baseBag = new RendererModel(this, 14, 45);
        baseBag.addBox(2.466667F, 10F, -3F, 2, 2, 6);
        baseBag.setRotationPoint(0F, 0F, 0F);
        baseBag.setTextureSize(128, 64);
        baseBag.mirror = true;
        setRotation(baseBag, 0F, 0F, 0F);

        strap = new RendererModel(this, 0, 33);
        strap.addBox(-4F, -3F, -3F, 1, 14, 6);
        strap.setRotationPoint(0F, 0F, 0F);
        strap.setTextureSize(128, 64);
        strap.mirror = true;
        setRotation(strap, 0F, 0F, -0.6108652F);

        feed = new RendererModel(this, 14, 38);
        feed.addBox(3.3F, 9.8F, -2.5F, 1, 2, 5);
        feed.setRotationPoint(0F, 0F, 0F);
        feed.setTextureSize(128, 64);
        feed.mirror = true;
        setRotation(feed, 0F, 0F, 0F);

        bipedHead.addChild(beardBot);
        bipedHead.addChild(beardTop);

        bipedBody.addChild(feed);
        bipedBody.addChild(strap);
        bipedBody.addChild(baseBag);
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
