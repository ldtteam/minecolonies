// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityBlacksmithFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBlacksmithFemale()
    {
        ModelRenderer headdetail;
        ModelRenderer hair;
        ModelRenderer hairF1;
        ModelRenderer hairF2;
        ModelRenderer hairF3;
        ModelRenderer hairF4;
        ModelRenderer hairF5;
        ModelRenderer hairF6;
        ModelRenderer hairF7;
        ModelRenderer hairF8;
        ModelRenderer hairBack;
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
        ModelRenderer hairBand;
        ModelRenderer chest;

        textureWidth = 128;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 6.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 6.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 14.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 14.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setRotationPoint(0.0F, 20.0F, 0.0F);
        bipedHead.addChild(headdetail);
        headdetail.setTextureOffset(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hair);
        

        hairF1 = new ModelRenderer(this);
        hairF1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF1);
        hairF1.setTextureOffset(0, 37).addBox(1.85F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF2 = new ModelRenderer(this);
        hairF2.setRotationPoint(0.0F, 0.5F, 0.0F);
        hair.addChild(hairF2);
        hairF2.setTextureOffset(0, 41).addBox(-4.5F, -3.0F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF3 = new ModelRenderer(this);
        hairF3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF3);
        hairF3.setTextureOffset(10, 38).addBox(-2.95F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF4 = new ModelRenderer(this);
        hairF4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF4);
        hairF4.setTextureOffset(20, 39).addBox(-1.3F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF5 = new ModelRenderer(this);
        hairF5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF5);
        hairF5.setTextureOffset(30, 40).addBox(0.35F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF6 = new ModelRenderer(this);
        hairF6.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF6);
        hairF6.setTextureOffset(41, 42).addBox(3.2F, -3.5F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairF7 = new ModelRenderer(this);
        hairF7.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF7);
        hairF7.setTextureOffset(11, 41).addBox(3.5F, -2.5F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF8 = new ModelRenderer(this);
        hairF8.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF8);
        hairF8.setTextureOffset(49, 42).addBox(3.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairBack = new ModelRenderer(this);
        hairBack.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack);
        hairBack.setTextureOffset(0, 48).addBox(-4.0F, -4.5F, -3.5F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        hairF9 = new ModelRenderer(this);
        hairF9.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF9);
        hairF9.setTextureOffset(2, 48).addBox(3.4F, -4.2F, -4.3F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF10 = new ModelRenderer(this);
        hairF10.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF10);
        hairF10.setTextureOffset(25, 53).addBox(-3.7F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF11 = new ModelRenderer(this);
        hairF11.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF11);
        hairF11.setTextureOffset(26, 50).addBox(-0.55F, -2.5F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF12 = new ModelRenderer(this);
        hairF12.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF12);
        hairF12.setTextureOffset(31, 50).addBox(1.6F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF13 = new ModelRenderer(this);
        hairF13.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF13);
        hairF13.setTextureOffset(32, 52).addBox(0.6F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF14 = new ModelRenderer(this);
        hairF14.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF14);
        hairF14.setTextureOffset(38, 50).addBox(-1.7F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF15 = new ModelRenderer(this);
        hairF15.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF15);
        hairF15.setTextureOffset(22, 38).addBox(-4.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF16 = new ModelRenderer(this);
        hairF16.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF16);
        hairF16.setTextureOffset(32, 43).addBox(-4.6F, -4.3F, -4.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF17 = new ModelRenderer(this);
        hairF17.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF17);
        hairF17.setTextureOffset(59, 43).addBox(-4.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF18 = new ModelRenderer(this);
        hairF18.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF18);
        hairF18.setTextureOffset(42, 37).addBox(1.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack1);
        hairBack1.setTextureOffset(51, 38).addBox(2.1F, -4.4F, 3.6F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF19 = new ModelRenderer(this);
        hairF19.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF19);
        hairF19.setTextureOffset(61, 39).addBox(-2.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF20 = new ModelRenderer(this);
        hairF20.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF20);
        hairF20.setTextureOffset(70, 39).addBox(-0.4F, -4.6F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF21 = new ModelRenderer(this);
        hairF21.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF21);
        hairF21.setTextureOffset(0, 58).addBox(2.8F, -4.4F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF22 = new ModelRenderer(this);
        hairF22.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF22);
        hairF22.setTextureOffset(4, 58).addBox(3.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBack2 = new ModelRenderer(this);
        hairBack2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack2);
        hairBack2.setTextureOffset(8, 58).addBox(-2.9F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairBack3 = new ModelRenderer(this);
        hairBack3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack3);
        hairBack3.setTextureOffset(12, 58).addBox(-1.2F, -4.4F, 3.6F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        hairBack4 = new ModelRenderer(this);
        hairBack4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack4);
        hairBack4.setTextureOffset(16, 58).addBox(0.3F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF23 = new ModelRenderer(this);
        hairF23.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF23);
        hairF23.setTextureOffset(20, 58).addBox(-3.8F, -4.4F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF24 = new ModelRenderer(this);
        hairF24.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF24);
        hairF24.setTextureOffset(36, 53).addBox(3.3F, 2.5F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF25 = new ModelRenderer(this);
        hairF25.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF25);
        hairF25.setTextureOffset(24, 58).addBox(-4.3F, -2.5F, -2.0F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        hairF26 = new ModelRenderer(this);
        hairF26.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF26);
        hairF26.setTextureOffset(28, 58).addBox(-4.3F, 1.5F, -0.4F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF27 = new ModelRenderer(this);
        hairF27.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF27);
        hairF27.setTextureOffset(32, 58).addBox(-4.3F, -1.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF28 = new ModelRenderer(this);
        hairF28.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF28);
        hairF28.setTextureOffset(36, 58).addBox(-4.3F, 1.5F, 1.1F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF29 = new ModelRenderer(this);
        hairF29.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF29);
        hairF29.setTextureOffset(33, 55).addBox(-4.3F, -2.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF30 = new ModelRenderer(this);
        hairF30.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF30);
        hairF30.setTextureOffset(43, 51).addBox(-4.3F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF31 = new ModelRenderer(this);
        hairF31.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF31);
        hairF31.setTextureOffset(40, 55).addBox(-4.3F, -0.5F, -0.7F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF32 = new ModelRenderer(this);
        hairF32.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF32);
        hairF32.setTextureOffset(38, 56).addBox(-4.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF33 = new ModelRenderer(this);
        hairF33.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF33);
        hairF33.setTextureOffset(46, 57).addBox(-0.55F, -4.5F, -4.7F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairF34 = new ModelRenderer(this);
        hairF34.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF34);
        hairF34.setTextureOffset(51, 53).addBox(-4.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF35 = new ModelRenderer(this);
        hairF35.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF35);
        hairF35.setTextureOffset(55, 53).addBox(-4.4F, -4.2F, -4.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF36 = new ModelRenderer(this);
        hairF36.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF36);
        hairF36.setTextureOffset(59, 53).addBox(3.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF37 = new ModelRenderer(this);
        hairF37.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF37);
        hairF37.setTextureOffset(63, 53).addBox(3.3F, 0.5F, -0.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF38 = new ModelRenderer(this);
        hairF38.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF38);
        hairF38.setTextureOffset(67, 53).addBox(3.3F, -0.5F, -1.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF39 = new ModelRenderer(this);
        hairF39.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF39);
        hairF39.setTextureOffset(71, 53).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF40 = new ModelRenderer(this);
        hairF40.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF40);
        hairF40.setTextureOffset(52, 58).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF41 = new ModelRenderer(this);
        hairF41.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF41);
        hairF41.setTextureOffset(56, 57).addBox(3.3F, -0.5F, 0.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF42 = new ModelRenderer(this);
        hairF42.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF42);
        hairF42.setTextureOffset(60, 57).addBox(3.3F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF43 = new ModelRenderer(this);
        hairF43.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF43);
        hairF43.setTextureOffset(64, 57).addBox(3.3F, 2.5F, 1.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF44 = new ModelRenderer(this);
        hairF44.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF44);
        hairF44.setTextureOffset(68, 57).addBox(3.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF45 = new ModelRenderer(this);
        hairF45.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF45);
        hairF45.setTextureOffset(72, 57).addBox(3.3F, -2.5F, 1.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF46 = new ModelRenderer(this);
        hairF46.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF46);
        hairF46.setTextureOffset(0, 67).addBox(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF47 = new ModelRenderer(this);
        hairF47.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF47);
        hairF47.setTextureOffset(14, 70).addBox(-4.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBand1 = new ModelRenderer(this);
        hairBand1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand1);
        hairBand1.setTextureOffset(20, 69).addBox(-3.5F, -3.6F, -4.1F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        hairBand2 = new ModelRenderer(this);
        hairBand2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand2);
        hairBand2.setTextureOffset(18, 71).addBox(-4.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairBand = new ModelRenderer(this);
        hairBand.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand);
        hairBand.setTextureOffset(29, 64).addBox(3.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, 1.0782F, 0.0F, 0.0F);
        chest.setTextureOffset(0, 30).addBox(-3.5F, -1.0001F, -5.0F, 7.0F, 3.0F, 3.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
