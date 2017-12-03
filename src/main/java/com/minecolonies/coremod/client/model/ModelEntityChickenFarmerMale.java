package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityChickenFarmerMale extends ModelBiped
{
    ModelRenderer beardBot;
    ModelRenderer beardTop;
    ModelRenderer baseBag;
    ModelRenderer strap;
    ModelRenderer feed;

    public ModelEntityChickenFarmerMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        beardBot = new ModelRenderer(this, 31, 47);
        beardBot.addBox(-1F, 1F, -4F, 2, 1, 0);
        beardBot.setRotationPoint(0F, 0F, 0F);
        beardBot.setTextureSize(128, 64);
        beardBot.mirror = true;
        setRotation(beardBot, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        beardTop = new ModelRenderer(this, 31, 52);
        beardTop.addBox(-1.5F, 0F, -4F, 3, 1, 0);
        beardTop.setRotationPoint(0F, 0F, 0F);
        beardTop.setTextureSize(128, 64);
        beardTop.mirror = true;
        setRotation(beardTop, 0F, 0F, 0F);

        baseBag = new ModelRenderer(this, 14, 45);
        baseBag.addBox(2.466667F, 10F, -3F, 2, 2, 6);
        baseBag.setRotationPoint(0F, 0F, 0F);
        baseBag.setTextureSize(128, 64);
        baseBag.mirror = true;
        setRotation(baseBag, 0F, 0F, 0F);

        strap = new ModelRenderer(this, 0, 33);
        strap.addBox(-4F, -3F, -3F, 1, 14, 6);
        strap.setRotationPoint(0F, 0F, 0F);
        strap.setTextureSize(128, 64);
        strap.mirror = true;
        setRotation(strap, 0F, 0F, -0.6108652F);

        feed = new ModelRenderer(this, 14, 38);
        feed.addBox(3.3F, 9.8F, -2.5F, 1, 2, 5);
        feed.setRotationPoint(0F, 0F, 0F);
        feed.setTextureSize(128, 64);
        feed.mirror = true;
        setRotation(feed, 0F, 0F, 0F);

        bipedBody.addChild(baseBag);
        bipedBody.addChild(strap);
        bipedBody.addChild(feed);

        bipedHead.addChild(beardBot);
        bipedHead.addChild(beardTop);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
