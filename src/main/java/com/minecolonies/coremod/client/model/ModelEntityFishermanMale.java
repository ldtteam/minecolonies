package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityFishermanMale extends BipedModel
{
    //fields
    private final RendererModel string;
    private final RendererModel hookTie1;
    private final RendererModel hookTie2;
    private final RendererModel hookTie3;
    private final RendererModel fish1;
    private final RendererModel fish2;
    private final RendererModel fish3;
    private final RendererModel reel;
    private final RendererModel line;
    private final RendererModel pole;
    private final RendererModel shape1;
    private final RendererModel shape2;
    private final RendererModel shape3;
    private final RendererModel shape4;
    private final RendererModel shape5;
    private final RendererModel shape6;
    private final RendererModel shape7;
    private final RendererModel rightBoot;
    private final RendererModel leftBoot;

    public ModelEntityFishermanMale()
    {
        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);
        bipedLeftLeg.mirror = false;

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        fish3 = new RendererModel(this, 61, 46);
        fish3.addBox(0.4F, 10F, -2.2F, 2, 4, 0);
        fish3.setRotationPoint(0F, 0F, 0F);
        fish3.setTextureSize(256, 128);
        fish3.mirror = true;
        setRotation(fish3, 0F, 0F, 0F);

        hookTie3 = new RendererModel(this, 58, 46);
        hookTie3.addBox(0.5F, 8F, -2.2F, 1, 2, 0);
        hookTie3.setRotationPoint(0F, 0F, 0F);
        hookTie3.setTextureSize(256, 128);
        hookTie3.mirror = true;
        setRotation(hookTie3, 0F, 0F, 0F);

        hookTie1 = new RendererModel(this, 58, 38);
        hookTie1.addBox(-3.5F, 3.5F, -2.2F, 1, 2, 0);
        hookTie1.setRotationPoint(0F, 0F, 0F);
        hookTie1.setTextureSize(256, 128);
        hookTie1.mirror = true;
        setRotation(hookTie1, 0F, 0F, 0F);

        hookTie2 = new RendererModel(this, 58, 42);
        hookTie2.addBox(-1.5F, 5.5F, -2.2F, 1, 2, 0);
        hookTie2.setRotationPoint(0F, 0F, 0F);
        hookTie2.setTextureSize(256, 128);
        hookTie2.mirror = true;
        setRotation(hookTie2, 0F, 0F, 0F);

        fish1 = new RendererModel(this, 61, 38);
        fish1.addBox(-4.4F, 5.5F, -2.2F, 2, 4, 0);
        fish1.setRotationPoint(0F, 0F, 0F);
        fish1.setTextureSize(256, 128);
        fish1.mirror = true;
        setRotation(fish1, 0F, 0F, 0F);

        fish2 = new RendererModel(this, 61, 42);
        fish2.addBox(-2F, 7.5F, -2.2F, 2, 4, 0);
        fish2.setRotationPoint(0F, 0F, 0F);
        fish2.setTextureSize(256, 128);
        fish2.mirror = true;
        setRotation(fish2, 0F, 0F, 0F);

        string = new RendererModel(this, 53, 38);
        string.addBox(-5F, -0.5F, -2.3F, 1, 12, 1);
        string.setRotationPoint(0F, 0F, 0F);
        string.setTextureSize(256, 128);
        string.mirror = true;
        setRotation(string, 0F, 0F, -0.7435722F);

        reel = new RendererModel(this, 62, 64);
        reel.addBox(-6F, 6F, 2F, 2, 2, 1);
        reel.setRotationPoint(0F, 0F, 0F);
        reel.setTextureSize(256, 128);
        reel.mirror = true;
        setRotation(reel, 0F, 0F, 0F);

        line = new RendererModel(this, 62, 52);
        line.addBox(-4.5F, -4.75F, 2.5F, 1, 11, 0);
        line.setRotationPoint(0F, 0F, 0F);
        line.setTextureSize(256, 128);
        line.mirror = true;
        setRotation(line, 0F, 0F, 0F);

        pole = new RendererModel(this, 57, 52);
        pole.addBox(-4F, -5F, 2F, 1, 16, 1);
        pole.setRotationPoint(0F, 0F, 0F);
        pole.setTextureSize(256, 128);
        pole.mirror = true;
        setRotation(pole, 0F, 0F, 0F);

        shape1 = new RendererModel(this, 24, 45);
        shape1.addBox(-5F, -8.6F, 3.2F, 10, 1, 2);
        shape1.setRotationPoint(0F, 0F, 0F);
        shape1.setTextureSize(256, 128);
        shape1.mirror = true;
        setRotation(shape1, -0.2230717F, 0F, 0F);

        shape2 = new RendererModel(this, 0, 48);
        shape2.addBox(3.7F, -8.65F, -5.5F, 2, 1, 10);
        shape2.setRotationPoint(0F, 0F, 0F);
        shape2.setTextureSize(256, 128);
        shape2.mirror = true;
        setRotation(shape2, -0.074351F, 0F, 0.1487195F);

        shape3 = new RendererModel(this, 0, 45);
        shape3.addBox(-5F, -8.7F, -6.2F, 10, 1, 2);
        shape3.setRotationPoint(0F, 0F, 0F);
        shape3.setTextureSize(256, 128);
        shape3.mirror = true;
        setRotation(shape3, 0.0743572F, 0F, 0F);

        shape4 = new RendererModel(this, 0, 69);
        shape4.addBox(-3F, -13F, -3.5F, 6, 1, 6);
        shape4.setRotationPoint(0F, 1F, 0F);
        shape4.setTextureSize(256, 128);
        shape4.mirror = true;
        setRotation(shape4, -0.0743572F, 0F, 0F);

        shape5 = new RendererModel(this, 24, 48);
        shape5.addBox(-5.7F, -8.65F, -5.5F, 2, 1, 10);
        shape5.setRotationPoint(0F, 0F, 0F);
        shape5.setTextureSize(256, 128);
        shape5.mirror = true;
        setRotation(shape5, -0.074351F, 0F, -0.1487144F);

        shape6 = new RendererModel(this, 0, 33);
        shape6.addBox(-5F, -9F, -5.5F, 10, 2, 10);
        shape6.setRotationPoint(0F, 0F, 0F);
        shape6.setTextureSize(256, 128);
        shape6.mirror = true;
        setRotation(shape6, -0.0743572F, 0F, 0F);

        shape7 = new RendererModel(this, 0, 59);
        shape7.addBox(-4F, -11F, -4.5F, 8, 2, 8);
        shape7.setRotationPoint(0F, 0F, 0F);
        shape7.setTextureSize(256, 128);
        shape7.mirror = true;
        setRotation(shape7, -0.0743572F, 0F, 0F);

        rightBoot = new RendererModel(this, 20, 102);
        rightBoot.addBox(-2.7F, 4F, -2.5F, 5, 2, 5);
        rightBoot.setRotationPoint(-2F, 12F, 0F);
        rightBoot.setTextureSize(256, 128);
        rightBoot.mirror = true;
        setRotation(rightBoot, 0F, 0F, 0F);

        leftBoot = new RendererModel(this, 0, 102);
        leftBoot.addBox(-2.3F, 4F, -2.49F, 5, 2, 5);
        leftBoot.setRotationPoint(2F, 12F, 0F);
        leftBoot.setTextureSize(256, 128);
        leftBoot.mirror = true;
        setRotation(leftBoot, 0F, 0F, 0F);

        //bipedLeftLeg.addChild(leftBoot);
        //bipedRightLeg.addChild(rightBoot);

        bipedBody.addChild(hookTie1);
        hookTie1.addChild(fish1);
        bipedBody.addChild(hookTie2);
        hookTie2.addChild(fish2);
        bipedBody.addChild(hookTie3);
        hookTie3.addChild(fish3);

        string.addChild(pole);
        pole.addChild(reel);
        pole.addChild(line);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void render(final LivingEntity entity, final float f, final float f1, final float f2, final float f3, final float f4, final float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);

        string.render(f5);

        shape1.render(f5);//back of hat
        shape2.render(f5);//left side of hat
        shape3.render(f5);//hat front
        shape4.render(f5);//hat top
        shape5.render(f5);//hat right
        shape6.render(f5);//hat base
        shape7.render(f5);//hat mid

        rightBoot.render(f5);
        leftBoot.render(f5);
    }
}