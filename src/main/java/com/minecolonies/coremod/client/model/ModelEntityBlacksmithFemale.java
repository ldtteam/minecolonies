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

        texWidth = 128;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 6.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 6.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 14.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 14.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setPos(0.0F, 20.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hair);


        hairF1 = new ModelRenderer(this);
        hairF1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF1);
        hairF1.texOffs(0, 37).addBox(1.85F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF2 = new ModelRenderer(this);
        hairF2.setPos(0.0F, 0.5F, 0.0F);
        hair.addChild(hairF2);
        hairF2.texOffs(0, 41).addBox(-4.5F, -3.0F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF3 = new ModelRenderer(this);
        hairF3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF3);
        hairF3.texOffs(10, 38).addBox(-2.95F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF4 = new ModelRenderer(this);
        hairF4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF4);
        hairF4.texOffs(20, 39).addBox(-1.3F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF5 = new ModelRenderer(this);
        hairF5.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF5);
        hairF5.texOffs(30, 40).addBox(0.35F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF6 = new ModelRenderer(this);
        hairF6.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF6);
        hairF6.texOffs(41, 42).addBox(3.2F, -3.5F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairF7 = new ModelRenderer(this);
        hairF7.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF7);
        hairF7.texOffs(11, 41).addBox(3.5F, -2.5F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF8 = new ModelRenderer(this);
        hairF8.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF8);
        hairF8.texOffs(49, 42).addBox(3.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairBack = new ModelRenderer(this);
        hairBack.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack);
        hairBack.texOffs(0, 48).addBox(-4.0F, -4.5F, -3.5F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        hairF9 = new ModelRenderer(this);
        hairF9.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF9);
        hairF9.texOffs(2, 48).addBox(3.4F, -4.2F, -4.3F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF10 = new ModelRenderer(this);
        hairF10.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF10);
        hairF10.texOffs(25, 53).addBox(-3.7F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF11 = new ModelRenderer(this);
        hairF11.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF11);
        hairF11.texOffs(26, 50).addBox(-0.55F, -2.5F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF12 = new ModelRenderer(this);
        hairF12.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF12);
        hairF12.texOffs(31, 50).addBox(1.6F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF13 = new ModelRenderer(this);
        hairF13.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF13);
        hairF13.texOffs(32, 52).addBox(0.6F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF14 = new ModelRenderer(this);
        hairF14.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF14);
        hairF14.texOffs(38, 50).addBox(-1.7F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF15 = new ModelRenderer(this);
        hairF15.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF15);
        hairF15.texOffs(22, 38).addBox(-4.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF16 = new ModelRenderer(this);
        hairF16.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF16);
        hairF16.texOffs(32, 43).addBox(-4.6F, -4.3F, -4.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF17 = new ModelRenderer(this);
        hairF17.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF17);
        hairF17.texOffs(59, 43).addBox(-4.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF18 = new ModelRenderer(this);
        hairF18.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF18);
        hairF18.texOffs(42, 37).addBox(1.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack1);
        hairBack1.texOffs(51, 38).addBox(2.1F, -4.4F, 3.6F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF19 = new ModelRenderer(this);
        hairF19.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF19);
        hairF19.texOffs(61, 39).addBox(-2.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF20 = new ModelRenderer(this);
        hairF20.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF20);
        hairF20.texOffs(70, 39).addBox(-0.4F, -4.6F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF21 = new ModelRenderer(this);
        hairF21.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF21);
        hairF21.texOffs(0, 58).addBox(2.8F, -4.4F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF22 = new ModelRenderer(this);
        hairF22.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF22);
        hairF22.texOffs(4, 58).addBox(3.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBack2 = new ModelRenderer(this);
        hairBack2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack2);
        hairBack2.texOffs(8, 58).addBox(-2.9F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairBack3 = new ModelRenderer(this);
        hairBack3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack3);
        hairBack3.texOffs(12, 58).addBox(-1.2F, -4.4F, 3.6F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        hairBack4 = new ModelRenderer(this);
        hairBack4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack4);
        hairBack4.texOffs(16, 58).addBox(0.3F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF23 = new ModelRenderer(this);
        hairF23.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF23);
        hairF23.texOffs(20, 58).addBox(-3.8F, -4.4F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF24 = new ModelRenderer(this);
        hairF24.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF24);
        hairF24.texOffs(36, 53).addBox(3.3F, 2.5F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF25 = new ModelRenderer(this);
        hairF25.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF25);
        hairF25.texOffs(24, 58).addBox(-4.3F, -2.5F, -2.0F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        hairF26 = new ModelRenderer(this);
        hairF26.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF26);
        hairF26.texOffs(28, 58).addBox(-4.3F, 1.5F, -0.4F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF27 = new ModelRenderer(this);
        hairF27.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF27);
        hairF27.texOffs(32, 58).addBox(-4.3F, -1.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF28 = new ModelRenderer(this);
        hairF28.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF28);
        hairF28.texOffs(36, 58).addBox(-4.3F, 1.5F, 1.1F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF29 = new ModelRenderer(this);
        hairF29.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF29);
        hairF29.texOffs(33, 55).addBox(-4.3F, -2.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF30 = new ModelRenderer(this);
        hairF30.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF30);
        hairF30.texOffs(43, 51).addBox(-4.3F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF31 = new ModelRenderer(this);
        hairF31.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF31);
        hairF31.texOffs(40, 55).addBox(-4.3F, -0.5F, -0.7F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF32 = new ModelRenderer(this);
        hairF32.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF32);
        hairF32.texOffs(38, 56).addBox(-4.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF33 = new ModelRenderer(this);
        hairF33.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF33);
        hairF33.texOffs(46, 57).addBox(-0.55F, -4.5F, -4.7F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairF34 = new ModelRenderer(this);
        hairF34.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF34);
        hairF34.texOffs(51, 53).addBox(-4.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF35 = new ModelRenderer(this);
        hairF35.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF35);
        hairF35.texOffs(55, 53).addBox(-4.4F, -4.2F, -4.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF36 = new ModelRenderer(this);
        hairF36.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF36);
        hairF36.texOffs(59, 53).addBox(3.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF37 = new ModelRenderer(this);
        hairF37.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF37);
        hairF37.texOffs(63, 53).addBox(3.3F, 0.5F, -0.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF38 = new ModelRenderer(this);
        hairF38.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF38);
        hairF38.texOffs(67, 53).addBox(3.3F, -0.5F, -1.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF39 = new ModelRenderer(this);
        hairF39.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF39);
        hairF39.texOffs(71, 53).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF40 = new ModelRenderer(this);
        hairF40.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF40);
        hairF40.texOffs(52, 58).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF41 = new ModelRenderer(this);
        hairF41.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF41);
        hairF41.texOffs(56, 57).addBox(3.3F, -0.5F, 0.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF42 = new ModelRenderer(this);
        hairF42.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF42);
        hairF42.texOffs(60, 57).addBox(3.3F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF43 = new ModelRenderer(this);
        hairF43.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF43);
        hairF43.texOffs(64, 57).addBox(3.3F, 2.5F, 1.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF44 = new ModelRenderer(this);
        hairF44.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF44);
        hairF44.texOffs(68, 57).addBox(3.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF45 = new ModelRenderer(this);
        hairF45.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF45);
        hairF45.texOffs(72, 57).addBox(3.3F, -2.5F, 1.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF46 = new ModelRenderer(this);
        hairF46.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF46);
        hairF46.texOffs(0, 67).addBox(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF47 = new ModelRenderer(this);
        hairF47.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF47);
        hairF47.texOffs(14, 70).addBox(-4.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBand1 = new ModelRenderer(this);
        hairBand1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand1);
        hairBand1.texOffs(20, 69).addBox(-3.5F, -3.6F, -4.1F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        hairBand2 = new ModelRenderer(this);
        hairBand2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand2);
        hairBand2.texOffs(18, 71).addBox(-4.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairBand = new ModelRenderer(this);
        hairBand.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand);
        hairBand.texOffs(29, 64).addBox(3.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 4.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setPos(0.0F, 4.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, 1.0782F, 0.0F, 0.0F);
        chest.texOffs(0, 30).addBox(-3.5F, -1.0001F, -5.0F, 7.0F, 3.0F, 3.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
