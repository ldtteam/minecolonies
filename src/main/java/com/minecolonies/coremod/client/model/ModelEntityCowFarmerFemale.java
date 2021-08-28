// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityCowFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{


    public ModelEntityCowFarmerFemale()
    {
        ModelPart headDetail;
        ModelPart hair;
        ModelPart left_top_1;
        ModelPart backhair;
        ModelPart hairbackTop_2;
        ModelPart hairbackTop_3;
        ModelPart hairBackTop_4;
        ModelPart hairTop_1;
        ModelPart hairTop_2;
        ModelPart hairLeftTop_1;
        ModelPart hairLeftTop_2;
        ModelPart hairLeftTop_3;
        ModelPart hairLeftTop_4;
        ModelPart hairLeftTop_5;
        ModelPart hairbackbuttom1;
        ModelPart ponytail_1;
        ModelPart ponytail_2;
        ModelPart ponytail_3;
        ModelPart hairRightTop_1;
        ModelPart hairfrontTop_1;
        ModelPart hairfrontTop_2;
        ModelPart hairfrontTop_3;
        ModelPart backpack;
        ModelPart bagR;
        ModelPart bagL;
        ModelPart bagBack;
        ModelPart bagFront;
        ModelPart bagWheat;
        ModelPart bagBot;
        ModelPart bipedChest;

        texWidth = 128;
        texHeight = 64;

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelPart(this);
        hair.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hair);


        left_top_1 = new ModelPart(this);
        left_top_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(left_top_1);
        left_top_1.texOffs(0, 32).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 2.0F, 9.0F, 0.0F, true);

        backhair = new ModelPart(this);
        backhair.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(backhair);
        backhair.texOffs(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F, 0.0F, true);

        hairbackTop_2 = new ModelPart(this);
        hairbackTop_2.setPos(0.0F, 0.0F, -3.0F);
        hair.addChild(hairbackTop_2);
        hairbackTop_2.texOffs(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairbackTop_3 = new ModelPart(this);
        hairbackTop_3.setPos(0.0F, 0.0F, -4.0F);
        hair.addChild(hairbackTop_3);
        hairbackTop_3.texOffs(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairBackTop_4 = new ModelPart(this);
        hairBackTop_4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBackTop_4);
        hairBackTop_4.texOffs(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);

        hairTop_1 = new ModelPart(this);
        hairTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_1);
        hairTop_1.texOffs(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F, 0.0F, true);

        hairTop_2 = new ModelPart(this);
        hairTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_2);
        hairTop_2.texOffs(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

        hairLeftTop_1 = new ModelPart(this);
        hairLeftTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_1);
        hairLeftTop_1.texOffs(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_2 = new ModelPart(this);
        hairLeftTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_2);
        hairLeftTop_2.texOffs(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_3 = new ModelPart(this);
        hairLeftTop_3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_3);
        hairLeftTop_3.texOffs(17, 35).addBox(3.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairLeftTop_4 = new ModelPart(this);
        hairLeftTop_4.setPos(0.0F, -1.0F, 0.0F);
        hair.addChild(hairLeftTop_4);
        hairLeftTop_4.texOffs(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F, 0.0F, true);

        hairLeftTop_5 = new ModelPart(this);
        hairLeftTop_5.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_5);
        hairLeftTop_5.texOffs(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        hairbackbuttom1 = new ModelPart(this);
        hairbackbuttom1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairbackbuttom1);
        hairbackbuttom1.texOffs(58, 51).addBox(-3.5F, -0.5F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);

        ponytail_1 = new ModelPart(this);
        ponytail_1.setPos(7.0F, 4.2F, 2.0F);
        hair.addChild(ponytail_1);
        setRotationAngle(ponytail_1, -1.4486F, 0.0F, 0.0F);


        ponytail_2 = new ModelPart(this);
        ponytail_2.setPos(6.0F, -1.0F, 0.0F);
        hair.addChild(ponytail_2);
        setRotationAngle(ponytail_2, -1.0647F, 0.0F, 0.0F);


        ponytail_3 = new ModelPart(this);
        ponytail_3.setPos(6.5F, 0.9F, 0.7F);
        hair.addChild(ponytail_3);
        setRotationAngle(ponytail_3, -1.3613F, 0.0F, 0.0F);


        hairRightTop_1 = new ModelPart(this);
        hairRightTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairRightTop_1);
        hairRightTop_1.texOffs(1, 54).addBox(-4.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairfrontTop_1 = new ModelPart(this);
        hairfrontTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_1);
        hairfrontTop_1.texOffs(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairfrontTop_2 = new ModelPart(this);
        hairfrontTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_2);
        hairfrontTop_2.texOffs(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairfrontTop_3 = new ModelPart(this);
        hairfrontTop_3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_3);
        hairfrontTop_3.texOffs(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        backpack = new ModelPart(this);
        backpack.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(backpack);


        bagR = new ModelPart(this);
        bagR.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagR);
        bagR.texOffs(94, 4).addBox(3.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagL = new ModelPart(this);
        bagL.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagL);
        bagL.texOffs(94, 16).addBox(-4.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagBack = new ModelPart(this);
        bagBack.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagBack);
        bagBack.texOffs(102, 13).addBox(-3.0F, 0.0F, 2.0F, 6.0F, 9.0F, 1.0F, 0.0F, true);

        bagFront = new ModelPart(this);
        bagFront.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagFront);
        bagFront.texOffs(102, 4).addBox(-3.0F, 1.0F, 6.0F, 6.0F, 8.0F, 1.0F, 0.0F, true);

        bagWheat = new ModelPart(this);
        bagWheat.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagWheat);
        bagWheat.texOffs(94, 0).addBox(-3.0F, 1.5F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bagBot = new ModelPart(this);
        bagBot.setPos(0.0F, 0.0F, 0.0F);
        backpack.addChild(bagBot);
        bagBot.texOffs(102, 23).addBox(-3.0F, 9.0F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bipedChest = new ModelPart(this);
        bipedChest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bipedChest);
        setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
        bipedChest.texOffs(40, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        hat.visible = false;
    }
    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}