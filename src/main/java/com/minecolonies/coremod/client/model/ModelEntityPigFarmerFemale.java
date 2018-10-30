package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityPigFarmerFemale extends ModelBiped
{
    public ModelEntityPigFarmerFemale()
    {
        ModelRenderer carrot4;
        ModelRenderer strapR;
        ModelRenderer strapL;
        ModelRenderer carrotBase;
        ModelRenderer carrot1;
        ModelRenderer carrot2;
        ModelRenderer carrot3;

        ModelRenderer bipedChest;

        ModelRenderer hairLeftTop;
        ModelRenderer hairTop;
        ModelRenderer hairLeftTop_1;
        ModelRenderer hairLeftTop_2;
        ModelRenderer hairLeftTop_3;
        ModelRenderer hairLeftTop_4;
        ModelRenderer hairLeftTop_5;
        ModelRenderer hairTop_1;
        ModelRenderer hairTop_2;
        ModelRenderer hairTop_3;
        ModelRenderer hairLeftTop_6;
        ModelRenderer hairLeftTop_7;
        ModelRenderer hairLeftTop_8;
        ModelRenderer hairLeftTop_9;
        ModelRenderer hairLeftTop_10;
        ModelRenderer hairLeftTop_11;
        ModelRenderer hairLeftTop_12;
        ModelRenderer hairLeftTop_13;
        ModelRenderer hairTop_4;
        ModelRenderer hairLeftTop_14;
        ModelRenderer hairTop_5;
        ModelRenderer hairLeftTop_15;
        ModelRenderer hairLeftTop_18;
        ModelRenderer hairLeftTop_19;
        ModelRenderer hairTop_6;
        ModelRenderer hairTop_7;
        ModelRenderer hairTop_8;
        ModelRenderer hairLeftTop_20;
        ModelRenderer hairLeftTop_22;
        ModelRenderer hairLeftTop_24;
        ModelRenderer hairLeftTop_25;
        ModelRenderer hairLeftTop_26;
        ModelRenderer hairLeftTop_27;
        ModelRenderer hairTop_9;

        textureWidth = 128;
        textureHeight = 64;

        hairLeftTop_14 = new ModelRenderer(this, 0, 45);
        hairLeftTop_14.setRotationPoint(-0.5F, 0.0F, 0.0F);
        hairLeftTop_14.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8, 0.0F);

        hairTop_6 = new ModelRenderer(this, 0, 45);
        hairTop_6.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_6.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1, 0.0F);

        bipedChest = new ModelRenderer(this, 40, 32);
        bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedChest.addBox(-3.5F, 2.700000047683716F, -0.5F, 7, 3, 4, 0.0F);
        setRotation(bipedChest, -0.593411922454834F, -0.0F, 0.0F);

        hairLeftTop_4 = new ModelRenderer(this, 0, 45);
        hairLeftTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_4.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 5, 0.0F);

        hairLeftTop_20 = new ModelRenderer(this, 0, 45);
        hairLeftTop_20.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_20.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_25 = new ModelRenderer(this, 0, 45);
        hairLeftTop_25.setRotationPoint(0.0F, 0.0F, -1.0F);
        hairLeftTop_25.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairLeftTop_26 = new ModelRenderer(this, 0, 45);
        hairLeftTop_26.setRotationPoint(0.0F, -1.4F, 0.0F);
        hairLeftTop_26.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        carrot1 = new ModelRenderer(this, 814, 103);
        carrot1.setRotationPoint(0.0F, 0.0F, -0.9F);
        carrot1.addBox(-2.5F, 6.0F, -1.5F, 1, 3, 0, 0.0F);
        setRotation(carrot1, -0.11152653920243764F, 0.0F, -0.017453292519943295F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        hairTop_5 = new ModelRenderer(this, 0, 45);
        hairTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_5.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1, 0.0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);

        hairLeftTop_27 = new ModelRenderer(this, 0, 45);
        hairLeftTop_27.setRotationPoint(0.0F, -1.1F, 0.0F);
        hairLeftTop_27.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        carrot4 = new ModelRenderer(this, 818, 103);
        carrot4.setRotationPoint(0.0F, 0.0F, -0.9F);
        carrot4.addBox(0.0F, 6.5F, -2.5F, 1, 3, 0, 0.0F);
        setRotation(carrot4, 0.02775073510670984F, -0.14608405839192537F, -0.18797196043978928F);

        hairTop_8 = new ModelRenderer(this, 0, 45);
        hairTop_8.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_8.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9, 0.0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

        hairTop_4 = new ModelRenderer(this, 0, 45);
        hairTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_4.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1, 0.0F);

        hairTop = new ModelRenderer(this, 0, 45);
        hairTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1, 0.0F);

        hairLeftTop_13 = new ModelRenderer(this, 0, 45);
        hairLeftTop_13.setRotationPoint(0.0F, -0.8F, 0.0F);
        hairLeftTop_13.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        hairTop_2 = new ModelRenderer(this, 0, 45);
        hairTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_2.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9, 0.0F);

        hairTop_3 = new ModelRenderer(this, 0, 45);
        hairTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_3.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9, 0.0F);

        hairLeftTop_2 = new ModelRenderer(this, 0, 45);
        hairLeftTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_2.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1, 0.0F);

        hairLeftTop_22 = new ModelRenderer(this, 0, 45);
        hairLeftTop_22.setRotationPoint(0.0F, 0.9F, 0.0F);
        hairLeftTop_22.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        hairLeftTop_18 = new ModelRenderer(this, 0, 45);
        hairLeftTop_18.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_18.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 5, 0.0F);

        hairLeftTop_11 = new ModelRenderer(this, 0, 45);
        hairLeftTop_11.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_11.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairLeftTop_5 = new ModelRenderer(this, 0, 45);
        hairLeftTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_5.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 4, 0.0F);

        hairLeftTop_15 = new ModelRenderer(this, 0, 45);
        hairLeftTop_15.setRotationPoint(0.0F, 0.0F, -1.0F);
        hairLeftTop_15.addBox(2.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairLeftTop_24 = new ModelRenderer(this, 0, 45);
        hairLeftTop_24.setRotationPoint(0.0F, 0.9F, 0.0F);
        hairLeftTop_24.addBox(2.5F, -5.5F, -0.5F, 2, 2, 5, 0.0F);

        hairLeftTop_12 = new ModelRenderer(this, 0, 45);
        hairLeftTop_12.setRotationPoint(0.0F, -0.7F, 0.0F);
        hairLeftTop_12.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        strapR = new ModelRenderer(this, 810, 103);
        strapR.setRotationPoint(0.0F, 0.0F, -0.9F);
        strapR.addBox(-3.8F, 0.01F, -2.5F, 1, 9, 4, 0.0F);
        setRotation(strapR, -0.06981317007977318F, 0.0F, 0.0F);

        strapL = new ModelRenderer(this, 810, 103);
        strapL.setRotationPoint(0.0F, 0.0F, -0.9F);
        strapL.addBox(2.8F, 0.01F, -2.5F, 1, 9, 4, 0.0F);
        setRotation(strapL, -0.06981317007977318F, 0.0F, 0.0F);

        carrotBase = new ModelRenderer(this, 810, 104);
        carrotBase.setRotationPoint(0.0F, 0.0F, -0.9F);
        carrotBase.addBox(-3.5F, 8.0F, -3.5F, 7, 3, 4, 0.0F);

        hairLeftTop_10 = new ModelRenderer(this, 0, 45);
        hairLeftTop_10.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_10.addBox(2.5F, -5.5F, -0.5F, 2, 2, 5, 0.0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        hairTop_7 = new ModelRenderer(this, 0, 45);
        hairTop_7.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_7.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9, 0.0F);

        hairTop_1 = new ModelRenderer(this, 0, 45);
        hairTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_1.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1, 0.0F);

        hairLeftTop_7 = new ModelRenderer(this, 0, 45);
        hairLeftTop_7.setRotationPoint(0.0F, 2.7F, 3.1F);
        hairLeftTop_7.addBox(-4.5F, -5.5F, -3.5F, 9, 1, 1, 0.0F);

        hairLeftTop_19 = new ModelRenderer(this, 0, 45);
        hairLeftTop_19.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_19.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 4, 0.0F);
        hairLeftTop = new ModelRenderer(this, 0, 45);
        hairLeftTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8, 0.0F);

        hairTop_9 = new ModelRenderer(this, 0, 45);
        hairTop_9.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_9.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_3 = new ModelRenderer(this, 0, 45);
        hairLeftTop_3.setRotationPoint(-1.4F, 0.0F, 0.1F);
        hairLeftTop_3.addBox(-3.1F, -7.5F, -4.6F, 3, 2, 9, 0.0F);

        hairLeftTop_9 = new ModelRenderer(this, 0, 45);
        hairLeftTop_9.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_9.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        hairLeftTop_8 = new ModelRenderer(this, 0, 45);
        hairLeftTop_8.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_8.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        carrot3 = new ModelRenderer(this, 814, 103);
        carrot3.setRotationPoint(0.0F, 0.0F, -0.9F);
        carrot3.addBox(1.0F, 6.0F, -2.5F, 1, 3, 0, 0.0F);
        setRotation(carrot3, -0.01658062789394613F, -0.11030480872604163F, 0.14957471689591403F);

        carrot2 = new ModelRenderer(this, 814, 103);
        carrot2.setRotationPoint(0.0F, 0.0F, -0.9F);
        carrot2.addBox(0.5F, 6.0F, -2.5F, 1, 3, 0, 0.0F);
        setRotation(carrot2, 0.03874630939427412F, 0.3324852225049198F, 0.11798425743481668F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        hairLeftTop_1 = new ModelRenderer(this, 0, 45);
        hairLeftTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_1.addBox(2.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairLeftTop_6 = new ModelRenderer(this, 0, 45);
        hairLeftTop_6.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_6.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1, 0.0F);

        this.bipedBody.addChild(carrot1);
        this.bipedBody.addChild(carrot2);
        this.bipedBody.addChild(carrot3);
        this.bipedBody.addChild(carrot4);
        this.bipedBody.addChild(carrotBase);

        this.bipedBody.addChild(strapL);
        this.bipedBody.addChild(strapR);

        this.bipedBody.addChild(bipedChest);

        this.bipedHeadwear.addChild(hairLeftTop);
        this.bipedHeadwear.addChild(hairLeftTop_1);
        this.bipedHeadwear.addChild(hairLeftTop_2);
        this.bipedHeadwear.addChild(hairLeftTop_3);
        this.bipedHeadwear.addChild(hairLeftTop_4);
        this.bipedHeadwear.addChild(hairLeftTop_5);
        this.bipedHeadwear.addChild(hairLeftTop_6);
        this.bipedHeadwear.addChild(hairLeftTop_7);
        this.bipedHeadwear.addChild(hairLeftTop_9);
        this.bipedHeadwear.addChild(hairLeftTop_10);
        this.bipedHeadwear.addChild(hairLeftTop_11);
        this.bipedHeadwear.addChild(hairLeftTop_12);
        this.bipedHeadwear.addChild(hairLeftTop_13);
        this.bipedHeadwear.addChild(hairLeftTop_14);
        this.bipedHeadwear.addChild(hairLeftTop_15);
        this.bipedHeadwear.addChild(hairLeftTop_18);
        this.bipedHeadwear.addChild(hairLeftTop_19);
        this.bipedHeadwear.addChild(hairLeftTop_20);
        this.bipedHeadwear.addChild(hairLeftTop_22);
        this.bipedHeadwear.addChild(hairLeftTop_24);
        this.bipedHeadwear.addChild(hairLeftTop_25);
        this.bipedHeadwear.addChild(hairLeftTop_26);
        this.bipedHeadwear.addChild(hairLeftTop_27);


        this.bipedHeadwear.addChild(hairTop);
        this.bipedHeadwear.addChild(hairTop_1);
        this.bipedHeadwear.addChild(hairTop_2);
        this.bipedHeadwear.addChild(hairTop_3);
        this.bipedHeadwear.addChild(hairTop_4);
        this.bipedHeadwear.addChild(hairTop_5);
        this.bipedHeadwear.addChild(hairTop_6);
        this.bipedHeadwear.addChild(hairTop_7);
        this.bipedHeadwear.addChild(hairTop_8);
        //this.bipedHeadwear.addChild(hairTop_9);
    }

    @Override
    public void render(
      @NotNull final Entity entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
