// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityLumberjackFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityLumberjackFemale()
    {
        ModelRenderer headDetail;
        ModelRenderer ponytailBase;
        ModelRenderer ponytailEnd;
        ModelRenderer chest;
        ModelRenderer logBottom;
        ModelRenderer logMiddle;
        ModelRenderer logTop;
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

        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        ponytailBase = new ModelRenderer(this);
        ponytailBase.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(ponytailBase);
        setRotationAngle(ponytailBase, 0.5585F, 0.0F, 0.0F);
        ponytailBase.setTextureOffset(58, 25).addBox(-0.5F, 3.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, false);

        ponytailEnd = new ModelRenderer(this);
        ponytailEnd.setRotationPoint(0.0F, 0.0F, 0.0F);
        ponytailBase.addChild(ponytailEnd);
        setRotationAngle(ponytailEnd, -0.2296F, 0.0F, 0.0F);
        ponytailEnd.setTextureOffset(57, 19).addBox(-1.0F, -2.0F, 4.2F, 2.0F, 5.0F, 1.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
        chest.setTextureOffset(17, 33).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 4.0F, 4.0F, 0.0F, true);

        logBottom = new ModelRenderer(this);
        logBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(logBottom);
        setRotationAngle(logBottom, 0.0698F, 0.0F, 0.0F);
        logBottom.setTextureOffset(17, 58).addBox(-5.3F, 8.5F, 2.5F, 10.0F, 3.0F, 3.0F, 0.0F, true);

        logMiddle = new ModelRenderer(this);
        logMiddle.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(logMiddle);
        setRotationAngle(logMiddle, 0.6458F, 0.2967F, 0.0F);
        logMiddle.setTextureOffset(17, 51).addBox(-1.3F, 6.7F, -1.0F, 5.0F, 3.0F, 3.0F, 0.0F, true);

        logTop = new ModelRenderer(this);
        logTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(logTop);
        setRotationAngle(logTop, 0.0F, 0.7854F, 0.2094F);
        logTop.setTextureOffset(17, 41).addBox(-4.2F, 2.0F, 0.7F, 3.0F, 7.0F, 3.0F, 0.0F, true);

        BasketBL = new ModelRenderer(this);
        BasketBL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketBL);
        BasketBL.setTextureOffset(0, 33).addBox(2.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        BasketTB = new ModelRenderer(this);
        BasketTB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketTB);
        BasketTB.setTextureOffset(0, 38).addBox(-2.0F, 4.0F, 6.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        BasketBR = new ModelRenderer(this);
        BasketBR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketBR);
        BasketBR.setTextureOffset(0, 33).addBox(-3.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        BasketTML = new ModelRenderer(this);
        BasketTML.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketTML);
        setRotationAngle(BasketTML, 0.8081F, -0.1745F, 0.0F);
        BasketTML.setTextureOffset(11, 33).addBox(3.1F, 1.4F, 0.6F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        BasketBF = new ModelRenderer(this);
        BasketBF.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketBF);
        BasketBF.setTextureOffset(0, 38).addBox(-2.0F, 11.0F, 2.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        BasketMFR = new ModelRenderer(this);
        BasketMFR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketMFR);
        BasketMFR.setTextureOffset(11, 41).addBox(-3.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        BasketMFL = new ModelRenderer(this);
        BasketMFL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketMFL);
        BasketMFL.setTextureOffset(11, 41).addBox(2.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        BasketMBL = new ModelRenderer(this);
        BasketMBL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketMBL);
        BasketMBL.setTextureOffset(6, 41).addBox(2.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        BasketMBR = new ModelRenderer(this);
        BasketMBR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketMBR);
        BasketMBR.setTextureOffset(6, 41).addBox(-3.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        BasketTMR = new ModelRenderer(this);
        BasketTMR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketTMR);
        setRotationAngle(BasketTMR, 0.8081F, 0.1745F, 0.0F);
        BasketTMR.setTextureOffset(11, 33).addBox(-4.1F, 1.4F, 0.5F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        BasketBB = new ModelRenderer(this);
        BasketBB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(BasketBB);
        BasketBB.setTextureOffset(0, 38).addBox(-2.0F, 11.0F, 6.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
