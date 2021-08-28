// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityLumberjackFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityLumberjackFemale()
    {
        ModelPart headDetail;
        ModelPart ponytailBase;
        ModelPart ponytailEnd;
        ModelPart chest;
        ModelPart logBottom;
        ModelPart logMiddle;
        ModelPart logTop;
        ModelPart BasketBL;
        ModelPart BasketTB;
        ModelPart BasketBR;
        ModelPart BasketTML;
        ModelPart BasketBF;
        ModelPart BasketMFR;
        ModelPart BasketMFL;
        ModelPart BasketMBL;
        ModelPart BasketMBR;
        ModelPart BasketTMR;
        ModelPart BasketBB;

        texWidth = 128;
        texHeight = 64;

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        ponytailBase = new ModelPart(this);
        ponytailBase.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailBase);
        setRotationAngle(ponytailBase, 0.5585F, 0.0F, 0.0F);
        ponytailBase.texOffs(58, 25).addBox(-0.5F, 3.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, false);

        ponytailEnd = new ModelPart(this);
        ponytailEnd.setPos(0.0F, 0.0F, 0.0F);
        ponytailBase.addChild(ponytailEnd);
        setRotationAngle(ponytailEnd, -0.2296F, 0.0F, 0.0F);
        ponytailEnd.texOffs(57, 19).addBox(-1.0F, -2.0F, 4.2F, 2.0F, 5.0F, 1.0F, 0.0F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        chest = new ModelPart(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
        chest.texOffs(17, 33).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 4.0F, 4.0F, 0.0F, true);

        logBottom = new ModelPart(this);
        logBottom.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(logBottom);
        setRotationAngle(logBottom, 0.0698F, 0.0F, 0.0F);
        logBottom.texOffs(17, 58).addBox(-5.3F, 8.5F, 2.5F, 10.0F, 3.0F, 3.0F, 0.0F, true);

        logMiddle = new ModelPart(this);
        logMiddle.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(logMiddle);
        setRotationAngle(logMiddle, 0.6458F, 0.2967F, 0.0F);
        logMiddle.texOffs(17, 51).addBox(-1.3F, 6.7F, -1.0F, 5.0F, 3.0F, 3.0F, 0.0F, true);

        logTop = new ModelPart(this);
        logTop.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(logTop);
        setRotationAngle(logTop, 0.0F, 0.7854F, 0.2094F);
        logTop.texOffs(17, 41).addBox(-4.2F, 2.0F, 0.7F, 3.0F, 7.0F, 3.0F, 0.0F, true);

        BasketBL = new ModelPart(this);
        BasketBL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketBL);
        BasketBL.texOffs(0, 33).addBox(2.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        BasketTB = new ModelPart(this);
        BasketTB.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketTB);
        BasketTB.texOffs(0, 38).addBox(-2.0F, 4.0F, 6.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        BasketBR = new ModelPart(this);
        BasketBR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketBR);
        BasketBR.texOffs(0, 33).addBox(-3.0F, 11.0F, 3.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        BasketTML = new ModelPart(this);
        BasketTML.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketTML);
        setRotationAngle(BasketTML, 0.8081F, -0.1745F, 0.0F);
        BasketTML.texOffs(11, 33).addBox(3.1F, 1.4F, 0.6F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        BasketBF = new ModelPart(this);
        BasketBF.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketBF);
        BasketBF.texOffs(0, 38).addBox(-2.0F, 11.0F, 2.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        BasketMFR = new ModelPart(this);
        BasketMFR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketMFR);
        BasketMFR.texOffs(11, 41).addBox(-3.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        BasketMFL = new ModelPart(this);
        BasketMFL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketMFL);
        BasketMFL.texOffs(11, 41).addBox(2.0F, 0.0F, 2.0F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        BasketMBL = new ModelPart(this);
        BasketMBL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketMBL);
        BasketMBL.texOffs(6, 41).addBox(2.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        BasketMBR = new ModelPart(this);
        BasketMBR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketMBR);
        BasketMBR.texOffs(6, 41).addBox(-3.0F, 4.0F, 6.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        BasketTMR = new ModelPart(this);
        BasketTMR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketTMR);
        setRotationAngle(BasketTMR, 0.8081F, 0.1745F, 0.0F);
        BasketTMR.texOffs(11, 33).addBox(-4.1F, 1.4F, 0.5F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        BasketBB = new ModelPart(this);
        BasketBB.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(BasketBB);
        BasketBB.texOffs(0, 38).addBox(-2.0F, 11.0F, 6.0F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
