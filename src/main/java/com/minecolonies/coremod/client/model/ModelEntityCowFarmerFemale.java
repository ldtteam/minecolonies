package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCowFarmerFemale extends CitizenModel
{
    public ModelEntityCowFarmerFemale()
    {
        RendererModel bagR;
        RendererModel bagL;
        RendererModel bagBack;
        RendererModel bagFront;
        RendererModel bagWheat;
        RendererModel bagBot;

        RendererModel bipedChest;

        RendererModel hairLeftTop;
        RendererModel hairTop;
        RendererModel hairLeftTop_1;
        RendererModel hairLeftTop_2;
        RendererModel hairLeftTop_3;
        RendererModel hairLeftTop_4;
        RendererModel hairLeftTop_5;
        RendererModel hairTop_1;
        RendererModel hairTop_2;
        RendererModel hairTop_3;
        RendererModel hairLeftTop_6;
        RendererModel hairLeftTop_7;
        RendererModel hairLeftTop_8;
        RendererModel hairLeftTop_9;
        RendererModel hairLeftTop_10;
        RendererModel hairLeftTop_11;
        RendererModel hairLeftTop_12;
        RendererModel hairLeftTop_13;
        RendererModel hairTop_4;

        textureWidth = 128;
        textureHeight = 64;

        hairLeftTop_8 = new RendererModel(this, 0, 45);
        hairLeftTop_8.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_8.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        hairLeftTop_6 = new RendererModel(this, 0, 45);
        hairLeftTop_6.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_6.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_5 = new RendererModel(this, 0, 45);
        hairLeftTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_5.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 4, 0.0F);

        hairLeftTop_7 = new RendererModel(this, 0, 45);
        hairLeftTop_7.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_7.addBox(-4.5F, -5.5F, -3.5F, 9, 1, 1, 0.0F);

        hairTop_3 = new RendererModel(this, 0, 45);
        hairTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_3.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9, 0.0F);

        bagL = new RendererModel(this, 812, 425);
        bagL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagL.addBox(-4.0F, 0.0F, 3.0F, 1, 9, 3, 0.0F);

        hairLeftTop_10 = new RendererModel(this, 0, 45);
        hairLeftTop_10.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_10.addBox(2.5F, -5.5F, -0.5F, 2, 2, 5, 0.0F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

        hairLeftTop_12 = new RendererModel(this, 0, 45);
        hairLeftTop_12.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_12.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        bagBot = new RendererModel(this, 808, 426);
        bagBot.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagBot.addBox(-3.0F, 9.0F, 3.0F, 6, 1, 3, 0.0F);

        bagR = new RendererModel(this, 811, 425);
        bagR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagR.addBox(3.0F, 0.0F, 3.0F, 1, 9, 3, 0.0F);

        hairLeftTop_2 = new RendererModel(this, 0, 45);
        hairLeftTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_2.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1, 0.0F);

        hairTop_2 = new RendererModel(this, 0, 45);
        hairTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_2.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9, 0.0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);

        hairLeftTop = new RendererModel(this, 0, 45);
        hairLeftTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8, 0.0F);

        hairTop_4 = new RendererModel(this, 0, 45);
        hairTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_4.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_1 = new RendererModel(this, 0, 45);
        hairLeftTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_1.addBox(2.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairTop_1 = new RendererModel(this, 0, 45);
        hairTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_1.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1, 0.0F);

        hairLeftTop_9 = new RendererModel(this, 0, 45);
        hairLeftTop_9.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_9.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        bagFront = new RendererModel(this, 813, 430);
        bagFront.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagFront.addBox(-3.0F, 1.0F, 6.0F, 6, 8, 1, 0.0F);

        bipedChest = new RendererModel(this, 40, 32);
        bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedChest.addBox(-3.5F, 2.700000047683716F, -0.5F, 7, 3, 4, 0.0F);
        setRotation(bipedChest, -0.593411922454834F, -0.0F, 0.0F);

        bagWheat = new RendererModel(this, 830, 428);
        bagWheat.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagWheat.addBox(-3.0F, 1.5F, 3.0F, 6, 1, 3, 0.0F);

        bagBack = new RendererModel(this, 813, 425);
        bagBack.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagBack.addBox(-3.0F, 0.0F, 2.0F, 6, 9, 1, 0.0F);

        hairLeftTop_3 = new RendererModel(this, 0, 45);
        hairLeftTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_3.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9, 0.0F);

        hairLeftTop_4 = new RendererModel(this, 0, 45);
        hairLeftTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_4.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 5, 0.0F);

        hairLeftTop_13 = new RendererModel(this, 0, 45);
        hairLeftTop_13.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_13.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        hairLeftTop_11 = new RendererModel(this, 0, 45);
        hairLeftTop_11.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_11.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairTop = new RendererModel(this, 0, 45);
        hairTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1, 0.0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        this.bipedBody.addChild(bagR);
        this.bipedBody.addChild(bagL);
        this.bipedBody.addChild(bagBack);
        this.bipedBody.addChild(bagFront);
        this.bipedBody.addChild(bagWheat);
        this.bipedBody.addChild(bagBot);

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

        this.bipedHeadwear.addChild(hairTop);
        this.bipedHeadwear.addChild(hairTop_1);
        this.bipedHeadwear.addChild(hairTop_2);
        this.bipedHeadwear.addChild(hairTop_3);
        this.bipedHeadwear.addChild(hairTop_4);
    }

    @Override
    public void render(
      @NotNull final AbstractEntityCitizen entity,
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
