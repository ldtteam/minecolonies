// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntitySheepFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntitySheepFarmerFemale()
    {
        ModelRenderer headDetail;
        ModelRenderer hair;
        ModelRenderer left_top_1;
        ModelRenderer backhair;
        ModelRenderer hairbackTop_2;
        ModelRenderer hairbackTop_3;
        ModelRenderer hairBackTop_4;
        ModelRenderer hairTop_1;
        ModelRenderer hairTop_2;
        ModelRenderer hairLeftTop_1;
        ModelRenderer hairLeftTop_2;
        ModelRenderer hairLeftTop_3;
        ModelRenderer hairLeftTop_4;
        ModelRenderer hairLeftTop_5;
        ModelRenderer hairbackbuttom1;
        ModelRenderer ponytail_1;
        ModelRenderer ponytail_2;
        ModelRenderer ponytail_3;
        ModelRenderer hairRightTop_1;
        ModelRenderer hairfrontTop_1;
        ModelRenderer hairfrontTop_2;
        ModelRenderer hairfrontTop_3;
        ModelRenderer bagR;
        ModelRenderer bagL;
        ModelRenderer bagBack;
        ModelRenderer bagFront;
        ModelRenderer bagWheat;
        ModelRenderer bagBot;
        ModelRenderer bipedChest;

        texWidth = 128;
        texHeight = 64;

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hair);


        left_top_1 = new ModelRenderer(this);
        left_top_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(left_top_1);
        left_top_1.texOffs(0, 32).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 2.0F, 9.0F, 0.0F, true);

        backhair = new ModelRenderer(this);
        backhair.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(backhair);
        backhair.texOffs(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F, 0.0F, true);

        hairbackTop_2 = new ModelRenderer(this);
        hairbackTop_2.setPos(0.0F, 0.0F, -3.0F);
        hair.addChild(hairbackTop_2);
        hairbackTop_2.texOffs(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairbackTop_3 = new ModelRenderer(this);
        hairbackTop_3.setPos(0.0F, 0.0F, -4.0F);
        hair.addChild(hairbackTop_3);
        hairbackTop_3.texOffs(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairBackTop_4 = new ModelRenderer(this);
        hairBackTop_4.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBackTop_4);
        hairBackTop_4.texOffs(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);

        hairTop_1 = new ModelRenderer(this);
        hairTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_1);
        hairTop_1.texOffs(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F, 0.0F, true);

        hairTop_2 = new ModelRenderer(this);
        hairTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_2);
        hairTop_2.texOffs(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

        hairLeftTop_1 = new ModelRenderer(this);
        hairLeftTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_1);
        hairLeftTop_1.texOffs(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_2 = new ModelRenderer(this);
        hairLeftTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_2);
        hairLeftTop_2.texOffs(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_3 = new ModelRenderer(this);
        hairLeftTop_3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_3);
        hairLeftTop_3.texOffs(17, 35).addBox(3.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairLeftTop_4 = new ModelRenderer(this);
        hairLeftTop_4.setPos(0.0F, -1.0F, 0.0F);
        hair.addChild(hairLeftTop_4);
        hairLeftTop_4.texOffs(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F, 0.0F, true);

        hairLeftTop_5 = new ModelRenderer(this);
        hairLeftTop_5.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_5);
        hairLeftTop_5.texOffs(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        hairbackbuttom1 = new ModelRenderer(this);
        hairbackbuttom1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairbackbuttom1);
        hairbackbuttom1.texOffs(58, 51).addBox(-3.5F, -0.5F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);

        ponytail_1 = new ModelRenderer(this);
        ponytail_1.setPos(7.0F, 5.5F, 2.0F);
        hair.addChild(ponytail_1);
        setRotationAngle(ponytail_1, -1.1868F, 0.0F, 0.0F);
        ponytail_1.texOffs(66, 57).addBox(-7.5F, -7.5F, -4.5F, 1.0F, 1.0F, 4.0F, 0.0F, true);

        ponytail_2 = new ModelRenderer(this);
        ponytail_2.setPos(6.0F, -1.0F, 0.0F);
        hair.addChild(ponytail_2);
        setRotationAngle(ponytail_2, -1.0647F, 0.0F, 0.0F);
        ponytail_2.texOffs(26, 53).addBox(-7.5F, -7.5F, -4.5F, 3.0F, 2.0F, 4.0F, 0.0F, true);

        ponytail_3 = new ModelRenderer(this);
        ponytail_3.setPos(6.5F, 2.0F, 1.0F);
        hair.addChild(ponytail_3);
        setRotationAngle(ponytail_3, -1.1868F, 0.0F, 0.0F);
        ponytail_3.texOffs(47, 56).addBox(-7.5F, -7.5F, -4.5F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        hairRightTop_1 = new ModelRenderer(this);
        hairRightTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairRightTop_1);
        hairRightTop_1.texOffs(1, 54).addBox(-4.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairfrontTop_1 = new ModelRenderer(this);
        hairfrontTop_1.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_1);
        hairfrontTop_1.texOffs(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairfrontTop_2 = new ModelRenderer(this);
        hairfrontTop_2.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_2);
        hairfrontTop_2.texOffs(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairfrontTop_3 = new ModelRenderer(this);
        hairfrontTop_3.setPos(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_3);
        hairfrontTop_3.texOffs(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        bagR = new ModelRenderer(this);
        bagR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagR);
        bagR.texOffs(56, 17).addBox(3.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagL = new ModelRenderer(this);
        bagL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagL);
        bagL.texOffs(64, 17).addBox(-4.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagBack = new ModelRenderer(this);
        bagBack.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagBack);
        bagBack.texOffs(72, 17).addBox(-3.0F, 0.0F, 2.0F, 6.0F, 9.0F, 1.0F, 0.0F, true);

        bagFront = new ModelRenderer(this);
        bagFront.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagFront);
        bagFront.texOffs(86, 17).addBox(-3.0F, 1.0F, 6.0F, 6.0F, 8.0F, 1.0F, 0.0F, true);

        bagWheat = new ModelRenderer(this);
        bagWheat.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagWheat);
        bagWheat.texOffs(58, 40).addBox(-3.0F, 1.5F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bagBot = new ModelRenderer(this);
        bagBot.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bagBot);
        bagBot.texOffs(70, 28).addBox(-3.0F, 9.0F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bipedChest = new ModelRenderer(this);
        bipedChest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bipedChest);
        setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
        bipedChest.texOffs(40, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
