package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelChiefNorsemen extends NorsemenModel
{
	public ModelChiefNorsemen()
    {
        final ModelRenderer hornr;
        final ModelRenderer boner;
        final ModelRenderer boner2;
        final ModelRenderer hornl2;
        final ModelRenderer bonel3;
        final ModelRenderer bonel4;
        final ModelRenderer robe2;
        final ModelRenderer robe1;
        final ModelRenderer robe3;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(89, 44).addBox(-4.5F, -8.5F, -5.0F, 9.0F, 0.5F, 9.0F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.5F, -8.5F, 4.0F, 9.0F, 8.5F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(4.0F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.5F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(4.0F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.5F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(4.0F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.5F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F, 0.0F, false);
        bipedHead.setTextureOffset(89, 45).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 2.75F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 1.0F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-4.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(3.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-1.0F, -5.25F, -5.0F, 2.0F, 1.25F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(89, 42).addBox(-1.0F, -3.0F, -5.0F, 2.0F, 1.25F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(45, 36).addBox(4.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F, 0.0F, false);
        bipedHead.setTextureOffset(45, 36).addBox(-5.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-4.25F, -1.25F, -4.5F, 8.5F, 2.25F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-4.25F, -4.0F, -4.5F, 8.5F, 2.0F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-4.25F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(1.0F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(0.5F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-4.25F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-3.75F, 1.75F, -4.5F, 2.5F, 0.75F, 1.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-3.5F, 2.5F, -4.5F, 1.25F, 1.5F, 1.5F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(1.5F, 1.75F, -4.5F, 2.25F, 1.5F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(2.25F, 3.25F, -4.5F, 1.5F, 1.5F, 1.75F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(2.75F, 4.75F, -4.5F, 1.0F, 1.25F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(64, 23).addBox(-3.25F, 4.0F, -4.5F, 0.75F, 1.25F, 1.0F, 0.0F, false);

        hornr = new ModelRenderer(this);
        hornr.setRotationPoint(0.5F, -8.5F, 3.0F);
        bipedHead.addChild(hornr);
        hornr.setTextureOffset(2, 49).addBox(4.5F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F, 0.0F, false);
        hornr.setTextureOffset(2, 49).addBox(6.0F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F, 0.0F, false);

        boner = new ModelRenderer(this);
        boner.setRotationPoint(6.25F, 0.5F, 0.0F);
        hornr.addChild(boner);
        setRotationAngle(boner, 0.6109F, 0.7854F, 0.6109F);
        boner.setTextureOffset(2, 50).addBox(3.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F, 0.0F, false);

        boner2 = new ModelRenderer(this);
        boner2.setRotationPoint(-2.9362F, -0.7956F, 1.0631F);
        boner.addChild(boner2);
        setRotationAngle(boner2, 0.0F, 0.1745F, 0.0F);
        boner2.setTextureOffset(2, 49).addBox(6.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F, 0.0F, false);
        boner2.setTextureOffset(2, 49).addBox(8.5136F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F, 0.0F, false);
        boner2.setTextureOffset(2, 49).addBox(10.7569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F, 0.0F, false);
        boner2.setTextureOffset(2, 49).addBox(11.4919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F, 0.0F, false);

        hornl2 = new ModelRenderer(this);
        hornl2.setRotationPoint(-0.5F, -8.5F, 3.0F);
        bipedHead.addChild(hornl2);
        hornl2.setTextureOffset(2, 49).addBox(-6.25F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F, 0.0F, true);
        hornl2.setTextureOffset(2, 49).addBox(-8.25F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F, 0.0F, true);

        bonel3 = new ModelRenderer(this);
        bonel3.setRotationPoint(-6.25F, 0.5F, 0.0F);
        hornl2.addChild(bonel3);
        setRotationAngle(bonel3, 0.6109F, -0.7854F, -0.6109F);
        bonel3.setTextureOffset(2, 50).addBox(-5.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F, 0.0F, true);

        bonel4 = new ModelRenderer(this);
        bonel4.setRotationPoint(2.9362F, -0.7956F, 1.0631F);
        bonel3.addChild(bonel4);
        setRotationAngle(bonel4, 0.0F, -0.1745F, 0.0F);
        bonel4.setTextureOffset(2, 49).addBox(-9.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F, 0.0F, true);
        bonel4.setTextureOffset(2, 49).addBox(-11.2636F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F, 0.0F, true);
        bonel4.setTextureOffset(2, 49).addBox(-12.2569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F, 0.0F, true);
        bonel4.setTextureOffset(2, 49).addBox(-12.9919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(2.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(2.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(2.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(2.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        bipedLeftArm.setTextureOffset(0, 36).addBox(-1.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);

        robe2 = new ModelRenderer(this);
        robe2.setRotationPoint(-3.0F, 0.0F, 2.0F);
        bipedLeftArm.addChild(robe2);
        robe2.setTextureOffset(58, 47).addBox(2.25F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F, 0.0F, true);
        robe2.setTextureOffset(58, 46).addBox(6.0F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F, 0.0F, true);
        robe2.setTextureOffset(58, 47).addBox(6.0F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F, 0.0F, true);
        robe2.setTextureOffset(58, 47).addBox(6.0F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F, 0.0F, true);
        robe2.setTextureOffset(58, 47).addBox(2.25F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F, 0.0F, true);
        robe2.setTextureOffset(58, 47).addBox(4.25F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F, 0.0F, true);
        robe2.setTextureOffset(58, 47).addBox(2.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-1.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-1.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-1.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-1.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 36).addBox(-4.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);

        robe1 = new ModelRenderer(this);
        robe1.setRotationPoint(2.0F, 0.0F, 2.0F);
        bipedRightArm.addChild(robe1);
        robe1.setTextureOffset(58, 47).addBox(-6.75F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F, 0.0F, false);
        robe1.setTextureOffset(58, 46).addBox(-6.75F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F, 0.0F, false);
        robe1.setTextureOffset(58, 47).addBox(-6.75F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F, 0.0F, false);
        robe1.setTextureOffset(58, 47).addBox(-6.75F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F, 0.0F, false);
        robe1.setTextureOffset(58, 47).addBox(-6.0F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F, 0.0F, false);
        robe1.setTextureOffset(58, 47).addBox(-6.0F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F, 0.0F, false);
        robe1.setTextureOffset(58, 47).addBox(-4.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        bipedBody.setTextureOffset(70, 5).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 3.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(97, 1).addBox(-2.5F, 13.0F, -2.5F, 5.0F, 8.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(70, 5).addBox(-4.5F, 10.0F, 2.0F, 9.0F, 3.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(75, 2).addBox(-4.5F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(76, 2).addBox(4.0F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(32, 36).addBox(3.25F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(28, 36).addBox(2.5F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(31, 36).addBox(-3.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(28, 36).addBox(-4.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(28, 36).addBox(1.0F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(32, 36).addBox(1.75F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(28, 36).addBox(-0.5F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(32, 36).addBox(0.25F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(28, 36).addBox(-2.0F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(32, 36).addBox(-1.25F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(23, 48).addBox(2.25F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(27, 48).addBox(-3.75F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(27, 48).addBox(-2.0F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(23, 48).addBox(0.5F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(23, 49).addBox(0.5F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(23, 49).addBox(2.25F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(26, 49).addBox(-0.75F, 4.0F, 2.0F, 1.75F, 2.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(58, 49).addBox(-1.53F, 5.35F, 5.36F, 10.25F, 7.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(66, 49).addBox(-8.53F, 5.35F, 5.36F, 7.0F, 7.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(66, 49).addBox(-8.53F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F, 0.0F, false);
        bipedBody.setTextureOffset(66, 49).addBox(5.47F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F, 0.0F, false);
        bipedBody.setTextureOffset(59, 49).addBox(-5.78F, 10.6F, 2.11F, 11.25F, 2.5F, 3.25F, 0.0F, false);
        bipedBody.setTextureOffset(66, 49).addBox(-8.509F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F, 0.0F, false);
        bipedBody.setTextureOffset(66, 49).addBox(5.45F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F, 0.0F, false);
        bipedBody.setTextureOffset(27, 49).addBox(-2.0F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(27, 49).addBox(-3.75F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);

        robe3 = new ModelRenderer(this);
        robe3.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(robe3);
        setRotationAngle(robe3, 0.4363F, 0.0F, 0.0F);
        robe3.setTextureOffset(58, 49).addBox(-1.52F, -21.4F, 12.7123F, 10.25F, 3.5F, 0.5F, 0.0F, false);
        robe3.setTextureOffset(66, 49).addBox(-8.52F, -21.4F, 12.7123F, 7.0F, 3.5F, 0.5F, 0.0F, false);
        robe3.setTextureOffset(66, 49).addBox(-8.51F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F, 0.0F, false);
        robe3.setTextureOffset(66, 49).addBox(8.24F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F, 0.0F, false);
        robe3.setTextureOffset(66, 49).addBox(-8.52F, -17.9F, 12.6982F, 7.0F, 3.5F, 0.5F, 0.0F, false);
        robe3.setTextureOffset(58, 49).addBox(-1.52F, -17.9F, 12.6982F, 10.25F, 3.5F, 0.5F, 0.0F, false);

        bipedHeadwear.showModel = false;
	}

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
