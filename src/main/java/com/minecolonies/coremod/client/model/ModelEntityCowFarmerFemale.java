package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCowFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityCowFarmerFemale()
    {
        ModelRenderer bagR;
        ModelRenderer bagL;
        ModelRenderer bagBack;
        ModelRenderer bagFront;
        ModelRenderer bagWheat;
        ModelRenderer bagBot;

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

        textureWidth = 128;
        textureHeight = 64;

        hairLeftTop_8 = new ModelRenderer(this, 0, 45);
        hairLeftTop_8.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_8.addCuboid(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        hairLeftTop_6 = new ModelRenderer(this, 0, 45);
        hairLeftTop_6.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_6.addCuboid(-4.5F, -5.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_5 = new ModelRenderer(this, 0, 45);
        hairLeftTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_5.addCuboid(-4.5F, -4.5F, 0.5F, 2, 1, 4, 0.0F);

        hairLeftTop_7 = new ModelRenderer(this, 0, 45);
        hairLeftTop_7.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_7.addCuboid(-4.5F, -5.5F, -3.5F, 9, 1, 1, 0.0F);

        hairTop_3 = new ModelRenderer(this, 0, 45);
        hairTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_3.addCuboid(2.5F, -7.5F, -4.5F, 2, 1, 9, 0.0F);

        bagL = new ModelRenderer(this, 812, 425);
        bagL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagL.addCuboid(-4.0F, 0.0F, 3.0F, 1, 9, 3, 0.0F);

        hairLeftTop_10 = new ModelRenderer(this, 0, 45);
        hairLeftTop_10.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_10.addCuboid(2.5F, -5.5F, -0.5F, 2, 2, 5, 0.0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addCuboid(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addCuboid(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

        hairLeftTop_12 = new ModelRenderer(this, 0, 45);
        hairLeftTop_12.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_12.addCuboid(3.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        bagBot = new ModelRenderer(this, 808, 426);
        bagBot.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagBot.addCuboid(-3.0F, 9.0F, 3.0F, 6, 1, 3, 0.0F);

        bagR = new ModelRenderer(this, 811, 425);
        bagR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagR.addCuboid(3.0F, 0.0F, 3.0F, 1, 9, 3, 0.0F);

        hairLeftTop_2 = new ModelRenderer(this, 0, 45);
        hairLeftTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_2.addCuboid(-3.5F, -0.5F, 3.5F, 7, 3, 1, 0.0F);

        hairTop_2 = new ModelRenderer(this, 0, 45);
        hairTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_2.addCuboid(-4.5F, -8.5F, -4.5F, 9, 1, 9, 0.0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addCuboid(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);

        hairLeftTop = new ModelRenderer(this, 0, 45);
        hairLeftTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop.addCuboid(-1.5F, -7.5F, -4.5F, 4, 1, 8, 0.0F);

        hairTop_4 = new ModelRenderer(this, 0, 45);
        hairTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_4.addCuboid(3.5F, -6.5F, -4.5F, 1, 3, 1, 0.0F);

        hairLeftTop_1 = new ModelRenderer(this, 0, 45);
        hairLeftTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_1.addCuboid(2.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairTop_1 = new ModelRenderer(this, 0, 45);
        hairTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop_1.addCuboid(2.5F, -6.5F, -4.5F, 1, 1, 1, 0.0F);

        hairLeftTop_9 = new ModelRenderer(this, 0, 45);
        hairLeftTop_9.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_9.addCuboid(2.5F, -6.5F, -3.5F, 2, 1, 8, 0.0F);

        bagFront = new ModelRenderer(this, 813, 430);
        bagFront.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagFront.addCuboid(-3.0F, 1.0F, 6.0F, 6, 8, 1, 0.0F);

        bipedChest = new ModelRenderer(this, 40, 32);
        bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedChest.addCuboid(-3.5F, 2.700000047683716F, -0.5F, 7, 3, 4, 0.0F);
        setRotation(bipedChest, -0.593411922454834F, -0.0F, 0.0F);

        bagWheat = new ModelRenderer(this, 830, 428);
        bagWheat.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagWheat.addCuboid(-3.0F, 1.5F, 3.0F, 6, 1, 3, 0.0F);

        bagBack = new ModelRenderer(this, 813, 425);
        bagBack.setRotationPoint(0.0F, 0.0F, 0.0F);
        bagBack.addCuboid(-3.0F, 0.0F, 2.0F, 6, 9, 1, 0.0F);

        hairLeftTop_3 = new ModelRenderer(this, 0, 45);
        hairLeftTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_3.addCuboid(-4.5F, -7.5F, -4.5F, 3, 2, 9, 0.0F);

        hairLeftTop_4 = new ModelRenderer(this, 0, 45);
        hairLeftTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_4.addCuboid(-4.5F, -5.5F, -0.5F, 2, 1, 5, 0.0F);

        hairLeftTop_13 = new ModelRenderer(this, 0, 45);
        hairLeftTop_13.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_13.addCuboid(-4.5F, -0.5F, 2.5F, 1, 2, 2, 0.0F);

        hairLeftTop_11 = new ModelRenderer(this, 0, 45);
        hairLeftTop_11.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairLeftTop_11.addCuboid(-4.5F, -3.5F, 1.5F, 2, 3, 3, 0.0F);

        hairTop = new ModelRenderer(this, 0, 45);
        hairTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        hairTop.addCuboid(-2.5F, -7.5F, 3.5F, 5, 7, 1, 0.0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addCuboid(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

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

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
