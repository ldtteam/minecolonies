package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Planter model.
 */
public class ModelEntityPlanterFemale extends CitizenModel<AbstractEntityCitizen>
{
    /**
     * Create the male instance of the model.
     */
    public ModelEntityPlanterFemale()
    {
        ModelRenderer bipedChest;
        ModelRenderer backhair;
        ModelRenderer hairbackbuttom1;
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
        ModelRenderer left_top_1;
        ModelRenderer ponytail_1;
        ModelRenderer ponytail_2;
        ModelRenderer ponytail_3;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedChest = new ModelRenderer(this, 40, 32);
        bipedChest.addBox(-3F, 2.7F, -0.5F, 6, 3, 4);
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

        backhair = new ModelRenderer(this, 0, 45);
        backhair.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1);
        backhair.setRotationPoint(0F, 0F, 0F);
        backhair.setTextureSize(128, 64);
        backhair.mirror = true;
        setRotation(backhair, 0F, 0F, 0F);

        hairbackbuttom1 = new ModelRenderer(this, 0, 45);
        hairbackbuttom1.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1);
        hairbackbuttom1.setRotationPoint(0F, 0F, 0F);
        hairbackbuttom1.setTextureSize(128, 64);
        hairbackbuttom1.mirror = true;
        setRotation(hairbackbuttom1, 0F, 0F, 0F);

        hairbackTop_2 = new ModelRenderer(this, 0, 45);
        hairbackTop_2.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 8);
        hairbackTop_2.setRotationPoint(0F, 0F, -3F);
        hairbackTop_2.setTextureSize(128, 64);
        hairbackTop_2.mirror = true;
        setRotation(hairbackTop_2, 0F, 0F, 0F);

        hairbackTop_3 = new ModelRenderer(this, 0, 45);
        hairbackTop_3.addBox(-4.5F, -4.5F, 0.5F, 2, 3, 8);
        hairbackTop_3.setRotationPoint(0F, 0F, -4F);
        hairbackTop_3.setTextureSize(128, 64);
        hairbackTop_3.mirror = true;
        setRotation(hairbackTop_3, 0F, 0F, 0F);

        hairBackTop_4 = new ModelRenderer(this, 0, 45);
        hairBackTop_4.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 5);
        hairBackTop_4.setRotationPoint(0F, 0F, -2F);
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
        hairLeftTop_2.addBox(2.5F, -5.5F, -3.5F, 2, 3, 8);
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
        hairLeftTop_4.addBox(2.5F, -3.5F, 1.5F, 2, 4, 5);
        hairLeftTop_4.setRotationPoint(0F, -1F, -2F);
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

        left_top_1 = new ModelRenderer(this, 0, 45);
        left_top_1.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9);
        left_top_1.setRotationPoint(0F, 0F, 0F);
        left_top_1.setTextureSize(128, 64);
        left_top_1.mirror = true;
        setRotation(left_top_1, 0F, 0F, 0F);

        ponytail_1 = new ModelRenderer(this, 0, 45);
        ponytail_1.addBox(-7.5F, -7.5F, -4.5F, 1, 1, 4);
        ponytail_1.setRotationPoint(7F, 9.5F, 2F);
        ponytail_1.setTextureSize(128, 64);
        ponytail_1.mirror = true;
        setRotation(ponytail_1, -1.186824F, 0F, 0F);

        ponytail_1.mirror = false;
        ponytail_2 = new ModelRenderer(this, 0, 45);
        ponytail_2.addBox(-7.5F, -7.5F, -4.5F, 3, 2, 4);
        ponytail_2.setRotationPoint(6F, 3F, 0F);
        ponytail_2.setTextureSize(128, 64);
        ponytail_2.mirror = true;
        setRotation(ponytail_2, -1.064651F, 0F, 0F);

        ponytail_3 = new ModelRenderer(this, 0, 45);
        ponytail_3.addBox(-7.5F, -7.5F, -4.5F, 2, 2, 4);
        ponytail_3.setRotationPoint(6.5F, 6F, 1F);
        ponytail_3.setTextureSize(128, 64);
        ponytail_3.mirror = true;
        setRotation(ponytail_3, -1.186824F, 0F, 0F);
        
        bipedHeadwear.showModel = false;

        bipedBody.addChild(bipedChest);
        bipedHead.addChild(backhair);
        bipedHead.addChild(hairbackbuttom1);
        bipedHead.addChild(hairbackTop_2);
        bipedHead.addChild(hairbackTop_3);
        bipedHead.addChild(hairBackTop_4);
        bipedHead.addChild(hairfrontTop_1);
        bipedHead.addChild(hairfrontTop_2);
        bipedHead.addChild(hairfrontTop_3);
        bipedHead.addChild(hairTop_2);
        bipedHead.addChild(hairLeftTop_1);
        bipedHead.addChild(hairLeftTop_2);
        bipedHead.addChild(hairLeftTop_3);
        bipedHead.addChild(hairLeftTop_4);
        bipedHead.addChild(hairLeftTop_5);
        bipedHead.addChild(hairRightTop_1);
        bipedHead.addChild(hairTop_1);
        bipedHead.addChild(left_top_1);
        bipedHead.addChild(ponytail_1);
        bipedHead.addChild(ponytail_2);
        bipedHead.addChild(ponytail_3);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
