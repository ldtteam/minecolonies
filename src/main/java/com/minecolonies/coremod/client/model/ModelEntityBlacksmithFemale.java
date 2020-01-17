package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBlacksmithFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBlacksmithFemale()
    {
        ModelRenderer chest;
        ModelRenderer hairF1;
        ModelRenderer hairF2;
        ModelRenderer hairF3;
        ModelRenderer hairF4;
        ModelRenderer hairF5;
        ModelRenderer hairF6;
        ModelRenderer hairF7;
        ModelRenderer hairF8;
        ModelRenderer hairBack;
        ModelRenderer hairBand;
        ModelRenderer hairF9;
        ModelRenderer hairF10;
        ModelRenderer hairF11;
        ModelRenderer hairF12;
        ModelRenderer hairF13;
        ModelRenderer hairF14;
        ModelRenderer hairF15;
        ModelRenderer hairF16;
        ModelRenderer hairF17;
        ModelRenderer hairF18;
        ModelRenderer hairBack1;
        ModelRenderer hairF19;
        ModelRenderer hairF20;
        ModelRenderer hairF21;
        ModelRenderer hairF22;
        ModelRenderer hairBack2;
        ModelRenderer hairBack3;
        ModelRenderer hairBack4;
        ModelRenderer hairF23;
        ModelRenderer hairF24;
        ModelRenderer hairF25;
        ModelRenderer hairF26;
        ModelRenderer hairF27;
        ModelRenderer hairF28;
        ModelRenderer hairF29;
        ModelRenderer hairF30;
        ModelRenderer hairF31;
        ModelRenderer hairF32;
        ModelRenderer hairF33;
        ModelRenderer hairF34;
        ModelRenderer hairF35;
        ModelRenderer hairF36;
        ModelRenderer hairF37;
        ModelRenderer hairF38;
        ModelRenderer hairF39;
        ModelRenderer hairF40;
        ModelRenderer hairF41;
        ModelRenderer hairF42;
        ModelRenderer hairF43;
        ModelRenderer hairF44;
        ModelRenderer hairF45;
        ModelRenderer hairF46;
        ModelRenderer hairF47;
        ModelRenderer hairBand1;
        ModelRenderer hairBand2;

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-3F, 2F, -2F, 4, 10, 4);
        bipedRightArm.setRotationPoint(-5F, 6F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(-1F, 2F, -2F, 4, 10, 4);
        bipedLeftArm.setRotationPoint(5F, 6F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 2F, -2F, 4, 10, 4);
        bipedRightLeg.setRotationPoint(-2F, 14F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addCuboid(-2F, 2F, -2F, 4, 10, 4);
        bipedLeftLeg.setRotationPoint(2F, 14F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 0, 30);
        chest.addCuboid(-3.5F, -1F, -5F, 7, 3, 3);
        chest.setRotationPoint(0F, 4F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, 1.07818F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -4F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 4F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 4F, -2F, 8, 10, 4);
        bipedBody.setRotationPoint(0F, 4F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        hairF1 = new ModelRenderer(this, 70, 4);
        hairF1.addCuboid(1.85F, -8.7F, -3.6F, 1, 1, 8);
        hairF1.setRotationPoint(0F, 4F, 0F);
        hairF1.setTextureSize(256, 128);
        hairF1.mirror = true;
        setRotation(hairF1, 0F, 0F, 0F);

        hairF2 = new ModelRenderer(this, 57, 0);
        hairF2.addCuboid(-4.5F, -7F, 2.5F, 1, 1, 2);
        hairF2.setRotationPoint(0F, 4.5F, 0F);
        hairF2.setTextureSize(256, 128);
        hairF2.mirror = true;
        setRotation(hairF2, 0F, 0F, 0F);

        hairF3 = new ModelRenderer(this, 67, 3);
        hairF3.addCuboid(-2.95F, -8.7F, -3.6F, 1, 1, 8);
        hairF3.setRotationPoint(0F, 4F, 0F);
        hairF3.setTextureSize(256, 128);
        hairF3.mirror = true;
        setRotation(hairF3, 0F, 0F, 0F);

        hairF4 = new ModelRenderer(this, 65, 4);
        hairF4.addCuboid(-1.3F, -8.7F, -3.7F, 1, 1, 8);
        hairF4.setRotationPoint(0F, 4F, 0F);
        hairF4.setTextureSize(256, 128);
        hairF4.mirror = true;
        setRotation(hairF4, 0F, 0F, 0F);

        hairF5 = new ModelRenderer(this, 67, 4);
        hairF5.addCuboid(0.35F, -8.7F, -3.7F, 1, 1, 8);
        hairF5.setRotationPoint(0F, 4F, 0F);
        hairF5.setTextureSize(256, 128);
        hairF5.mirror = true;
        setRotation(hairF5, 0F, 0F, 0F);

        hairF6 = new ModelRenderer(this, 57, 0);
        hairF6.addCuboid(3.2F, -7.5F, -3.5F, 1, 1, 7);
        hairF6.setRotationPoint(0F, 4F, 0F);
        hairF6.setTextureSize(256, 128);
        hairF6.mirror = true;
        setRotation(hairF6, 0F, 0F, 0F);

        hairF7 = new ModelRenderer(this, 57, 0);
        hairF7.addCuboid(3.5F, -6.5F, 2.5F, 1, 1, 2);
        hairF7.setRotationPoint(0F, 4F, 0F);
        hairF7.setTextureSize(256, 128);
        hairF7.mirror = true;
        setRotation(hairF7, 0F, 0F, 0F);

        hairF8 = new ModelRenderer(this, 64, 8);
        hairF8.addCuboid(3.5F, -8.6F, -3.4F, 1, 1, 8);
        hairF8.setRotationPoint(0F, 4F, 0F);
        hairF8.setTextureSize(256, 128);
        hairF8.mirror = true;
        setRotation(hairF8, 0F, 0F, 0F);

        hairBack = new ModelRenderer(this, 57, 16);
        hairBack.addCuboid(-4F, -8.5F, -3.5F, 8, 1, 8);
        hairBack.setRotationPoint(0F, 4F, 0F);
        hairBack.setTextureSize(256, 128);
        hairBack.mirror = true;
        setRotation(hairBack, 0F, 0F, 0F);

        hairBand = new ModelRenderer(this, 57, 32);
        hairBand.addCuboid(3.3F, -7.6F, -3.5F, 1, 1, 7);
        hairBand.setRotationPoint(0F, 4F, 0F);
        hairBand.setTextureSize(256, 128);
        hairBand.mirror = true;
        setRotation(hairBand, 0F, 0F, 0F);

        hairF9 = new ModelRenderer(this, 57, 0);
        hairF9.addCuboid(3.4F, -8.2F, -4.3F, 1, 4, 1);
        hairF9.setRotationPoint(0F, 4F, 0F);
        hairF9.setTextureSize(256, 128);
        hairF9.mirror = true;
        setRotation(hairF9, 0F, 0F, 0F);

        hairF10 = new ModelRenderer(this, 57, 0);
        hairF10.addCuboid(-3.7F, -8.5F, -4.5F, 2, 1, 1);
        hairF10.setRotationPoint(0F, 4F, 0F);
        hairF10.setTextureSize(256, 128);
        hairF10.mirror = true;
        setRotation(hairF10, 0F, 0F, 0F);

        hairF11 = new ModelRenderer(this, 57, 0);
        hairF11.addCuboid(-0.55F, -6.5F, -4.2F, 1, 1, 1);
        hairF11.setRotationPoint(0F, 4F, 0F);
        hairF11.setTextureSize(256, 128);
        hairF11.mirror = true;
        setRotation(hairF11, 0F, 0F, 0F);

        hairF12 = new ModelRenderer(this, 57, 0);
        hairF12.addCuboid(1.6F, -8.5F, -4.5F, 2, 1, 1);
        hairF12.setRotationPoint(0F, 4F, 0F);
        hairF12.setTextureSize(256, 128);
        hairF12.mirror = true;
        setRotation(hairF12, 0F, 0F, 0F);

        hairF13 = new ModelRenderer(this, 57, 0);
        hairF13.addCuboid(0.6F, -8.5F, -4.5F, 1, 2, 1);
        hairF13.setRotationPoint(0F, 4F, 0F);
        hairF13.setTextureSize(256, 128);
        hairF13.mirror = true;
        setRotation(hairF13, 0F, 0F, 0F);

        hairF14 = new ModelRenderer(this, 57, 0);
        hairF14.addCuboid(-1.7F, -8.5F, -4.5F, 1, 2, 1);
        hairF14.setRotationPoint(0F, 4F, 0F);
        hairF14.setTextureSize(256, 128);
        hairF14.mirror = true;
        setRotation(hairF14, 0F, 0F, 0F);

        hairF15 = new ModelRenderer(this, 57, 0);
        hairF15.addCuboid(-4.3F, -6.6F, 2.4F, 1, 7, 1);
        hairF15.setRotationPoint(0F, 4F, 0F);
        hairF15.setTextureSize(256, 128);
        hairF15.mirror = true;
        setRotation(hairF15, 0F, 0F, 0F);

        hairF16 = new ModelRenderer(this, 57, 0);
        hairF16.addCuboid(-4.6F, -8.3F, -4F, 1, 2, 1);
        hairF16.setRotationPoint(0F, 4F, 0F);
        hairF16.setTextureSize(256, 128);
        hairF16.mirror = true;
        setRotation(hairF16, 0F, 0F, 0F);

        hairF17 = new ModelRenderer(this, 67, 0);
        hairF17.addCuboid(-4.5F, -8.6F, -3.4F, 1, 1, 8);
        hairF17.setRotationPoint(0F, 4F, 0F);
        hairF17.setTextureSize(256, 128);
        hairF17.mirror = true;
        setRotation(hairF17, 0F, 0F, 0F);

        hairF18 = new ModelRenderer(this, 59, 0);
        hairF18.addCuboid(1.1F, -8.6F, 3.7F, 1, 9, 1);
        hairF18.setRotationPoint(0F, 4F, 0F);
        hairF18.setTextureSize(256, 128);
        hairF18.mirror = true;
        setRotation(hairF18, 0F, 0F, 0F);

        hairBack1 = new ModelRenderer(this, 58, 18);
        hairBack1.addCuboid(2.1F, -8.4F, 3.6F, 1, 9, 1);
        hairBack1.setRotationPoint(0F, 4F, 0F);
        hairBack1.setTextureSize(256, 128);
        hairBack1.mirror = true;
        setRotation(hairBack1, 0F, 0F, 0F);

        hairF19 = new ModelRenderer(this, 60, 0);
        hairF19.addCuboid(-2.1F, -8.6F, 3.7F, 1, 9, 1);
        hairF19.setRotationPoint(0F, 4F, 0F);
        hairF19.setTextureSize(256, 128);
        hairF19.mirror = true;
        setRotation(hairF19, 0F, 0F, 0F);

        hairF20 = new ModelRenderer(this, 60, 0);
        hairF20.addCuboid(-0.4F, -8.6F, 3.7F, 1, 10, 1);
        hairF20.setRotationPoint(0F, 4F, 0F);
        hairF20.setTextureSize(256, 128);
        hairF20.mirror = true;
        setRotation(hairF20, 0F, 0F, 0F);

        hairF21 = new ModelRenderer(this, 58, 0);
        hairF21.addCuboid(2.8F, -8.4F, 3.7F, 1, 10, 1);
        hairF21.setRotationPoint(0F, 4F, 0F);
        hairF21.setTextureSize(256, 128);
        hairF21.mirror = true;
        setRotation(hairF21, 0F, 0F, 0F);

        hairF22 = new ModelRenderer(this, 57, 0);
        hairF22.addCuboid(3.4F, -7.9F, 3.2F, 1, 8, 1);
        hairF22.setRotationPoint(0F, 4F, 0F);
        hairF22.setTextureSize(256, 128);
        hairF22.mirror = true;
        setRotation(hairF22, 0F, 0F, 0F);

        hairBack2 = new ModelRenderer(this, 61, 17);
        hairBack2.addCuboid(-2.9F, -8.4F, 3.6F, 1, 10, 1);
        hairBack2.setRotationPoint(0F, 4F, 0F);
        hairBack2.setTextureSize(256, 128);
        hairBack2.mirror = true;
        setRotation(hairBack2, 0F, 0F, 0F);

        hairBack3 = new ModelRenderer(this, 58, 15);
        hairBack3.addCuboid(-1.2F, -8.4F, 3.6F, 1, 11, 1);
        hairBack3.setRotationPoint(0F, 4F, 0F);
        hairBack3.setTextureSize(256, 128);
        hairBack3.mirror = true;
        setRotation(hairBack3, 0F, 0F, 0F);

        hairBack4 = new ModelRenderer(this, 57, 16);
        hairBack4.addCuboid(0.3F, -8.4F, 3.6F, 1, 10, 1);
        hairBack4.setRotationPoint(0F, 4F, 0F);
        hairBack4.setTextureSize(256, 128);
        hairBack4.mirror = true;
        setRotation(hairBack4, 0F, 0F, 0F);

        hairF23 = new ModelRenderer(this, 58, 1);
        hairF23.addCuboid(-3.8F, -8.4F, 3.7F, 1, 9, 1);
        hairF23.setRotationPoint(0F, 4F, 0F);
        hairF23.setTextureSize(256, 128);
        hairF23.mirror = true;
        setRotation(hairF23, 0F, 0F, 0F);

        hairF24 = new ModelRenderer(this, 57, 0);
        hairF24.addCuboid(3.3F, -1.5F, -2F, 1, 1, 1);
        hairF24.setRotationPoint(0F, 4F, 0F);
        hairF24.setTextureSize(256, 128);
        hairF24.mirror = true;
        setRotation(hairF24, 0F, 0F, 0F);

        hairF25 = new ModelRenderer(this, 57, 0);
        hairF25.addCuboid(-4.3F, -6.5F, -2F, 1, 6, 1);
        hairF25.setRotationPoint(0F, 4F, 0F);
        hairF25.setTextureSize(256, 128);
        hairF25.mirror = true;
        setRotation(hairF25, 0F, 0F, 0F);

        hairF26 = new ModelRenderer(this, 57, 0);
        hairF26.addCuboid(-4.3F, -2.5F, -0.4F, 1, 2, 1);
        hairF26.setRotationPoint(0F, 4F, 0F);
        hairF26.setTextureSize(256, 128);
        hairF26.mirror = true;
        setRotation(hairF26, 0F, 0F, 0F);

        hairF27 = new ModelRenderer(this, 57, 0);
        hairF27.addCuboid(-4.3F, -5.5F, 1.3F, 1, 3, 1);
        hairF27.setRotationPoint(0F, 4F, 0F);
        hairF27.setTextureSize(256, 128);
        hairF27.mirror = true;
        setRotation(hairF27, 0F, 0F, 0F);

        hairF28 = new ModelRenderer(this, 57, 0);
        hairF28.addCuboid(-4.3F, -2.5F, 1.1F, 1, 3, 1);
        hairF28.setRotationPoint(0F, 4F, 0F);
        hairF28.setTextureSize(256, 128);
        hairF28.mirror = true;
        setRotation(hairF28, 0F, 0F, 0F);

        hairF29 = new ModelRenderer(this, 57, 0);
        hairF29.addCuboid(-4.3F, -6.5F, 1F, 1, 1, 1);
        hairF29.setRotationPoint(0F, 4F, 0F);
        hairF29.setTextureSize(256, 128);
        hairF29.mirror = true;
        setRotation(hairF29, 0F, 0F, 0F);

        hairF30 = new ModelRenderer(this, 57, 0);
        hairF30.addCuboid(-4.3F, -6.5F, -0.5F, 1, 2, 1);
        hairF30.setRotationPoint(0F, 4F, 0F);
        hairF30.setTextureSize(256, 128);
        hairF30.mirror = true;
        setRotation(hairF30, 0F, 0F, 0F);

        hairF31 = new ModelRenderer(this, 57, 0);
        hairF31.addCuboid(-4.3F, -4.5F, -0.7F, 1, 2, 1);
        hairF31.setRotationPoint(0F, 4F, 0F);
        hairF31.setTextureSize(256, 128);
        hairF31.mirror = true;
        setRotation(hairF31, 0F, 0F, 0F);

        hairF32 = new ModelRenderer(this, 57, 0);
        hairF32.addCuboid(-4.5F, -7F, -3.5F, 1, 1, 6);
        hairF32.setRotationPoint(0F, 4F, 0F);
        hairF32.setTextureSize(256, 128);
        hairF32.mirror = true;
        setRotation(hairF32, 0F, 0F, 0F);

        hairF33 = new ModelRenderer(this, 57, 0);
        hairF33.addCuboid(-0.55F, -8.5F, -4.7F, 1, 2, 2);
        hairF33.setRotationPoint(0F, 4F, 0F);
        hairF33.setTextureSize(256, 128);
        hairF33.mirror = true;
        setRotation(hairF33, 0F, 0F, 0F);

        hairF34 = new ModelRenderer(this, 57, 0);
        hairF34.addCuboid(-4.3F, -6.5F, -3.5F, 1, 4, 1);
        hairF34.setRotationPoint(0F, 4F, 0F);
        hairF34.setTextureSize(256, 128);
        hairF34.mirror = true;
        setRotation(hairF34, 0F, 0F, 0F);

        hairF35 = new ModelRenderer(this, 57, 0);
        hairF35.addCuboid(-4.4F, -8.2F, -4.3F, 1, 3, 1);
        hairF35.setRotationPoint(0F, 4F, 0F);
        hairF35.setTextureSize(256, 128);
        hairF35.mirror = true;
        setRotation(hairF35, 0F, 0F, 0F);

        hairF36 = new ModelRenderer(this, 57, 0);
        hairF36.addCuboid(3.3F, -6.5F, -3.5F, 1, 4, 1);
        hairF36.setRotationPoint(0F, 4F, 0F);
        hairF36.setTextureSize(256, 128);
        hairF36.mirror = true;
        setRotation(hairF36, 0F, 0F, 0F);

        hairF37 = new ModelRenderer(this, 57, 0);
        hairF37.addCuboid(3.3F, -3.5F, -0.3F, 1, 3, 1);
        hairF37.setRotationPoint(0F, 4F, 0F);
        hairF37.setTextureSize(256, 128);
        hairF37.mirror = true;
        setRotation(hairF37, 0F, 0F, 0F);

        hairF38 = new ModelRenderer(this, 57, 0);
        hairF38.addCuboid(3.3F, -4.5F, -1.8F, 1, 3, 1);
        hairF38.setRotationPoint(0F, 4F, 0F);
        hairF38.setTextureSize(256, 128);
        hairF38.mirror = true;
        setRotation(hairF38, 0F, 0F, 0F);

        hairF39 = new ModelRenderer(this, 57, 0);
        hairF39.addCuboid(3.3F, -6.5F, -2F, 1, 2, 1);
        hairF39.setRotationPoint(0F, 4F, 0F);
        hairF39.setTextureSize(256, 128);
        hairF39.mirror = true;
        setRotation(hairF39, 0F, 0F, 0F);

        hairF40 = new ModelRenderer(this, 57, 0);
        hairF40.addCuboid(3.3F, -6.5F, -2F, 1, 2, 1);
        hairF40.setRotationPoint(0F, 4F, 0F);
        hairF40.setTextureSize(256, 128);
        hairF40.mirror = true;
        setRotation(hairF40, 0F, 0F, 0F);

        hairF41 = new ModelRenderer(this, 57, 0);
        hairF41.addCuboid(3.3F, -4.5F, 0.8F, 1, 3, 1);
        hairF41.setRotationPoint(0F, 4F, 0F);
        hairF41.setTextureSize(256, 128);
        hairF41.mirror = true;
        setRotation(hairF41, 0F, 0F, 0F);

        hairF42 = new ModelRenderer(this, 57, 0);
        hairF42.addCuboid(3.3F, -6.5F, -0.5F, 1, 3, 1);
        hairF42.setRotationPoint(0F, 4F, 0F);
        hairF42.setTextureSize(256, 128);
        hairF42.mirror = true;
        setRotation(hairF42, 0F, 0F, 0F);

        hairF43 = new ModelRenderer(this, 57, 0);
        hairF43.addCuboid(3.3F, -1.5F, 1.1F, 1, 2, 1);
        hairF43.setRotationPoint(0F, 4F, 0F);
        hairF43.setTextureSize(256, 128);
        hairF43.mirror = true;
        setRotation(hairF43, 0F, 0F, 0F);

        hairF44 = new ModelRenderer(this, 57, 0);
        hairF44.addCuboid(3.3F, -6.6F, 2.4F, 1, 7, 1);
        hairF44.setRotationPoint(0F, 4F, 0F);
        hairF44.setTextureSize(256, 128);
        hairF44.mirror = true;
        setRotation(hairF44, 0F, 0F, 0F);

        hairF45 = new ModelRenderer(this, 57, 0);
        hairF45.addCuboid(3.3F, -6.5F, 1F, 1, 2, 1);
        hairF45.setRotationPoint(0F, 4F, 0F);
        hairF45.setTextureSize(256, 128);
        hairF45.mirror = true;
        setRotation(hairF45, 0F, 0F, 0F);

        hairF46 = new ModelRenderer(this, 57, 0);
        hairF46.addCuboid(3.5F, -7F, -3.5F, 1, 1, 6);
        hairF46.setRotationPoint(0F, 4F, 0F);
        hairF46.setTextureSize(256, 128);
        hairF46.mirror = true;
        setRotation(hairF46, 0F, 0F, 0F);

        hairF47 = new ModelRenderer(this, 57, 0);
        hairF47.addCuboid(-4.4F, -7.9F, 3.2F, 1, 8, 1);
        hairF47.setRotationPoint(0F, 4F, 0F);
        hairF47.setTextureSize(256, 128);
        hairF47.mirror = true;
        setRotation(hairF47, 0F, 0F, 0F);

        hairBand1 = new ModelRenderer(this, 57, 32);
        hairBand1.addCuboid(-3.5F, -7.6F, -4.1F, 7, 1, 1);
        hairBand1.setRotationPoint(0F, 4F, 0F);
        hairBand1.setTextureSize(256, 128);
        hairBand1.mirror = true;
        setRotation(hairBand1, 0F, 0F, 0F);

        hairBand2 = new ModelRenderer(this, 57, 32);
        hairBand2.addCuboid(-4.3F, -7.6F, -3.5F, 1, 1, 7);
        hairBand2.setRotationPoint(0F, 4F, 0F);
        hairBand2.setTextureSize(256, 128);
        hairBand2.mirror = true;
        setRotation(hairBand2, 0F, 0F, 0F);

        this.bipedBody.addChild(chest);

        this.bipedHeadwear.addChild(hairBand);
        this.bipedHeadwear.addChild(hairBand1);
        this.bipedHeadwear.addChild(hairBand2);

        this.bipedHeadwear.addChild(hairBack);
        this.bipedHeadwear.addChild(hairBack1);
        this.bipedHeadwear.addChild(hairBack2);
        this.bipedHeadwear.addChild(hairBack3);
        this.bipedHeadwear.addChild(hairBack4);


        this.bipedHeadwear.addChild(hairF1);
        this.bipedHeadwear.addChild(hairF2);
        this.bipedHeadwear.addChild(hairF3);
        this.bipedHeadwear.addChild(hairF4);
        this.bipedHeadwear.addChild(hairF5);
        this.bipedHeadwear.addChild(hairF6);
        this.bipedHeadwear.addChild(hairF7);
        this.bipedHeadwear.addChild(hairF8);
        this.bipedHeadwear.addChild(hairF9);
        this.bipedHeadwear.addChild(hairF10);
        this.bipedHeadwear.addChild(hairF11);
        this.bipedHeadwear.addChild(hairF12);
        this.bipedHeadwear.addChild(hairF13);
        this.bipedHeadwear.addChild(hairF14);
        this.bipedHeadwear.addChild(hairF15);
        this.bipedHeadwear.addChild(hairF16);
        this.bipedHeadwear.addChild(hairF17);
        this.bipedHeadwear.addChild(hairF18);
        this.bipedHeadwear.addChild(hairF19);
        this.bipedHeadwear.addChild(hairF20);
        this.bipedHeadwear.addChild(hairF21);
        this.bipedHeadwear.addChild(hairF22);
        this.bipedHeadwear.addChild(hairF23);
        this.bipedHeadwear.addChild(hairF24);
        this.bipedHeadwear.addChild(hairF25);
        this.bipedHeadwear.addChild(hairF26);
        this.bipedHeadwear.addChild(hairF27);
        this.bipedHeadwear.addChild(hairF28);
        this.bipedHeadwear.addChild(hairF29);
        this.bipedHeadwear.addChild(hairF30);
        this.bipedHeadwear.addChild(hairF31);
        this.bipedHeadwear.addChild(hairF32);
        this.bipedHeadwear.addChild(hairF33);
        this.bipedHeadwear.addChild(hairF34);
        this.bipedHeadwear.addChild(hairF35);
        this.bipedHeadwear.addChild(hairF36);
        this.bipedHeadwear.addChild(hairF37);
        this.bipedHeadwear.addChild(hairF38);
        this.bipedHeadwear.addChild(hairF39);
        this.bipedHeadwear.addChild(hairF40);
        this.bipedHeadwear.addChild(hairF41);
        this.bipedHeadwear.addChild(hairF42);
        this.bipedHeadwear.addChild(hairF43);
        this.bipedHeadwear.addChild(hairF44);
        this.bipedHeadwear.addChild(hairF45);
        this.bipedHeadwear.addChild(hairF46);
        this.bipedHeadwear.addChild(hairF47);

    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
