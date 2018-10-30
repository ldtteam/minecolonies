package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Female sheep farmer model.
 */
public class ModelEntitySheepFarmerFemale extends ModelBiped
{
    public ModelEntitySheepFarmerFemale()
    {
        ModelRenderer bipedChest;
        ModelRenderer ponytail_1;
        ModelRenderer ponytail_2;
        ModelRenderer ponytail_3;
        ModelRenderer ponytail_4;
        ModelRenderer ponytail_5;
        ModelRenderer ponytail_6;
        ModelRenderer ponytail_7;
        ModelRenderer ponytail_8;
        ModelRenderer ponytail_9;
        ModelRenderer ponytail_10;
        ModelRenderer ponytail_11;
        ModelRenderer hairbackbuttom1;
        ModelRenderer hairbackTop_1;
        ModelRenderer hairbackTop_2;
        ModelRenderer hairbackTop_3;
        ModelRenderer hairBackTop_4;
        ModelRenderer hairfrontTop_1;
        ModelRenderer hairfrontTop_2;
        ModelRenderer hairfrontTop_3;
        ModelRenderer hairTop_2;
        ModelRenderer hairLeftTop_1;
        ModelRenderer hairLeftTop_2;
        ModelRenderer hairLeftTop_3;
        ModelRenderer hairLeftTop_4;
        ModelRenderer hairLeftTop_5;
        ModelRenderer hairRightTop_1;
        ModelRenderer hairTop_1;
        ModelRenderer bagR;
        ModelRenderer bagL;
        ModelRenderer bagBack;
        ModelRenderer bagFront;
        ModelRenderer bagWheat;
        ModelRenderer bagBot;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedChest = new ModelRenderer(this, 40, 32);
        bipedChest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        bipedChest.setRotationPoint(0F, 0F, 0F);
        bipedChest.setTextureSize(128, 64);
        bipedChest.mirror = true;
        setRotation(bipedChest, -0.5934119F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        ponytail_1 = new ModelRenderer(this, 0, 45);
        ponytail_1.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1);
        ponytail_1.setRotationPoint(0F, 0F, 0F);
        ponytail_1.setTextureSize(128, 64);
        ponytail_1.mirror = true;
        setRotation(ponytail_1, 0F, 0F, 0F);

        ponytail_2 = new ModelRenderer(this, 0, 45);
        ponytail_2.addBox(-1.5F, -6.1F, -3.5F, 2, 1, 3);
        ponytail_2.setRotationPoint(2F, -2F, 3F);
        ponytail_2.setTextureSize(128, 64);
        ponytail_2.mirror = true;
        setRotation(ponytail_2, 0F, 1.58825F, -0.3316126F);

        ponytail_3 = new ModelRenderer(this, 0, 45);
        ponytail_3.addBox(-1.5F, -5.5F, -3.5F, 1, 1, 3);
        ponytail_3.setRotationPoint(2F, -2.1F, 3F);
        ponytail_3.setTextureSize(128, 64);
        ponytail_3.mirror = true;
        setRotation(ponytail_3, 0.25F, 1.58825F, -1.012291F);

        ponytail_4 = new ModelRenderer(this, 0, 45);
        ponytail_4.addBox(-1.5F, -5.5F, -3.5F, 2, 1, 3);
        ponytail_4.setRotationPoint(2F, -2.3F, 3F);
        ponytail_4.setTextureSize(128, 64);
        ponytail_4.mirror = true;
        setRotation(ponytail_4, 0.25F, 1.58825F, -0.3316126F);

        ponytail_5 = new ModelRenderer(this, 0, 45);
        ponytail_5.addBox(0F, -5.5F, -3.5F, 3, 1, 2);
        ponytail_5.setRotationPoint(2.5F, -5F, 3F);
        ponytail_5.setTextureSize(128, 64);
        ponytail_5.mirror = true;
        setRotation(ponytail_5, 0.25F, 1.518436F, -2.164208F);

        ponytail_6 = new ModelRenderer(this, 0, 45);
        ponytail_6.addBox(-1.5F, -5.5F, -3.5F, 4, 1, 2);
        ponytail_6.setRotationPoint(2.5F, -2.2F, 3F);
        ponytail_6.setTextureSize(128, 64);
        ponytail_6.mirror = true;
        setRotation(ponytail_6, 0.25F, 1.58825F, -1.012291F);

        ponytail_7 = new ModelRenderer(this, 0, 45);
        ponytail_7.addBox(-0.5F, -5.5F, -3.5F, 3, 1, 2);
        ponytail_7.setRotationPoint(2.5F, -4.7F, 3F);
        ponytail_7.setTextureSize(128, 64);
        ponytail_7.mirror = true;
        setRotation(ponytail_7, 0.25F, 1.500983F, -2.164208F);

        ponytail_8 = new ModelRenderer(this, 0, 45);
        ponytail_8.addBox(-1.5F, -5.5F, -3.5F, 1, 1, 5);
        ponytail_8.setRotationPoint(1F, -3.7F, 2.1F);
        ponytail_8.setTextureSize(128, 64);
        ponytail_8.mirror = true;
        setRotation(ponytail_8, 0.25F, 1.58825F, -1.012291F);

        ponytail_9 = new ModelRenderer(this, 0, 45);
        ponytail_9.addBox(-1.5F, -5.5F, -3.5F, 3, 1, 2);
        ponytail_9.setRotationPoint(2.5F, -5.5F, 3F);
        ponytail_9.setTextureSize(128, 64);
        ponytail_9.mirror = true;
        setRotation(ponytail_9, 0.25F, 1.692969F, -2.164208F);

        ponytail_10 = new ModelRenderer(this, 0, 45);
        ponytail_10.addBox(-2.5F, -5.5F, -3.5F, 2, 1, 4);
        ponytail_10.setRotationPoint(1.6F, -4.7F, 1.4F);
        ponytail_10.setTextureSize(128, 64);
        ponytail_10.mirror = true;
        setRotation(ponytail_10, 0.25F, 1.58825F, -1.012291F);

        ponytail_11 = new ModelRenderer(this, 0, 45);
        ponytail_11.addBox(-1.5F, -5.5F, -3.5F, 1, 1, 4);
        ponytail_11.setRotationPoint(1.5F, -2.9F, 2.6F);
        ponytail_11.setTextureSize(128, 64);
        ponytail_11.mirror = true;
        setRotation(ponytail_11, 0.25F, 1.58825F, -1.012291F);

        hairbackbuttom1 = new ModelRenderer(this, 0, 45);
        hairbackbuttom1.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1);
        hairbackbuttom1.setRotationPoint(0F, 0F, 0F);
        hairbackbuttom1.setTextureSize(128, 64);
        hairbackbuttom1.mirror = true;
        setRotation(hairbackbuttom1, 0F, 0F, 0F);

        hairbackTop_1 = new ModelRenderer(this, 0, 45);
        hairbackTop_1.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9);
        hairbackTop_1.setRotationPoint(0F, 0F, 0F);
        hairbackTop_1.setTextureSize(128, 64);
        hairbackTop_1.mirror = true;
        setRotation(hairbackTop_1, 0F, 0F, 0F);

        hairbackTop_2 = new ModelRenderer(this, 0, 45);
        hairbackTop_2.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 8);
        hairbackTop_2.setRotationPoint(0F, 0F, -3F);
        hairbackTop_2.setTextureSize(128, 64);
        hairbackTop_2.mirror = true;
        setRotation(hairbackTop_2, 0F, 0F, 0F);

        hairbackTop_3 = new ModelRenderer(this, 0, 45);
        hairbackTop_3.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 8);
        hairbackTop_3.setRotationPoint(0F, 0F, -4F);
        hairbackTop_3.setTextureSize(128, 64);
        hairbackTop_3.mirror = true;
        setRotation(hairbackTop_3, 0F, 0F, 0F);

        hairBackTop_4 = new ModelRenderer(this, 0, 45);
        hairBackTop_4.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3);
        hairBackTop_4.setRotationPoint(0F, 0F, 0F);
        hairBackTop_4.setTextureSize(128, 64);
        hairBackTop_4.mirror = true;
        setRotation(hairBackTop_4, 0F, 0F, 0F);

        hairfrontTop_1 = new ModelRenderer(this, 0, 45);
        hairfrontTop_1.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1);
        hairfrontTop_1.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_1.setTextureSize(128, 64);
        hairfrontTop_1.mirror = true;
        setRotation(hairfrontTop_1, 0F, 0F, 0F);

        hairfrontTop_2 = new ModelRenderer(this, 0, 45);
        hairfrontTop_2.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1);
        hairfrontTop_2.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_2.setTextureSize(128, 64);
        hairfrontTop_2.mirror = true;
        setRotation(hairfrontTop_2, 0F, 0F, 0F);

        hairfrontTop_3 = new ModelRenderer(this, 0, 45);
        hairfrontTop_3.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1);
        hairfrontTop_3.setRotationPoint(0F, 0F, 0F);
        hairfrontTop_3.setTextureSize(128, 64);
        hairfrontTop_3.mirror = true;
        setRotation(hairfrontTop_3, 0F, 0F, 0F);

        hairTop_2 = new ModelRenderer(this, 0, 45);
        hairTop_2.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9);
        hairTop_2.setRotationPoint(0F, 0F, 0F);
        hairTop_2.setTextureSize(128, 64);
        hairTop_2.mirror = true;
        setRotation(hairTop_2, 0F, 0F, 0F);

        hairLeftTop_1 = new ModelRenderer(this, 0, 45);
        hairLeftTop_1.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8);
        hairLeftTop_1.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_1.setTextureSize(128, 64);
        hairLeftTop_1.mirror = true;
        setRotation(hairLeftTop_1, 0F, 0F, 0F);

        hairLeftTop_2 = new ModelRenderer(this, 0, 45);
        hairLeftTop_2.addBox(2.5F, -5.5F, -3.5F, 2, 1, 8);
        hairLeftTop_2.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_2.setTextureSize(128, 64);
        hairLeftTop_2.mirror = true;
        setRotation(hairLeftTop_2, 0F, 0F, 0F);

        hairLeftTop_3 = new ModelRenderer(this, 0, 45);
        hairLeftTop_3.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2);
        hairLeftTop_3.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_3.setTextureSize(128, 64);
        hairLeftTop_3.mirror = true;
        setRotation(hairLeftTop_3, 0F, 0F, 0F);

        hairLeftTop_4 = new ModelRenderer(this, 0, 45);
        hairLeftTop_4.addBox(2.5F, -3.5F, 1.5F, 2, 4, 3);
        hairLeftTop_4.setRotationPoint(0F, -1F, 0F);
        hairLeftTop_4.setTextureSize(128, 64);
        hairLeftTop_4.mirror = true;
        setRotation(hairLeftTop_4, 0F, 0F, 0F);

        hairLeftTop_5 = new ModelRenderer(this, 0, 45);
        hairLeftTop_5.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8);
        hairLeftTop_5.setRotationPoint(0F, 0F, 0F);
        hairLeftTop_5.setTextureSize(128, 64);
        hairLeftTop_5.mirror = true;
        setRotation(hairLeftTop_5, 0F, 0F, 0F);

        hairRightTop_1 = new ModelRenderer(this, 0, 45);
        hairRightTop_1.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2);
        hairRightTop_1.setRotationPoint(0F, 0F, 0F);
        hairRightTop_1.setTextureSize(128, 64);
        hairRightTop_1.mirror = true;
        setRotation(hairRightTop_1, 0F, 0F, 0F);

        hairTop_1 = new ModelRenderer(this, 0, 45);
        hairTop_1.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        hairTop_1.setRotationPoint(0F, 0F, 0F);
        hairTop_1.setTextureSize(128, 64);
        hairTop_1.mirror = true;
        setRotation(hairTop_1, 0F, 0F, 0F);

        bagR = new ModelRenderer(this, 40, 41);
        bagR.addBox(3F, 0F, 3F, 1, 9, 3);
        bagR.setRotationPoint(0F, 0F, 0F);
        bagR.setTextureSize(128, 64);
        bagR.mirror = true;
        setRotation(bagR, 0F, 0F, 0F);

        bagL = new ModelRenderer(this, 40, 41);
        bagL.addBox(-4F, 0F, 3F, 1, 9, 3);
        bagL.setRotationPoint(0F, 0F, 0F);
        bagL.setTextureSize(128, 64);
        bagL.mirror = true;
        setRotation(bagL, 0F, 0F, 0F);

        bagBack = new ModelRenderer(this, 40, 44);
        bagBack.addBox(-3F, 0F, 2F, 6, 9, 1);
        bagBack.setRotationPoint(0F, 0F, 0F);
        bagBack.setTextureSize(128, 64);
        bagBack.mirror = true;
        setRotation(bagBack, 0F, 0F, 0F);

        bagFront = new ModelRenderer(this, 40, 41);
        bagFront.addBox(-3F, 1F, 6F, 6, 8, 1);
        bagFront.setRotationPoint(0F, 0F, 0F);
        bagFront.setTextureSize(128, 64);
        bagFront.mirror = true;
        setRotation(bagFront, 0F, 0F, 0F);

        bagWheat = new ModelRenderer(this, 56, 41);
        bagWheat.addBox(-3F, 1.5F, 3F, 6, 1, 3);
        bagWheat.setRotationPoint(0F, 0F, 0F);
        bagWheat.setTextureSize(128, 64);
        bagWheat.mirror = true;
        setRotation(bagWheat, 0F, 0F, 0F);

        bagBot = new ModelRenderer(this, 40, 46);
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

        this.bipedHead.addChild(hairbackTop_1);
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
        this.bipedHead.addChild(ponytail_4);
        this.bipedHead.addChild(ponytail_5);
        this.bipedHead.addChild(ponytail_6);
        this.bipedHead.addChild(ponytail_7);
        this.bipedHead.addChild(ponytail_8);
        this.bipedHead.addChild(ponytail_9);
        this.bipedHead.addChild(ponytail_10);
        this.bipedHead.addChild(ponytail_11);

        this.bipedHead.addChild(hairRightTop_1);
        this.bipedHead.addChild(hairfrontTop_1);
        this.bipedHead.addChild(hairfrontTop_2);
        this.bipedHead.addChild(hairfrontTop_3);
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

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotation(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
