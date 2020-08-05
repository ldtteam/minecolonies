// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityTeacherFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityTeacherFemale()
    {
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
        ModelRenderer headDetail;
        ModelRenderer bipedChest;


        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hair);


        left_top_1 = new ModelRenderer(this);
        left_top_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(left_top_1);
        left_top_1.setTextureOffset(0, 32).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 2.0F, 9.0F, 0.0F, true);

        backhair = new ModelRenderer(this);
        backhair.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(backhair);
        backhair.setTextureOffset(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F, 0.0F, true);

        hairbackTop_2 = new ModelRenderer(this);
        hairbackTop_2.setRotationPoint(0.0F, 0.0F, -3.0F);
        hair.addChild(hairbackTop_2);
        hairbackTop_2.setTextureOffset(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairbackTop_3 = new ModelRenderer(this);
        hairbackTop_3.setRotationPoint(0.0F, 0.0F, -4.0F);
        hair.addChild(hairbackTop_3);
        hairbackTop_3.setTextureOffset(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairBackTop_4 = new ModelRenderer(this);
        hairBackTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairBackTop_4);
        hairBackTop_4.setTextureOffset(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);

        hairTop_1 = new ModelRenderer(this);
        hairTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_1);
        hairTop_1.setTextureOffset(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F, 0.0F, true);

        hairTop_2 = new ModelRenderer(this);
        hairTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairTop_2);
        hairTop_2.setTextureOffset(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

        hairLeftTop_1 = new ModelRenderer(this);
        hairLeftTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_1);
        hairLeftTop_1.setTextureOffset(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_2 = new ModelRenderer(this);
        hairLeftTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_2);
        hairLeftTop_2.setTextureOffset(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

        hairLeftTop_3 = new ModelRenderer(this);
        hairLeftTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_3);
        hairLeftTop_3.setTextureOffset(17, 35).addBox(3.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairLeftTop_4 = new ModelRenderer(this);
        hairLeftTop_4.setRotationPoint(0.0F, -1.0F, 0.0F);
        hair.addChild(hairLeftTop_4);
        hairLeftTop_4.setTextureOffset(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F, 0.0F, true);

        hairLeftTop_5 = new ModelRenderer(this);
        hairLeftTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairLeftTop_5);
        hairLeftTop_5.setTextureOffset(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        hairbackbuttom1 = new ModelRenderer(this);
        hairbackbuttom1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairbackbuttom1);
        hairbackbuttom1.setTextureOffset(58, 51).addBox(-3.5F, -0.5F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);

        ponytail_1 = new ModelRenderer(this);
        ponytail_1.setRotationPoint(7.0F, 4.2F, 2.0F);
        hair.addChild(ponytail_1);
        setRotationAngle(ponytail_1, -1.4486F, 0.0F, 0.0F);
        ponytail_1.setTextureOffset(66, 57).addBox(-7.5F, -4.7814F, -0.1798F, 1.0F, 1.0F, 4.0F, 0.0F, true);

        ponytail_2 = new ModelRenderer(this);
        ponytail_2.setRotationPoint(6.0F, -1.0F, 0.0F);
        hair.addChild(ponytail_2);
        setRotationAngle(ponytail_2, -1.0647F, 0.0F, 0.0F);
        ponytail_2.setTextureOffset(26, 53).addBox(-7.5F, -5.3091F, -0.3407F, 3.0F, 2.0F, 4.0F, 0.0F, true);

        ponytail_3 = new ModelRenderer(this);
        ponytail_3.setRotationPoint(6.5F, 0.9F, 0.7F);
        hair.addChild(ponytail_3);
        setRotationAngle(ponytail_3, -1.3613F, 0.0F, 0.0F);
        ponytail_3.setTextureOffset(47, 56).addBox(-7.5F, -5.737F, -0.1521F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        hairRightTop_1 = new ModelRenderer(this);
        hairRightTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairRightTop_1);
        hairRightTop_1.setTextureOffset(1, 54).addBox(-4.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        hairfrontTop_1 = new ModelRenderer(this);
        hairfrontTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_1);
        hairfrontTop_1.setTextureOffset(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        hairfrontTop_2 = new ModelRenderer(this);
        hairfrontTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_2);
        hairfrontTop_2.setTextureOffset(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairfrontTop_3 = new ModelRenderer(this);
        hairfrontTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hair.addChild(hairfrontTop_3);
        hairfrontTop_3.setTextureOffset(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        bipedChest = new ModelRenderer(this);
        bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bipedChest);
        setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
        bipedChest.setTextureOffset(40, 32).addBox(-3.0F, 2.7F, -0.5F, 6.0F, 3.0F, 4.0F, 0.0F, false);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
