// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityHealerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityHealerFemale()
    {
        ModelRenderer button;
        ModelRenderer leftarmcoat;
        ModelRenderer rightarmcoat;
        ModelRenderer chest;
        ModelRenderer belt;
        ModelRenderer leftarmcoat4;
        ModelRenderer rightarmcoat2;
        ModelRenderer chest2;
        ModelRenderer leftarmGlove;
        ModelRenderer rightlegshoe;
        ModelRenderer leftlegshoe;
        ModelRenderer MaskL2;
        ModelRenderer MaskTop;
        ModelRenderer MaskL3;
        ModelRenderer MaskL1;
        ModelRenderer MaskR3;
        ModelRenderer MaskR1;
        ModelRenderer MaskR2;
        ModelRenderer MaskBottom;
        ModelRenderer maskFace;
        ModelRenderer eyeL;
        ModelRenderer eyeR;
        ModelRenderer maskFace1;
        ModelRenderer maskFace2;
        ModelRenderer maskFace3;
        ModelRenderer maskFace4;
        ModelRenderer BrimB;
        ModelRenderer BrimL;
        ModelRenderer MidMid;
        ModelRenderer BrimR;
        ModelRenderer BrimF;
        ModelRenderer Brim;
        ModelRenderer MidT;
        ModelRenderer MidB;
        ModelRenderer BeakBottom;
        ModelRenderer BeakEnd2;
        ModelRenderer BeakTop;
        ModelRenderer BeakEnd1;
        ModelRenderer BeakEnd3;
        ModelRenderer headDetail;
        ModelRenderer rightarmGlove;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        button = new ModelRenderer(this);
        button.setRotationPoint(2.0F, 10.3F, -4.2F);
        bipedBody.addChild(button);
        button.setTextureOffset(119, 17).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        leftarmcoat = new ModelRenderer(this);
        leftarmcoat.setRotationPoint(2.0F, 1.0F, 0.0F);
        bipedBody.addChild(leftarmcoat);
        leftarmcoat.setTextureOffset(82, 16).addBox(-1.5F, -2.0F, -2.5F, 8.0F, 7.0F, 5.0F, 0.0F, true);

        rightarmcoat = new ModelRenderer(this);
        rightarmcoat.setRotationPoint(-5.0F, -4.0F, 0.0F);
        bipedBody.addChild(rightarmcoat);
        rightarmcoat.setTextureOffset(56, 16).addBox(-3.5F, 3.0F, -2.5F, 8.0F, 7.0F, 5.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(5.0F, 7.0F, -1.5F);
        bipedBody.addChild(chest);
        chest.setTextureOffset(26, 32).addBox(-3.5F, 3.0F, -2.5F, 2.0F, 3.0F, 2.0F, 0.0F, true);

        belt = new ModelRenderer(this);
        belt.setRotationPoint(-1.0F, 8.0F, -0.5F);
        bipedBody.addChild(belt);
        belt.setTextureOffset(0, 32).addBox(-3.5F, 3.0F, -2.5F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        leftarmcoat4 = new ModelRenderer(this);
        leftarmcoat4.setRotationPoint(2.5F, 4.2F, -2.5F);
        bipedBody.addChild(leftarmcoat4);
        setRotationAngle(leftarmcoat4, -0.7854F, 0.0F, 0.0F);
        leftarmcoat4.setTextureOffset(112, 43).addBox(-2.0F, -4.8385F, -2.7071F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightarmcoat2 = new ModelRenderer(this);
        rightarmcoat2.setRotationPoint(-2.5F, 4.2F, -2.5F);
        bipedBody.addChild(rightarmcoat2);
        setRotationAngle(rightarmcoat2, -0.7854F, 0.0F, 0.0F);
        rightarmcoat2.setTextureOffset(112, 53).addBox(-2.0F, -4.8385F, -2.7071F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        chest2 = new ModelRenderer(this);
        chest2.setRotationPoint(0.5F, 4.2F, -0.5F);
        bipedBody.addChild(chest2);
        setRotationAngle(chest2, -0.7854F, 0.0F, 0.0F);
        chest2.setTextureOffset(40, 32).addBox(-4.0F, -0.8636F, -3.6263F, 7.0F, 3.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftarmGlove = new ModelRenderer(this);
        leftarmGlove.setRotationPoint(7.0F, 6.0F, 0.0F);
        bipedLeftArm.addChild(leftarmGlove);
        leftarmGlove.setTextureOffset(69, 30).addBox(-8.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightlegshoe = new ModelRenderer(this);
        rightlegshoe.setRotationPoint(-1.0F, 17.0F, 0.0F);
        bipedRightLeg.addChild(rightlegshoe);
        rightlegshoe.setTextureOffset(0, 40).addBox(-1.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftlegshoe = new ModelRenderer(this);
        leftlegshoe.setRotationPoint(3.0F, 17.0F, 0.0F);
        bipedLeftLeg.addChild(leftlegshoe);
        leftlegshoe.setTextureOffset(20, 40).addBox(-5.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        MaskL2 = new ModelRenderer(this);
        MaskL2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskL2);
        MaskL2.setTextureOffset(75, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);

        MaskTop = new ModelRenderer(this);
        MaskTop.setRotationPoint(0.0F, 0.2F, 0.0F);
        bipedHead.addChild(MaskTop);
        MaskTop.setTextureOffset(71, 8).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        MaskL3 = new ModelRenderer(this);
        MaskL3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskL3);
        MaskL3.setTextureOffset(98, 6).addBox(3.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskL1 = new ModelRenderer(this);
        MaskL1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskL1);
        MaskL1.setTextureOffset(92, 3).addBox(3.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR3 = new ModelRenderer(this);
        MaskR3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskR3);
        MaskR3.setTextureOffset(64, 10).addBox(-4.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR1 = new ModelRenderer(this);
        MaskR1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskR1);
        MaskR1.setTextureOffset(26, 0).addBox(-4.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR2 = new ModelRenderer(this);
        MaskR2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskR2);
        MaskR2.setTextureOffset(56, 0).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);

        MaskBottom = new ModelRenderer(this);
        MaskBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MaskBottom);
        MaskBottom.setTextureOffset(75, 12).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        maskFace = new ModelRenderer(this);
        maskFace.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedHead.addChild(maskFace);
        maskFace.setTextureOffset(99, 12).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 3.0F, 1.0F, 0.0F, true);

        eyeL = new ModelRenderer(this);
        eyeL.setRotationPoint(0.2F, 1.0F, 0.0F);
        bipedHead.addChild(eyeL);
        eyeL.setTextureOffset(33, 1).addBox(-3.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        eyeR = new ModelRenderer(this);
        eyeR.setRotationPoint(-0.2F, 1.0F, 0.0F);
        bipedHead.addChild(eyeR);
        eyeR.setTextureOffset(33, 1).addBox(1.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace1 = new ModelRenderer(this);
        maskFace1.setRotationPoint(7.0F, 2.0F, 0.0F);
        bipedHead.addChild(maskFace1);
        maskFace1.setTextureOffset(108, 6).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace2 = new ModelRenderer(this);
        maskFace2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(maskFace2);
        maskFace2.setTextureOffset(82, 0).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace3 = new ModelRenderer(this);
        maskFace3.setRotationPoint(0.0F, 2.0F, 0.0F);
        bipedHead.addChild(maskFace3);
        maskFace3.setTextureOffset(104, 4).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace4 = new ModelRenderer(this);
        maskFace4.setRotationPoint(3.0F, 2.0F, 0.0F);
        bipedHead.addChild(maskFace4);
        maskFace4.setTextureOffset(100, 0).addBox(-4.0F, -7.5F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        BrimB = new ModelRenderer(this);
        BrimB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimB);
        setRotationAngle(BrimB, -0.0698F, 0.0F, 0.0F);
        BrimB.setTextureOffset(74, 43).addBox(-4.5F, -9.0F, 4.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        BrimL = new ModelRenderer(this);
        BrimL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimL);
        setRotationAngle(BrimL, -0.0698F, 0.0F, 0.0F);
        BrimL.setTextureOffset(72, 45).addBox(5.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        MidMid = new ModelRenderer(this);
        MidMid.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MidMid);
        setRotationAngle(MidMid, -0.0698F, 0.0F, 0.0F);
        MidMid.setTextureOffset(0, 49).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 1.0F, 7.0F, 0.0F, true);

        BrimR = new ModelRenderer(this);
        BrimR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimR);
        setRotationAngle(BrimR, -0.0698F, 0.0F, 0.0F);
        BrimR.setTextureOffset(54, 45).addBox(-6.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        BrimF = new ModelRenderer(this);
        BrimF.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimF);
        setRotationAngle(BrimF, -0.0698F, 0.0F, 0.0F);
        BrimF.setTextureOffset(94, 43).addBox(-4.5F, -9.0F, -5.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        Brim = new ModelRenderer(this);
        Brim.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(Brim);
        setRotationAngle(Brim, -0.0698F, 0.0F, 0.0F);
        Brim.setTextureOffset(43, 54).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 1.0F, 9.0F, 0.0F, true);

        MidT = new ModelRenderer(this);
        MidT.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MidT);
        setRotationAngle(MidT, -0.0698F, 0.0F, 0.0F);
        MidT.setTextureOffset(82, 46).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        MidB = new ModelRenderer(this);
        MidB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(MidB);
        setRotationAngle(MidB, -0.0698F, 0.0F, 0.0F);
        MidB.setTextureOffset(81, 55).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        BeakBottom = new ModelRenderer(this);
        BeakBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BeakBottom);
        setRotationAngle(BeakBottom, 0.2782F, 0.0F, 0.0F);
        BeakBottom.setTextureOffset(107, 0).addBox(-1.0F, -3.4F, -7.2F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        BeakEnd2 = new ModelRenderer(this);
        BeakEnd2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BeakEnd2);
        setRotationAngle(BeakEnd2, 0.5749F, 0.0F, 0.0F);
        BeakEnd2.setTextureOffset(109, 6).addBox(-0.5F, -6.2F, -8.5F, 1.0F, 2.0F, 3.0F, 0.0F, true);

        BeakTop = new ModelRenderer(this);
        BeakTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BeakTop);
        setRotationAngle(BeakTop, 0.4004F, 0.0F, 0.0F);
        BeakTop.setTextureOffset(114, 0).addBox(-1.0F, -5.6F, -7.5F, 2.0F, 2.0F, 5.0F, 0.0F, true);

        BeakEnd1 = new ModelRenderer(this);
        BeakEnd1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BeakEnd1);
        setRotationAngle(BeakEnd1, 0.4528F, 0.0F, 0.0F);
        BeakEnd1.setTextureOffset(116, 7).addBox(-1.0F, -5.7F, -8.0F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        BeakEnd3 = new ModelRenderer(this);
        BeakEnd3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BeakEnd3);
        setRotationAngle(BeakEnd3, 0.7669F, 0.0F, 0.0F);
        BeakEnd3.setTextureOffset(103, 0).addBox(-0.5F, -7.2F, -9.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightarmGlove = new ModelRenderer(this);
        rightarmGlove.setRotationPoint(-5.0F, 6.0F, 0.0F);
        bipedRightArm.addChild(rightarmGlove);
        rightarmGlove.setTextureOffset(90, 30).addBox(1.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
