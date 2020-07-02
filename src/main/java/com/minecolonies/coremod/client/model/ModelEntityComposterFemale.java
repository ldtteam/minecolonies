package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityComposterFemale extends CitizenModel<AbstractEntityCitizen>
{
    //fields
    public ModelEntityComposterFemale()
    {
        final ModelRenderer gloveR;
        final ModelRenderer gloveL;
        final ModelRenderer bootR;
        final ModelRenderer bootL;

        final ModelRenderer chest;

        final ModelRenderer hairLeftTop1;
        final ModelRenderer hairLeftTop2;
        final ModelRenderer hairLeftTop3;
        final ModelRenderer hairLeftTop4;
        final ModelRenderer hairLeftTop5;
        final ModelRenderer hairLeftTop6;
        final ModelRenderer hairLeftTop7;
        final ModelRenderer hairLeftTop8;
        final ModelRenderer hairLeftTop9;
        final ModelRenderer hairLeftTop10;
        final ModelRenderer hairLeftTop11;
        final ModelRenderer hairLeftTop12;
        final ModelRenderer hairLeftTop13;
        final ModelRenderer hairLeftTop14;

        final ModelRenderer hairTop1;
        final ModelRenderer hairTop2;
        final ModelRenderer hairTop3;
        final ModelRenderer hairTop4;
        final ModelRenderer hairTop5;

        textureWidth = 128;
        textureHeight = 64;

        gloveR = new ModelRenderer(this, 20, 32);
        gloveR.addBox(1.5F, 3F, -2.5F, 5, 1, 5);
        gloveR.setRotationPoint(-5F, 2F, 0F);
        gloveR.setTextureSize(128, 64);
        gloveR.mirror = true;
        setRotation(gloveR, 0F, 0F, 0F);

        gloveL = new ModelRenderer(this, 0, 32);
        gloveL.addBox(-6.5F, 3F, -2.5F, 5, 1, 5);
        gloveL.setRotationPoint(5F, 2F, 0F);
        gloveL.setTextureSize(128, 64);
        gloveL.mirror = true;
        setRotation(gloveL, 0F, 0F, 0F);

        bootR = new ModelRenderer(this, 20, 38);
        bootR.addBox(-0.5F, -8F, -2.5F, 5, 2, 5);
        bootR.setRotationPoint(-2F, 12F, 0F);
        bootR.setTextureSize(128, 64);
        bootR.mirror = true;
        setRotation(bootR, 0F, 0F, 0F);

        bootL = new ModelRenderer(this, 0, 38);
        bootL.addBox(-4.5F, -8F, -2.5F, 5, 2, 5);
        bootL.setRotationPoint(2F, 12F, 0F);
        bootL.setTextureSize(128, 64);
        bootL.mirror = true;
        setRotation(bootL, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 40, 32);
        chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

        hairLeftTop1 = new ModelRenderer(this, 0, 45);
        hairLeftTop1.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8);
        hairLeftTop1.setRotationPoint(0F, 0F, 0F);
        hairLeftTop1.setTextureSize(128, 64);
        hairLeftTop1.mirror = true;
        setRotation(hairLeftTop1, 0F, 0F, 0F);

        hairTop1 = new ModelRenderer(this, 0, 45);
        hairTop1.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1);
        hairTop1.setRotationPoint(0F, 0F, 0F);
        hairTop1.setTextureSize(128, 64);
        hairTop1.mirror = true;
        setRotation(hairTop1, 0F, 0F, 0F);

        hairLeftTop2 = new ModelRenderer(this, 0, 45);
        hairLeftTop2.addBox(2.5F, -3.5F, 1.5F, 2, 3, 3);
        hairLeftTop2.setRotationPoint(0F, 0F, 0F);
        hairLeftTop2.setTextureSize(128, 64);
        hairLeftTop2.mirror = true;
        setRotation(hairLeftTop2, 0F, 0F, 0F);

        hairLeftTop3 = new ModelRenderer(this, 0, 45);
        hairLeftTop3.addBox(-3.5F, -0.5F, 3.5F, 7, 3, 1);
        hairLeftTop3.setRotationPoint(0F, 0F, 0F);
        hairLeftTop3.setTextureSize(128, 64);
        hairLeftTop3.mirror = true;
        setRotation(hairLeftTop3, 0F, 0F, 0F);

        hairLeftTop4 = new ModelRenderer(this, 0, 45);
        hairLeftTop4.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9);
        hairLeftTop4.setRotationPoint(0F, 0F, 0F);
        hairLeftTop4.setTextureSize(128, 64);
        hairLeftTop4.mirror = true;
        setRotation(hairLeftTop4, 0F, 0F, 0F);

        hairLeftTop5 = new ModelRenderer(this, 0, 45);
        hairLeftTop5.addBox(-4.5F, -5.5F, -0.5F, 2, 1, 5);
        hairLeftTop5.setRotationPoint(0F, 0F, 0F);
        hairLeftTop5.setTextureSize(128, 64);
        hairLeftTop5.mirror = true;
        setRotation(hairLeftTop5, 0F, 0F, 0F);

        hairLeftTop6 = new ModelRenderer(this, 0, 45);
        hairLeftTop6.addBox(-4.5F, -4.5F, 0.5F, 2, 1, 4);
        hairLeftTop6.setRotationPoint(0F, 0F, 0F);
        hairLeftTop6.setTextureSize(128, 64);
        hairLeftTop6.mirror = true;
        setRotation(hairLeftTop6, 0F, 0F, 0F);

        hairTop2 = new ModelRenderer(this, 0, 45);
        hairTop2.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1);
        hairTop2.setRotationPoint(0F, 0F, 0F);
        hairTop2.setTextureSize(128, 64);
        hairTop2.mirror = true;
        setRotation(hairTop2, 0F, 0F, 0F);

        hairTop3 = new ModelRenderer(this, 0, 45);
        hairTop3.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        hairTop3.setRotationPoint(0F, 0F, 0F);
        hairTop3.setTextureSize(128, 64);
        hairTop3.mirror = true;
        setRotation(hairTop3, 0F, 0F, 0F);

        hairTop4 = new ModelRenderer(this, 0, 45);
        hairTop4.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9);
        hairTop4.setRotationPoint(0F, 0F, 0F);
        hairTop4.setTextureSize(128, 64);
        hairTop4.mirror = true;
        setRotation(hairTop4, 0F, 0F, 0F);

        hairLeftTop7 = new ModelRenderer(this, 0, 45);
        hairLeftTop7.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 1);
        hairLeftTop7.setRotationPoint(0F, 0F, 0F);
        hairLeftTop7.setTextureSize(128, 64);
        hairLeftTop7.mirror = true;
        setRotation(hairLeftTop7, 0F, 0F, 0F);

        hairLeftTop8 = new ModelRenderer(this, 0, 45);
        hairLeftTop8.addBox(-4.5F, -5.5F, -3.5F, 9, 1, 1);
        hairLeftTop8.setRotationPoint(0F, 0F, 0F);
        hairLeftTop8.setTextureSize(128, 64);
        hairLeftTop8.mirror = true;
        setRotation(hairLeftTop8, 0F, 0F, 0F);

        hairLeftTop9 = new ModelRenderer(this, 0, 45);
        hairLeftTop9.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8);
        hairLeftTop9.setRotationPoint(0F, 0F, 0F);
        hairLeftTop9.setTextureSize(128, 64);
        hairLeftTop9.mirror = true;
        setRotation(hairLeftTop9, 0F, 0F, 0F);

        hairLeftTop10 = new ModelRenderer(this, 0, 45);
        hairLeftTop10.addBox(2.5F, -6.5F, -3.5F, 2, 1, 8);
        hairLeftTop10.setRotationPoint(0F, 0F, 0F);
        hairLeftTop10.setTextureSize(128, 64);
        hairLeftTop10.mirror = true;
        setRotation(hairLeftTop10, 0F, 0F, 0F);

        hairLeftTop11 = new ModelRenderer(this, 0, 45);
        hairLeftTop11.addBox(2.5F, -5.5F, -0.5F, 2, 2, 5);
        hairLeftTop11.setRotationPoint(0F, 0F, 0F);
        hairLeftTop11.setTextureSize(128, 64);
        hairLeftTop11.mirror = true;
        setRotation(hairLeftTop11, 0F, 0F, 0F);

        hairLeftTop12 = new ModelRenderer(this, 0, 45);
        hairLeftTop12.addBox(-4.5F, -3.5F, 1.5F, 2, 3, 3);
        hairLeftTop12.setRotationPoint(0F, 0F, 0F);
        hairLeftTop12.setTextureSize(128, 64);
        hairLeftTop12.mirror = true;
        setRotation(hairLeftTop12, 0F, 0F, 0F);

        hairLeftTop13 = new ModelRenderer(this, 0, 45);
        hairLeftTop13.addBox(3.5F, -0.5F, 2.5F, 1, 2, 2);
        hairLeftTop13.setRotationPoint(0F, 0F, 0F);
        hairLeftTop13.setTextureSize(128, 64);
        hairLeftTop13.mirror = true;
        setRotation(hairLeftTop13, 0F, 0F, 0F);

        hairLeftTop14 = new ModelRenderer(this, 0, 45);
        hairLeftTop14.addBox(-4.5F, -0.5F, 2.5F, 1, 2, 2);
        hairLeftTop14.setRotationPoint(0F, 0F, 0F);
        hairLeftTop14.setTextureSize(128, 64);
        hairLeftTop14.mirror = true;
        setRotation(hairLeftTop14, 0F, 0F, 0F);

        hairTop5 = new ModelRenderer(this, 0, 45);
        hairTop5.addBox(3.5F, -6.5F, -4.5F, 1, 3, 1);
        hairTop5.setRotationPoint(0F, 0F, 0F);
        hairTop5.setTextureSize(128, 64);
        hairTop5.mirror = true;
        setRotation(hairTop5, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

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

        bipedBody.addChild(chest);
        bipedRightArm.addChild(gloveR);
        bipedLeftArm.addChild(gloveL);

        bipedRightLeg.addChild(bootR);
        bipedLeftLeg.addChild(bootL);


        bipedHead.addChild(hairTop1);
        bipedHead.addChild(hairTop2);
        bipedHead.addChild(hairTop3);
        bipedHead.addChild(hairTop4);
        bipedHead.addChild(hairTop5);
        bipedHead.addChild(hairLeftTop1);
        bipedHead.addChild(hairLeftTop2);
        bipedHead.addChild(hairLeftTop3);
        bipedHead.addChild(hairLeftTop4);
        bipedHead.addChild(hairLeftTop5);
        bipedHead.addChild(hairLeftTop6);
        bipedHead.addChild(hairLeftTop7);
        bipedHead.addChild(hairLeftTop8);
        bipedHead.addChild(hairLeftTop9);
        bipedHead.addChild(hairLeftTop10);
        bipedHead.addChild(hairLeftTop11);
        bipedHead.addChild(hairLeftTop12);
        bipedHead.addChild(hairLeftTop13);
        bipedHead.addChild(hairLeftTop14);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
