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

        texWidth = 128;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        button = new ModelRenderer(this);
        button.setPos(2.0F, 10.3F, -4.2F);
        body.addChild(button);
        button.texOffs(119, 17).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        leftarmcoat = new ModelRenderer(this);
        leftarmcoat.setPos(2.0F, 1.0F, 0.0F);
        body.addChild(leftarmcoat);
        leftarmcoat.texOffs(82, 16).addBox(-1.5F, -2.0F, -2.5F, 8.0F, 7.0F, 5.0F, 0.0F, true);

        rightarmcoat = new ModelRenderer(this);
        rightarmcoat.setPos(-5.0F, -4.0F, 0.0F);
        body.addChild(rightarmcoat);
        rightarmcoat.texOffs(56, 16).addBox(-3.5F, 3.0F, -2.5F, 8.0F, 7.0F, 5.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setPos(5.0F, 7.0F, -1.5F);
        body.addChild(chest);
        chest.texOffs(26, 32).addBox(-3.5F, 3.0F, -2.5F, 2.0F, 3.0F, 2.0F, 0.0F, true);

        belt = new ModelRenderer(this);
        belt.setPos(-1.0F, 8.0F, -0.5F);
        body.addChild(belt);
        belt.texOffs(0, 32).addBox(-3.5F, 3.0F, -2.5F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        leftarmcoat4 = new ModelRenderer(this);
        leftarmcoat4.setPos(2.5F, 4.2F, -2.5F);
        body.addChild(leftarmcoat4);
        setRotationAngle(leftarmcoat4, -0.7854F, 0.0F, 0.0F);
        leftarmcoat4.texOffs(112, 43).addBox(-2.0F, -4.8385F, -2.7071F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightarmcoat2 = new ModelRenderer(this);
        rightarmcoat2.setPos(-2.5F, 4.2F, -2.5F);
        body.addChild(rightarmcoat2);
        setRotationAngle(rightarmcoat2, -0.7854F, 0.0F, 0.0F);
        rightarmcoat2.texOffs(112, 53).addBox(-2.0F, -4.8385F, -2.7071F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        chest2 = new ModelRenderer(this);
        chest2.setPos(0.5F, 4.2F, -0.5F);
        body.addChild(chest2);
        setRotationAngle(chest2, -0.7854F, 0.0F, 0.0F);
        chest2.texOffs(40, 32).addBox(-4.0F, -0.8636F, -3.6263F, 7.0F, 3.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftarmGlove = new ModelRenderer(this);
        leftarmGlove.setPos(7.0F, 6.0F, 0.0F);
        leftArm.addChild(leftarmGlove);
        leftarmGlove.texOffs(69, 30).addBox(-8.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightlegshoe = new ModelRenderer(this);
        rightlegshoe.setPos(-1.0F, 17.0F, 0.0F);
        rightLeg.addChild(rightlegshoe);
        rightlegshoe.texOffs(0, 40).addBox(-1.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftlegshoe = new ModelRenderer(this);
        leftlegshoe.setPos(3.0F, 17.0F, 0.0F);
        leftLeg.addChild(leftlegshoe);
        leftlegshoe.texOffs(20, 40).addBox(-5.5F, -9.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        MaskL2 = new ModelRenderer(this);
        MaskL2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskL2);
        MaskL2.texOffs(75, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);

        MaskTop = new ModelRenderer(this);
        MaskTop.setPos(0.0F, 0.2F, 0.0F);
        head.addChild(MaskTop);
        MaskTop.texOffs(71, 8).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        MaskL3 = new ModelRenderer(this);
        MaskL3.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskL3);
        MaskL3.texOffs(98, 6).addBox(3.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskL1 = new ModelRenderer(this);
        MaskL1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskL1);
        MaskL1.texOffs(92, 3).addBox(3.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR3 = new ModelRenderer(this);
        MaskR3.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskR3);
        MaskR3.texOffs(64, 10).addBox(-4.5F, -7.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR1 = new ModelRenderer(this);
        MaskR1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskR1);
        MaskR1.texOffs(26, 0).addBox(-4.5F, -2.5F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        MaskR2 = new ModelRenderer(this);
        MaskR2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskR2);
        MaskR2.texOffs(56, 0).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);

        MaskBottom = new ModelRenderer(this);
        MaskBottom.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MaskBottom);
        MaskBottom.texOffs(75, 12).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        maskFace = new ModelRenderer(this);
        maskFace.setPos(0.0F, 4.0F, 0.0F);
        head.addChild(maskFace);
        maskFace.texOffs(99, 12).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 3.0F, 1.0F, 0.0F, true);

        eyeL = new ModelRenderer(this);
        eyeL.setPos(0.2F, 1.0F, 0.0F);
        head.addChild(eyeL);
        eyeL.texOffs(33, 1).addBox(-3.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        eyeR = new ModelRenderer(this);
        eyeR.setPos(-0.2F, 1.0F, 0.0F);
        head.addChild(eyeR);
        eyeR.texOffs(33, 1).addBox(1.2F, -6.5F, -5.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace1 = new ModelRenderer(this);
        maskFace1.setPos(7.0F, 2.0F, 0.0F);
        head.addChild(maskFace1);
        maskFace1.texOffs(108, 6).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace2 = new ModelRenderer(this);
        maskFace2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(maskFace2);
        maskFace2.texOffs(82, 0).addBox(-4.0F, -7.5F, -5.0F, 8.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace3 = new ModelRenderer(this);
        maskFace3.setPos(0.0F, 2.0F, 0.0F);
        head.addChild(maskFace3);
        maskFace3.texOffs(104, 4).addBox(-4.0F, -7.5F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        maskFace4 = new ModelRenderer(this);
        maskFace4.setPos(3.0F, 2.0F, 0.0F);
        head.addChild(maskFace4);
        maskFace4.texOffs(100, 0).addBox(-4.0F, -7.5F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        BrimB = new ModelRenderer(this);
        BrimB.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimB);
        setRotationAngle(BrimB, -0.0698F, 0.0F, 0.0F);
        BrimB.texOffs(74, 43).addBox(-4.5F, -9.0F, 4.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        BrimL = new ModelRenderer(this);
        BrimL.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimL);
        setRotationAngle(BrimL, -0.0698F, 0.0F, 0.0F);
        BrimL.texOffs(72, 45).addBox(5.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        MidMid = new ModelRenderer(this);
        MidMid.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MidMid);
        setRotationAngle(MidMid, -0.0698F, 0.0F, 0.0F);
        MidMid.texOffs(0, 49).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 1.0F, 7.0F, 0.0F, true);

        BrimR = new ModelRenderer(this);
        BrimR.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimR);
        setRotationAngle(BrimR, -0.0698F, 0.0F, 0.0F);
        BrimR.texOffs(54, 45).addBox(-6.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        BrimF = new ModelRenderer(this);
        BrimF.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimF);
        setRotationAngle(BrimF, -0.0698F, 0.0F, 0.0F);
        BrimF.texOffs(94, 43).addBox(-4.5F, -9.0F, -5.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        Brim = new ModelRenderer(this);
        Brim.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(Brim);
        setRotationAngle(Brim, -0.0698F, 0.0F, 0.0F);
        Brim.texOffs(43, 54).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 1.0F, 9.0F, 0.0F, true);

        MidT = new ModelRenderer(this);
        MidT.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MidT);
        setRotationAngle(MidT, -0.0698F, 0.0F, 0.0F);
        MidT.texOffs(82, 46).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        MidB = new ModelRenderer(this);
        MidB.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(MidB);
        setRotationAngle(MidB, -0.0698F, 0.0F, 0.0F);
        MidB.texOffs(81, 55).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.0F, true);

        BeakBottom = new ModelRenderer(this);
        BeakBottom.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BeakBottom);
        setRotationAngle(BeakBottom, 0.2782F, 0.0F, 0.0F);
        BeakBottom.texOffs(107, 0).addBox(-1.0F, -3.4F, -7.2F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        BeakEnd2 = new ModelRenderer(this);
        BeakEnd2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BeakEnd2);
        setRotationAngle(BeakEnd2, 0.5749F, 0.0F, 0.0F);
        BeakEnd2.texOffs(109, 6).addBox(-0.5F, -6.2F, -8.5F, 1.0F, 2.0F, 3.0F, 0.0F, true);

        BeakTop = new ModelRenderer(this);
        BeakTop.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BeakTop);
        setRotationAngle(BeakTop, 0.4004F, 0.0F, 0.0F);
        BeakTop.texOffs(114, 0).addBox(-1.0F, -5.6F, -7.5F, 2.0F, 2.0F, 5.0F, 0.0F, true);

        BeakEnd1 = new ModelRenderer(this);
        BeakEnd1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BeakEnd1);
        setRotationAngle(BeakEnd1, 0.4528F, 0.0F, 0.0F);
        BeakEnd1.texOffs(116, 7).addBox(-1.0F, -5.7F, -8.0F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        BeakEnd3 = new ModelRenderer(this);
        BeakEnd3.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BeakEnd3);
        setRotationAngle(BeakEnd3, 0.7669F, 0.0F, 0.0F);
        BeakEnd3.texOffs(103, 0).addBox(-0.5F, -7.2F, -9.0F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightarmGlove = new ModelRenderer(this);
        rightarmGlove.setPos(-5.0F, 6.0F, 0.0F);
        rightArm.addChild(rightarmGlove);
        rightarmGlove.texOffs(90, 30).addBox(1.5F, 1.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
