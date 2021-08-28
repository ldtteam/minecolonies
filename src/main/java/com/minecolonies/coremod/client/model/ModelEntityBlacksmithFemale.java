// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityBlacksmithFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBlacksmithFemale()
    {
        ModelPart headdetail;
        ModelPart hair;
        ModelPart hairF1;
        ModelPart hairF2;
        ModelPart hairF3;
        ModelPart hairF4;
        ModelPart hairF5;
        ModelPart hairF6;
        ModelPart hairF7;
        ModelPart hairF8;
        ModelPart hairBack;
        ModelPart hairF9;
        ModelPart hairF10;
        ModelPart hairF11;
        ModelPart hairF12;
        ModelPart hairF13;
        ModelPart hairF14;
        ModelPart hairF15;
        ModelPart hairF16;
        ModelPart hairF17;
        ModelPart hairF18;
        ModelPart hairBack1;
        ModelPart hairF19;
        ModelPart hairF20;
        ModelPart hairF21;
        ModelPart hairF22;
        ModelPart hairBack2;
        ModelPart hairBack3;
        ModelPart hairBack4;
        ModelPart hairF23;
        ModelPart hairF24;
        ModelPart hairF25;
        ModelPart hairF26;
        ModelPart hairF27;
        ModelPart hairF28;
        ModelPart hairF29;
        ModelPart hairF30;
        ModelPart hairF31;
        ModelPart hairF32;
        ModelPart hairF33;
        ModelPart hairF34;
        ModelPart hairF35;
        ModelPart hairF36;
        ModelPart hairF37;
        ModelPart hairF38;
        ModelPart hairF39;
        ModelPart hairF40;
        ModelPart hairF41;
        ModelPart hairF42;
        ModelPart hairF43;
        ModelPart hairF44;
        ModelPart hairF45;
        ModelPart hairF46;
        ModelPart hairF47;
        ModelPart hairBand1;
        ModelPart hairBand2;
        ModelPart hairBand;
        ModelPart chest;

        texWidth = 128;
        texHeight = 128;

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 6.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 6.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 14.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 14.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelPart(this);
        headdetail.setPos(0.0F, 20.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelPart(this);
        hair.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hair);


        hairF1 = new ModelPart(this);
        hairF1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF1);
        hairF1.texOffs(0, 37).addBox(1.85F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF2 = new ModelPart(this);
        hairF2.setPos(0.0F, 0.5F, 0.0F);
        hair.addChild(hairF2);
        hairF2.texOffs(0, 41).addBox(-4.5F, -3.0F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF3 = new ModelPart(this);
        hairF3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF3);
        hairF3.texOffs(10, 38).addBox(-2.95F, -4.7F, -3.6F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF4 = new ModelPart(this);
        hairF4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF4);
        hairF4.texOffs(20, 39).addBox(-1.3F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF5 = new ModelPart(this);
        hairF5.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF5);
        hairF5.texOffs(30, 40).addBox(0.35F, -4.7F, -3.7F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF6 = new ModelPart(this);
        hairF6.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF6);
        hairF6.texOffs(41, 42).addBox(3.2F, -3.5F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairF7 = new ModelPart(this);
        hairF7.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF7);
        hairF7.texOffs(11, 41).addBox(3.5F, -2.5F, 2.5F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        hairF8 = new ModelPart(this);
        hairF8.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF8);
        hairF8.texOffs(49, 42).addBox(3.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairBack = new ModelPart(this);
        hairBack.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack);
        hairBack.texOffs(0, 48).addBox(-4.0F, -4.5F, -3.5F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        hairF9 = new ModelPart(this);
        hairF9.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF9);
        hairF9.texOffs(2, 48).addBox(3.4F, -4.2F, -4.3F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF10 = new ModelPart(this);
        hairF10.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF10);
        hairF10.texOffs(25, 53).addBox(-3.7F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF11 = new ModelPart(this);
        hairF11.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF11);
        hairF11.texOffs(26, 50).addBox(-0.55F, -2.5F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF12 = new ModelPart(this);
        hairF12.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF12);
        hairF12.texOffs(31, 50).addBox(1.6F, -4.5F, -4.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        hairF13 = new ModelPart(this);
        hairF13.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF13);
        hairF13.texOffs(32, 52).addBox(0.6F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF14 = new ModelPart(this);
        hairF14.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF14);
        hairF14.texOffs(38, 50).addBox(-1.7F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF15 = new ModelPart(this);
        hairF15.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF15);
        hairF15.texOffs(22, 38).addBox(-4.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF16 = new ModelPart(this);
        hairF16.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF16);
        hairF16.texOffs(32, 43).addBox(-4.6F, -4.3F, -4.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF17 = new ModelPart(this);
        hairF17.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF17);
        hairF17.texOffs(59, 43).addBox(-4.5F, -4.6F, -3.4F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        hairF18 = new ModelPart(this);
        hairF18.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF18);
        hairF18.texOffs(42, 37).addBox(1.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairBack1 = new ModelPart(this);
        hairBack1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack1);
        hairBack1.texOffs(51, 38).addBox(2.1F, -4.4F, 3.6F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF19 = new ModelPart(this);
        hairF19.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF19);
        hairF19.texOffs(61, 39).addBox(-2.1F, -4.6F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF20 = new ModelPart(this);
        hairF20.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF20);
        hairF20.texOffs(70, 39).addBox(-0.4F, -4.6F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF21 = new ModelPart(this);
        hairF21.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF21);
        hairF21.texOffs(0, 58).addBox(2.8F, -4.4F, 3.7F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF22 = new ModelPart(this);
        hairF22.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF22);
        hairF22.texOffs(4, 58).addBox(3.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBack2 = new ModelPart(this);
        hairBack2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack2);
        hairBack2.texOffs(8, 58).addBox(-2.9F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairBack3 = new ModelPart(this);
        hairBack3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack3);
        hairBack3.texOffs(12, 58).addBox(-1.2F, -4.4F, 3.6F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        hairBack4 = new ModelPart(this);
        hairBack4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBack4);
        hairBack4.texOffs(16, 58).addBox(0.3F, -4.4F, 3.6F, 1.0F, 10.0F, 1.0F, 0.0F, true);

        hairF23 = new ModelPart(this);
        hairF23.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF23);
        hairF23.texOffs(20, 58).addBox(-3.8F, -4.4F, 3.7F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        hairF24 = new ModelPart(this);
        hairF24.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF24);
        hairF24.texOffs(36, 53).addBox(3.3F, 2.5F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF25 = new ModelPart(this);
        hairF25.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF25);
        hairF25.texOffs(24, 58).addBox(-4.3F, -2.5F, -2.0F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        hairF26 = new ModelPart(this);
        hairF26.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF26);
        hairF26.texOffs(28, 58).addBox(-4.3F, 1.5F, -0.4F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF27 = new ModelPart(this);
        hairF27.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF27);
        hairF27.texOffs(32, 58).addBox(-4.3F, -1.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF28 = new ModelPart(this);
        hairF28.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF28);
        hairF28.texOffs(36, 58).addBox(-4.3F, 1.5F, 1.1F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF29 = new ModelPart(this);
        hairF29.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF29);
        hairF29.texOffs(33, 55).addBox(-4.3F, -2.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairF30 = new ModelPart(this);
        hairF30.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF30);
        hairF30.texOffs(43, 51).addBox(-4.3F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF31 = new ModelPart(this);
        hairF31.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF31);
        hairF31.texOffs(40, 55).addBox(-4.3F, -0.5F, -0.7F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF32 = new ModelPart(this);
        hairF32.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF32);
        hairF32.texOffs(38, 56).addBox(-4.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF33 = new ModelPart(this);
        hairF33.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF33);
        hairF33.texOffs(46, 57).addBox(-0.55F, -4.5F, -4.7F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairF34 = new ModelPart(this);
        hairF34.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF34);
        hairF34.texOffs(51, 53).addBox(-4.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF35 = new ModelPart(this);
        hairF35.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF35);
        hairF35.texOffs(55, 53).addBox(-4.4F, -4.2F, -4.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF36 = new ModelPart(this);
        hairF36.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF36);
        hairF36.texOffs(59, 53).addBox(3.3F, -2.5F, -3.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hairF37 = new ModelPart(this);
        hairF37.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF37);
        hairF37.texOffs(63, 53).addBox(3.3F, 0.5F, -0.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF38 = new ModelPart(this);
        hairF38.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF38);
        hairF38.texOffs(67, 53).addBox(3.3F, -0.5F, -1.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF39 = new ModelPart(this);
        hairF39.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF39);
        hairF39.texOffs(71, 53).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF40 = new ModelPart(this);
        hairF40.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF40);
        hairF40.texOffs(52, 58).addBox(3.3F, -2.5F, -2.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF41 = new ModelPart(this);
        hairF41.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF41);
        hairF41.texOffs(56, 57).addBox(3.3F, -0.5F, 0.8F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF42 = new ModelPart(this);
        hairF42.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF42);
        hairF42.texOffs(60, 57).addBox(3.3F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairF43 = new ModelPart(this);
        hairF43.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF43);
        hairF43.texOffs(64, 57).addBox(3.3F, 2.5F, 1.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF44 = new ModelPart(this);
        hairF44.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF44);
        hairF44.texOffs(68, 57).addBox(3.3F, -2.6F, 2.4F, 1.0F, 7.0F, 1.0F, 0.0F, true);

        hairF45 = new ModelPart(this);
        hairF45.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF45);
        hairF45.texOffs(72, 57).addBox(3.3F, -2.5F, 1.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairF46 = new ModelPart(this);
        hairF46.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF46);
        hairF46.texOffs(0, 67).addBox(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        hairF47 = new ModelPart(this);
        hairF47.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairF47);
        hairF47.texOffs(14, 70).addBox(-4.4F, -3.9F, 3.2F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        hairBand1 = new ModelPart(this);
        hairBand1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand1);
        hairBand1.texOffs(20, 69).addBox(-3.5F, -3.6F, -4.1F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        hairBand2 = new ModelPart(this);
        hairBand2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand2);
        hairBand2.texOffs(18, 71).addBox(-4.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        hairBand = new ModelPart(this);
        hairBand.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBand);
        hairBand.texOffs(29, 64).addBox(3.3F, -3.6F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);

        body = new ModelPart(this);
        body.setPos(0.0F, 4.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        chest = new ModelPart(this);
        chest.setPos(0.0F, 4.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, 1.0782F, 0.0F, 0.0F);
        chest.texOffs(0, 30).addBox(-3.5F, -1.0001F, -5.0F, 7.0F, 3.0F, 3.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
