package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityLumberjackFemale extends CitizenModel
{
    ModelRenderer chest;
    ModelRenderer ponytailBase;
    ModelRenderer ponytailEnd;
    ModelRenderer BasketBL;
    ModelRenderer BasketTB;
    ModelRenderer BasketBR;
    ModelRenderer BasketTML;
    ModelRenderer BasketBF;
    ModelRenderer BasketMFR;
    ModelRenderer BasketMFL;
    ModelRenderer BasketMBL;
    ModelRenderer BasketMBR;
    ModelRenderer BasketTMR;
    ModelRenderer BasketBB;
    ModelRenderer logTop;
    ModelRenderer logMiddle;
    ModelRenderer logBottom;

    public ModelEntityLumberjackFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

        chest = new ModelRenderer(this, 17, 33);
        chest.addCuboid(-3.5F, 1.7F, -1F, 7, 4, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);
        bipedLeftLeg.mirror = false;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        ponytailBase = new ModelRenderer(this, 33, 6);
        ponytailBase.addCuboid(-0.5F, 3.2F, 3.8F, 1, 5, 1);
        ponytailBase.setRotationPoint(0F, 0F, 0F);
        ponytailBase.setTextureSize(128, 64);
        setRotation(ponytailBase, 0.5585054F, 0F, 0F);

        ponytailEnd = new ModelRenderer(this, 32, 0);
        ponytailEnd.addCuboid(-1F, -2F, 4.2F, 2, 5, 1);
        ponytailEnd.setRotationPoint(0F, 0F, 0F);
        ponytailEnd.setTextureSize(128, 64);
        setRotation(ponytailEnd, -0.2296264F, 0F, 0F);

        BasketBL = new ModelRenderer(this, 0, 33);
        BasketBL.addCuboid(2F, 11F, 3F, 1, 1, 3);
        BasketBL.setRotationPoint(0F, 0F, 0F);
        BasketBL.setTextureSize(128, 64);
        BasketBL.mirror = true;
        setRotation(BasketBL, 0F, 0F, 0F);

        BasketTB = new ModelRenderer(this, 0, 38);
        BasketTB.addCuboid(-2F, 4F, 6F, 4, 1, 1);
        BasketTB.setRotationPoint(0F, 0F, 0F);
        BasketTB.setTextureSize(128, 64);
        BasketTB.mirror = true;
        setRotation(BasketTB, 0F, 0F, 0F);

        BasketBR = new ModelRenderer(this, 0, 33);
        BasketBR.addCuboid(-3F, 11F, 3F, 1, 1, 3);
        BasketBR.setRotationPoint(0F, 0F, 0F);
        BasketBR.setTextureSize(128, 64);
        BasketBR.mirror = true;
        setRotation(BasketBR, 0F, 0F, 0F);

        BasketTML = new ModelRenderer(this, 11, 33);
        BasketTML.addCuboid(3.1F, 1.4F, 0.6F, 1, 6, 1);
        BasketTML.setRotationPoint(0F, 0F, 0F);
        BasketTML.setTextureSize(128, 64);
        BasketTML.mirror = true;
        setRotation(BasketTML, 0.8080874F, -0.1745329F, 0F);

        BasketBF = new ModelRenderer(this, 0, 38);
        BasketBF.addCuboid(-2F, 11F, 2F, 4, 1, 1);
        BasketBF.setRotationPoint(0F, 0F, 0F);
        BasketBF.setTextureSize(128, 64);
        BasketBF.mirror = true;
        setRotation(BasketBF, 0F, 0F, 0F);

        BasketMFR = new ModelRenderer(this, 11, 41);
        BasketMFR.addCuboid(-3F, 0F, 2F, 1, 12, 1);
        BasketMFR.setRotationPoint(0F, 0F, 0F);
        BasketMFR.setTextureSize(128, 64);
        BasketMFR.mirror = true;
        setRotation(BasketMFR, 0F, 0F, 0F);

        BasketMFL = new ModelRenderer(this, 11, 41);
        BasketMFL.addCuboid(2F, 0F, 2F, 1, 12, 1);
        BasketMFL.setRotationPoint(0F, 0F, 0F);
        BasketMFL.setTextureSize(128, 64);
        BasketMFL.mirror = true;
        setRotation(BasketMFL, 0F, 0F, 0F);

        BasketMBL = new ModelRenderer(this, 6, 41);
        BasketMBL.addCuboid(2F, 4F, 6F, 1, 8, 1);
        BasketMBL.setRotationPoint(0F, 0F, 0F);
        BasketMBL.setTextureSize(128, 64);
        BasketMBL.mirror = true;
        setRotation(BasketMBL, 0F, 0F, 0F);

        BasketMBR = new ModelRenderer(this, 6, 41);
        BasketMBR.addCuboid(-3F, 4F, 6F, 1, 8, 1);
        BasketMBR.setRotationPoint(0F, 0F, 0F);
        BasketMBR.setTextureSize(128, 64);
        BasketMBR.mirror = true;
        setRotation(BasketMBR, 0F, 0F, 0F);

        BasketTMR = new ModelRenderer(this, 11, 33);
        BasketTMR.addCuboid(-4.1F, 1.4F, 0.5F, 1, 6, 1);
        BasketTMR.setRotationPoint(0F, 0F, 0F);
        BasketTMR.setTextureSize(128, 64);
        BasketTMR.mirror = true;
        setRotation(BasketTMR, 0.8080874F, 0.1745329F, 0F);

        BasketBB = new ModelRenderer(this, 0, 38);
        BasketBB.addCuboid(-2F, 11F, 6F, 4, 1, 1);
        BasketBB.setRotationPoint(0F, 0F, 0F);
        BasketBB.setTextureSize(128, 64);
        BasketBB.mirror = true;
        setRotation(BasketBB, 0F, 0F, 0F);

        logTop = new ModelRenderer(this, 17, 41);
        logTop.addCuboid(-4.2F, 2F, 0.7F, 3, 7, 3);
        logTop.setRotationPoint(0F, 0F, 0F);
        logTop.setTextureSize(128, 64);
        logTop.mirror = true;
        setRotation(logTop, 0F, 0.7853982F, 0.2094395F);

        logMiddle = new ModelRenderer(this, 17, 51);
        logMiddle.addCuboid(-1.3F, 6.7F, -1F, 5, 3, 3);
        logMiddle.setRotationPoint(0F, 0F, 0F);
        logMiddle.setTextureSize(128, 64);
        logMiddle.mirror = true;
        setRotation(logMiddle, 0.6457718F, 0.296706F, 0F);

        logBottom = new ModelRenderer(this, 17, 58);
        logBottom.addCuboid(-5.3F, 8.5F, 2.5F, 10, 3, 3);
        logBottom.setRotationPoint(0F, 0F, 0F);
        logBottom.setTextureSize(128, 64);
        logBottom.mirror = true;
        setRotation(logBottom, 0.0698132F, 0F, 0F);

        bipedHead.addChild(ponytailBase);
        ponytailBase.addChild(ponytailEnd);

        bipedBody.addChild(chest);

        bipedBody.addChild(logBottom);
        bipedBody.addChild(logMiddle);
        bipedBody.addChild(logTop);

        //this could be better, but the basket is a mess, so I'm doing this for now
        bipedBody.addChild(BasketBL);
        bipedBody.addChild(BasketTB);
        bipedBody.addChild(BasketBR);
        bipedBody.addChild(BasketTML);
        bipedBody.addChild(BasketBF);
        bipedBody.addChild(BasketMFR);
        bipedBody.addChild(BasketMFL);
        bipedBody.addChild(BasketMBL);
        bipedBody.addChild(BasketMBR);
        bipedBody.addChild(BasketTMR);
        bipedBody.addChild(BasketBB);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
