package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelChiefNorsemen extends NorsemenModel
{
    public ModelChiefNorsemen()
    {
        final ModelPart hornr;
        final ModelPart boner;
        final ModelPart boner2;
        final ModelPart hornl2;
        final ModelPart bonel3;
        final ModelPart bonel4;
        final ModelPart robe2;
        final ModelPart robe1;
        final ModelPart robe3;

        texWidth = 128;
        texHeight = 64;

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        head.texOffs(89, 44).addBox(-4.5F, -8.5F, -5.0F, 9.0F, 0.5F, 9.0F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.5F, -8.5F, 4.0F, 9.0F, 8.5F, 0.5F, 0.0F, false);
        head.texOffs(89, 42).addBox(4.0F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.5F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F, 0.0F, false);
        head.texOffs(89, 42).addBox(4.0F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.5F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F, 0.0F, false);
        head.texOffs(89, 42).addBox(4.0F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.5F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F, 0.0F, false);
        head.texOffs(89, 45).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 2.75F, 0.75F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 1.0F, 0.75F, 0.0F, false);
        head.texOffs(89, 42).addBox(-4.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F, 0.0F, false);
        head.texOffs(89, 42).addBox(3.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F, 0.0F, false);
        head.texOffs(89, 42).addBox(-1.0F, -5.25F, -5.0F, 2.0F, 1.25F, 0.75F, 0.0F, false);
        head.texOffs(89, 42).addBox(-1.0F, -3.0F, -5.0F, 2.0F, 1.25F, 0.75F, 0.0F, false);
        head.texOffs(45, 36).addBox(4.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F, 0.0F, false);
        head.texOffs(45, 36).addBox(-5.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F, 0.0F, false);
        head.texOffs(64, 23).addBox(-4.25F, -1.25F, -4.5F, 8.5F, 2.25F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(-4.25F, -4.0F, -4.5F, 8.5F, 2.0F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(-4.25F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(1.0F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(0.5F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(-4.25F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(-3.75F, 1.75F, -4.5F, 2.5F, 0.75F, 1.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(-3.5F, 2.5F, -4.5F, 1.25F, 1.5F, 1.5F, 0.0F, false);
        head.texOffs(64, 23).addBox(1.5F, 1.75F, -4.5F, 2.25F, 1.5F, 2.0F, 0.0F, false);
        head.texOffs(64, 23).addBox(2.25F, 3.25F, -4.5F, 1.5F, 1.5F, 1.75F, 0.0F, false);
        head.texOffs(64, 23).addBox(2.75F, 4.75F, -4.5F, 1.0F, 1.25F, 1.0F, 0.0F, false);
        head.texOffs(64, 23).addBox(-3.25F, 4.0F, -4.5F, 0.75F, 1.25F, 1.0F, 0.0F, false);

        hornr = new ModelPart(this);
        hornr.setPos(0.5F, -8.5F, 3.0F);
        head.addChild(hornr);
        hornr.texOffs(2, 49).addBox(4.5F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F, 0.0F, false);
        hornr.texOffs(2, 49).addBox(6.0F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F, 0.0F, false);

        boner = new ModelPart(this);
        boner.setPos(6.25F, 0.5F, 0.0F);
        hornr.addChild(boner);
        setRotationAngle(boner, 0.6109F, 0.7854F, 0.6109F);
        boner.texOffs(2, 50).addBox(3.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F, 0.0F, false);

        boner2 = new ModelPart(this);
        boner2.setPos(-2.9362F, -0.7956F, 1.0631F);
        boner.addChild(boner2);
        setRotationAngle(boner2, 0.0F, 0.1745F, 0.0F);
        boner2.texOffs(2, 49).addBox(6.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F, 0.0F, false);
        boner2.texOffs(2, 49).addBox(8.5136F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F, 0.0F, false);
        boner2.texOffs(2, 49).addBox(10.7569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F, 0.0F, false);
        boner2.texOffs(2, 49).addBox(11.4919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F, 0.0F, false);

        hornl2 = new ModelPart(this);
        hornl2.setPos(-0.5F, -8.5F, 3.0F);
        head.addChild(hornl2);
        hornl2.texOffs(2, 49).addBox(-6.25F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F, 0.0F, true);
        hornl2.texOffs(2, 49).addBox(-8.25F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F, 0.0F, true);

        bonel3 = new ModelPart(this);
        bonel3.setPos(-6.25F, 0.5F, 0.0F);
        hornl2.addChild(bonel3);
        setRotationAngle(bonel3, 0.6109F, -0.7854F, -0.6109F);
        bonel3.texOffs(2, 50).addBox(-5.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F, 0.0F, true);

        bonel4 = new ModelPart(this);
        bonel4.setPos(2.9362F, -0.7956F, 1.0631F);
        bonel3.addChild(bonel4);
        setRotationAngle(bonel4, 0.0F, -0.1745F, 0.0F);
        bonel4.texOffs(2, 49).addBox(-9.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F, 0.0F, true);
        bonel4.texOffs(2, 49).addBox(-11.2636F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F, 0.0F, true);
        bonel4.texOffs(2, 49).addBox(-12.2569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F, 0.0F, true);
        bonel4.texOffs(2, 49).addBox(-12.9919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        leftArm.texOffs(0, 36).addBox(-1.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(2.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(2.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(2.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(2.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        leftArm.texOffs(0, 36).addBox(-1.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);

        robe2 = new ModelPart(this);
        robe2.setPos(-3.0F, 0.0F, 2.0F);
        leftArm.addChild(robe2);
        robe2.texOffs(58, 47).addBox(2.25F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F, 0.0F, true);
        robe2.texOffs(58, 46).addBox(6.0F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F, 0.0F, true);
        robe2.texOffs(58, 47).addBox(6.0F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F, 0.0F, true);
        robe2.texOffs(58, 47).addBox(6.0F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F, 0.0F, true);
        robe2.texOffs(58, 47).addBox(2.25F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F, 0.0F, true);
        robe2.texOffs(58, 47).addBox(4.25F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F, 0.0F, true);
        robe2.texOffs(58, 47).addBox(2.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F, 0.0F, true);

        rightArm = new ModelPart(this);
        rightArm.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-1.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-1.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-1.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-1.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F, 0.0F, false);
        rightArm.texOffs(0, 36).addBox(-4.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F, 0.0F, false);

        robe1 = new ModelPart(this);
        robe1.setPos(2.0F, 0.0F, 2.0F);
        rightArm.addChild(robe1);
        robe1.texOffs(58, 47).addBox(-6.75F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F, 0.0F, false);
        robe1.texOffs(58, 46).addBox(-6.75F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F, 0.0F, false);
        robe1.texOffs(58, 47).addBox(-6.75F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F, 0.0F, false);
        robe1.texOffs(58, 47).addBox(-6.75F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F, 0.0F, false);
        robe1.texOffs(58, 47).addBox(-6.0F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F, 0.0F, false);
        robe1.texOffs(58, 47).addBox(-6.0F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F, 0.0F, false);
        robe1.texOffs(58, 47).addBox(-4.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F, 0.0F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        body.texOffs(70, 5).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 3.0F, 0.5F, 0.0F, false);
        body.texOffs(97, 1).addBox(-2.5F, 13.0F, -2.5F, 5.0F, 8.0F, 0.5F, 0.0F, false);
        body.texOffs(70, 5).addBox(-4.5F, 10.0F, 2.0F, 9.0F, 3.0F, 0.5F, 0.0F, false);
        body.texOffs(75, 2).addBox(-4.5F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F, 0.0F, false);
        body.texOffs(76, 2).addBox(4.0F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F, 0.0F, false);
        body.texOffs(32, 36).addBox(3.25F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(28, 36).addBox(2.5F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(31, 36).addBox(-3.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(28, 36).addBox(-4.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(28, 36).addBox(1.0F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(32, 36).addBox(1.75F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(28, 36).addBox(-0.5F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(32, 36).addBox(0.25F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(28, 36).addBox(-2.0F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(32, 36).addBox(-1.25F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(23, 48).addBox(2.25F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F, 0.0F, false);
        body.texOffs(27, 48).addBox(-3.75F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F, 0.0F, false);
        body.texOffs(27, 48).addBox(-2.0F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(23, 48).addBox(0.5F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(23, 49).addBox(0.5F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(23, 49).addBox(2.25F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(26, 49).addBox(-0.75F, 4.0F, 2.0F, 1.75F, 2.75F, 0.5F, 0.0F, false);
        body.texOffs(58, 49).addBox(-1.53F, 5.35F, 5.36F, 10.25F, 7.75F, 0.5F, 0.0F, false);
        body.texOffs(66, 49).addBox(-8.53F, 5.35F, 5.36F, 7.0F, 7.75F, 0.5F, 0.0F, false);
        body.texOffs(66, 49).addBox(-8.53F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F, 0.0F, false);
        body.texOffs(66, 49).addBox(5.47F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F, 0.0F, false);
        body.texOffs(59, 49).addBox(-5.78F, 10.6F, 2.11F, 11.25F, 2.5F, 3.25F, 0.0F, false);
        body.texOffs(66, 49).addBox(-8.509F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F, 0.0F, false);
        body.texOffs(66, 49).addBox(5.45F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F, 0.0F, false);
        body.texOffs(27, 49).addBox(-2.0F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);
        body.texOffs(27, 49).addBox(-3.75F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F, 0.0F, false);

        robe3 = new ModelPart(this);
        robe3.setPos(0.0F, 24.0F, 0.0F);
        body.addChild(robe3);
        setRotationAngle(robe3, 0.4363F, 0.0F, 0.0F);
        robe3.texOffs(58, 49).addBox(-1.52F, -21.4F, 12.7123F, 10.25F, 3.5F, 0.5F, 0.0F, false);
        robe3.texOffs(66, 49).addBox(-8.52F, -21.4F, 12.7123F, 7.0F, 3.5F, 0.5F, 0.0F, false);
        robe3.texOffs(66, 49).addBox(-8.51F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F, 0.0F, false);
        robe3.texOffs(66, 49).addBox(8.24F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F, 0.0F, false);
        robe3.texOffs(66, 49).addBox(-8.52F, -17.9F, 12.6982F, 7.0F, 3.5F, 0.5F, 0.0F, false);
        robe3.texOffs(58, 49).addBox(-1.52F, -17.9F, 12.6982F, 10.25F, 3.5F, 0.5F, 0.0F, false);

        hat.visible = false;
    }

    private void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
