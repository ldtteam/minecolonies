package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

/**
 * Female sheep farmer model.
 */
public class ModelEntitySheepFarmerFemale extends CitizenModel
{
    public ModelEntitySheepFarmerFemale()
    {
        RendererModel bipedChest;

        RendererModel backhair;
        RendererModel hairbackbuttom1;
        RendererModel hairbackTop_2;
        RendererModel hairbackTop_3;
        RendererModel hairBackTop_4;
        RendererModel hairfrontTop_1;
        RendererModel hairfrontTop_2;
        RendererModel hairfrontTop_3;
        RendererModel hairTop_2;
        RendererModel hairLeftTop_1;
        RendererModel hairLeftTop_2;
        RendererModel hairLeftTop_3;
        RendererModel hairLeftTop_4;
        RendererModel hairLeftTop_5;
        RendererModel hairRightTop_1;
        RendererModel hairTop_1;
        RendererModel left_top_1;
        RendererModel ponytail_1;
        RendererModel ponytail_2;
        RendererModel ponytail_3;

        RendererModel bagR;
        RendererModel bagL;
        RendererModel bagBack;
        RendererModel bagFront;
        RendererModel bagWheat;
        RendererModel bagBot;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedChest = new RendererModel(this, 40, 32);
        bipedChest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        bipedChest.setRotationPoint(0F, 0F, 0F);
        bipedChest.setTextureSize(128, 64);
        bipedChest.mirror = true;
        setRotation(bipedChest, -0.5934119F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        backhair = new RendererModel(this, 0, 45);
        backhair.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1);
        backhair.setRotationPoint(0F, 0F, 0F);
        backhair.setTextureSize(128, 64);
        backhair.mirror = true;
        setRotation(backhair, 0F, 0F, 0F);

        hairbackbuttom1 = new RendererModel(this, 0, 45);
        hairbackbuttom1.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1);
        hairbackbuttom1.setRotationPoint(0F, 0F, 0F);
        hairbackbuttom1.setTextureSize(128, 64);
        hairbackbuttom1.mirror = true;
        setRotation(hairbackbuttom1, 0F, 0F, 0F);

        hairbackTop_2 = new RendererModel(this, 0, 45);
        hairbackTop_2.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 8);
        hairbackTop_2.setRotationPoint(0F, 0F, -3F);
        hairbackTop_2.setTextureSize(128, 64);
        hairbackTop_2.mirror = true;

        setRotation(hairbackTop_2, 0F, 0F, 0F);
        hairbackTop_3 = new RendererModel(this, 0, 45);
        hairbackTop_3.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 8);
        hairbackTop_3.setRotationPoint(0F, 0F, -4F);
        hairbackTop_3.setTextureSize(128, 64);
        hairbackTop_3.mirror = true;
        setRotation(hairbackTop_3, 0F, 0F, 0F);

        hairBackTop_4 = new RendererModel(this, 0, 45);
        hairBackTop_4.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3);
        hairBackTop_4.setRotationPoint(0F, 0F, 0F);
        hairBackTop_4.setTextureSize(128, 64);
        hairBackTop_4.mirror = true;
        setRotation(hairBackTop_4, 0F, 0F, 0F);

        hairfrontTop_1 = new RendererModel(this, 0, 45);
        hairfrontTop_1.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1);
        hairfrontTop_1.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_1.setTextureSize(128, 64);
        hairfrontTop_1.mirror = true;
        setRotation(hairfrontTop_1, 0F, 0F, 0F);

        hairfrontTop_2 = new RendererModel(this, 0, 45);
        hairfrontTop_2.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1);
        hairfrontTop_2.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_2.setTextureSize(128, 64);
        hairfrontTop_2.mirror = true;
        setRotation(hairfrontTop_2, 0F, 0F, 0F);

        hairfrontTop_3 = new RendererModel(this, 0, 45);
        hairfrontTop_3.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1);
        hairfrontTop_3.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_3.setTextureSize(128, 64);
        hairfrontTop_3.mirror = true;
        setRotation(hairfrontTop_3, 0F, 0F, 0F);

        hairTop_2 = new RendererModel(this, 0, 45);
        hairTop_2.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9);
        hairTop_2.setRotationPoint(0F, 0F, 0F);
        hairTop_2.setTextureSize(128, 64);
        hairTop_2.mirror = true;
        setRotation(hairTop_2, 0F, 0F, 0F);

        hairLeftTop_1 = new RendererModel(this, 0, 45);
        hairLeftTop_1.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8);
        hairLeftTop_1.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_1.setTextureSize(128, 64);
        hairLeftTop_1.mirror = true;
        setRotation(hairLeftTop_1, 0F, 0F, 0F);

        hairLeftTop_2 = new RendererModel(this, 0, 45);
        hairLeftTop_2.addBox(2.5F, -5.5F, -3.5F, 2, 1, 8);
        hairLeftTop_2.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_2.setTextureSize(128, 64);
        hairLeftTop_2.mirror = true;
        setRotation(hairLeftTop_2, 0F, 0F, 0F);

        hairLeftTop_3 = new RendererModel(this, 0, 45);
        hairLeftTop_3.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2);
        hairLeftTop_3.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_3.setTextureSize(128, 64);
        hairLeftTop_3.mirror = true;
        setRotation(hairLeftTop_3, 0F, 0F, 0F);

        hairLeftTop_4 = new RendererModel(this, 0, 45);
        hairLeftTop_4.addBox(2.5F, -3.5F, 1.5F, 2, 4, 3);
        hairLeftTop_4.setRotationPoint(0F, -1F, 0F);
        hairLeftTop_4.setTextureSize(128, 64);
        hairLeftTop_4.mirror = true;
        setRotation(hairLeftTop_4, 0F, 0F, 0F);

        hairLeftTop_5 = new RendererModel(this, 0, 45);
        hairLeftTop_5.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8);
        hairLeftTop_5.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_5.setTextureSize(128, 64);
        hairLeftTop_5.mirror = true;
        setRotation(hairLeftTop_5, 0F, 0F, 0F);

        hairRightTop_1 = new RendererModel(this, 0, 45);
        hairRightTop_1.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2);
        hairRightTop_1.setRotationPoint(0F, 0F, 0F);
        hairRightTop_1.setTextureSize(128, 64);
        hairRightTop_1.mirror = true;
        setRotation(hairRightTop_1, 0F, 0F, 0F);

        hairTop_1 = new RendererModel(this, 0, 45);
        hairTop_1.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        hairTop_1.setRotationPoint(0F, 0F, 0F);
        hairTop_1.setTextureSize(128, 64);
        hairTop_1.mirror = true;
        setRotation(hairTop_1, 0F, 0F, 0F);

        left_top_1 = new RendererModel(this, 0, 45);
        left_top_1.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9);
        left_top_1.setRotationPoint(0F, 0F, 0F);
        left_top_1.setTextureSize(128, 64);
        left_top_1.mirror = true;
        setRotation(left_top_1, 0F, 0F, 0F);

        ponytail_1 = new RendererModel(this, 0, 45);
        ponytail_1.addBox(-7.5F, -7.5F, -4.5F, 1, 1, 4);
        ponytail_1.setRotationPoint(7F, 5.5F, 2F);
        ponytail_1.setTextureSize(128, 64);
        ponytail_1.mirror = true;
        setRotation(ponytail_1, -1.186824F, 0F, 0F);

        ponytail_2 = new RendererModel(this, 0, 45);
        ponytail_2.addBox(-7.5F, -7.5F, -4.5F, 3, 2, 4);
        ponytail_2.setRotationPoint(6F, -1F, 0F);
        ponytail_2.setTextureSize(128, 64);
        ponytail_2.mirror = true;
        setRotation(ponytail_2, -1.064651F, 0F, 0F);

        ponytail_3 = new RendererModel(this, 0, 45);
        ponytail_3.addBox(-7.5F, -7.5F, -4.5F, 2, 2, 4);
        ponytail_3.setRotationPoint(6.5F, 2F, 1F);
        ponytail_3.setTextureSize(128, 64);
        ponytail_3.mirror = true;
        setRotation(ponytail_3, -1.186824F, 0F, 0F);

        bagR = new RendererModel(this, 40, 41);
        bagR.addBox(3F, 0F, 3F, 1, 9, 3);
        bagR.setRotationPoint(0F, 0F, 0F);
        bagR.setTextureSize(128, 64);
        bagR.mirror = true;
        setRotation(bagR, 0F, 0F, 0F);

        bagL = new RendererModel(this, 40, 41);
        bagL.addBox(-4F, 0F, 3F, 1, 9, 3);
        bagL.setRotationPoint(0F, 0F, 0F);
        bagL.setTextureSize(128, 64);
        bagL.mirror = true;
        setRotation(bagL, 0F, 0F, 0F);

        bagBack = new RendererModel(this, 40, 44);
        bagBack.addBox(-3F, 0F, 2F, 6, 9, 1);
        bagBack.setRotationPoint(0F, 0F, 0F);
        bagBack.setTextureSize(128, 64);
        bagBack.mirror = true;
        setRotation(bagBack, 0F, 0F, 0F);

        bagFront = new RendererModel(this, 40, 41);
        bagFront.addBox(-3F, 1F, 6F, 6, 8, 1);
        bagFront.setRotationPoint(0F, 0F, 0F);
        bagFront.setTextureSize(128, 64);
        bagFront.mirror = true;
        setRotation(bagFront, 0F, 0F, 0F);

        bagWheat = new RendererModel(this, 56, 41);
        bagWheat.addBox(-3F, 1.5F, 3F, 6, 1, 3);
        bagWheat.setRotationPoint(0F, 0F, 0F);
        bagWheat.setTextureSize(128, 64);
        bagWheat.mirror = true;
        setRotation(bagWheat, 0F, 0F, 0F);

        bagBot = new RendererModel(this, 40, 46);
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
        this.bipedBody.addChild(bipedChest);

        this.bipedHead.addChild(left_top_1);

        this.bipedHead.addChild(backhair);
        this.bipedHead.addChild(hairbackTop_2);
        this.bipedHead.addChild(hairbackTop_3);
        this.bipedHead.addChild(hairBackTop_4);

        this.bipedHead.addChild(hairTop_1);
        this.bipedHead.addChild(hairTop_2);

        this.bipedHead.addChild(hairLeftTop_1);
        this.bipedHead.addChild(hairLeftTop_2);
        this.bipedHead.addChild(hairLeftTop_3);
        this.bipedHead.addChild(hairLeftTop_4);
        this.bipedHead.addChild(hairLeftTop_5);
        
        this.bipedHead.addChild(hairbackbuttom1);

        this.bipedHead.addChild(ponytail_1);
        this.bipedHead.addChild(ponytail_2);
        this.bipedHead.addChild(ponytail_3);

        this.bipedHead.addChild(hairRightTop_1);
        this.bipedHead.addChild(hairfrontTop_1);
        this.bipedHead.addChild(hairfrontTop_2);
        this.bipedHead.addChild(hairfrontTop_3);
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

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotation(RendererModel RendererModel, float x, float y, float z)
    {
        RendererModel.rotateAngleX = x;
        RendererModel.rotateAngleY = y;
        RendererModel.rotateAngleZ = z;
    }
}
