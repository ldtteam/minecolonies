// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityCrafterMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityCrafterMale()
    {
        ModelRenderer RArmRot;
        ModelRenderer FingerL;
        ModelRenderer FingerM;
        ModelRenderer FingerR;
        ModelRenderer RArmB2;
        ModelRenderer RArmB;
        ModelRenderer RArmL;
        ModelRenderer RArmM;
        ModelRenderer CoreF;
        ModelRenderer CoreB;
        ModelRenderer RArmFT;
        ModelRenderer RArmBT;
        ModelRenderer RArmBBB;
        ModelRenderer RArmBBM;
        ModelRenderer Vent12;
        ModelRenderer Vent10;
        ModelRenderer Vent8;
        ModelRenderer Vent6;
        ModelRenderer Vent4;
        ModelRenderer Vent1;
        ModelRenderer Vent2;
        ModelRenderer Vent5;
        ModelRenderer Vent7;
        ModelRenderer Vent9;
        ModelRenderer Vent13;
        ModelRenderer Vent11;
        ModelRenderer Vent3;
        ModelRenderer headdetail;
        ModelRenderer HatT;
        ModelRenderer BrimBa;
        ModelRenderer BrimR;
        ModelRenderer BrimL;
        ModelRenderer BrimF;
        ModelRenderer BrimBo;
        ModelRenderer HatM;
        ModelRenderer SpecLB;
        ModelRenderer SpecL;
        ModelRenderer SpecMid;
        ModelRenderer SpecR;
        ModelRenderer SpecRB;

        texWidth = 256;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        RArmRot = new ModelRenderer(this);
        RArmRot.setPos(-5.2F, 1.65F, 0.0F);
        rightArm.addChild(RArmRot);
        setRotationAngle(RArmRot, 0.0F, 0.0F, 0.7854F);
        RArmRot.texOffs(58, 27).addBox(-0.2F, -6.5F, -1.5F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        FingerL = new ModelRenderer(this);
        FingerL.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(FingerL);
        FingerL.texOffs(60, 48).addBox(4.8F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        FingerM = new ModelRenderer(this);
        FingerM.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(FingerM);
        FingerM.texOffs(56, 48).addBox(3.5F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        FingerR = new ModelRenderer(this);
        FingerR.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(FingerR);
        FingerR.texOffs(52, 48).addBox(2.2F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmB2 = new ModelRenderer(this);
        RArmB2.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmB2);
        RArmB2.texOffs(52, 45).addBox(1.7F, 3.5F, -2.3F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        RArmB = new ModelRenderer(this);
        RArmB.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmB);
        RArmB.texOffs(48, 40).addBox(1.7F, -2.5F, 1.3F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        RArmL = new ModelRenderer(this);
        RArmL.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmL);
        RArmL.texOffs(48, 33).addBox(1.7F, -2.5F, -2.3F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        RArmM = new ModelRenderer(this);
        RArmM.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmM);
        RArmM.texOffs(52, 33).addBox(1.7F, -1.5F, -2.3F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        CoreF = new ModelRenderer(this);
        CoreF.setPos(-4.0F, 2.7F, 0.0F);
        rightArm.addChild(CoreF);
        setRotationAngle(CoreF, 0.0F, 0.0F, 0.7854F);
        CoreF.texOffs(75, 32).addBox(0.8F, -3.7F, 1.7F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        CoreB = new ModelRenderer(this);
        CoreB.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(CoreB);
        setRotationAngle(CoreB, 0.0F, 0.0F, 0.7854F);
        CoreB.texOffs(67, 31).addBox(0.8F, -3.7F, 1.5F, 3.0F, 3.0F, 1.0F, 0.0F, true);

        RArmFT = new ModelRenderer(this);
        RArmFT.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmFT);
        RArmFT.texOffs(58, 31).addBox(2.7F, -4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmBT = new ModelRenderer(this);
        RArmBT.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmBT);
        RArmBT.texOffs(62, 31).addBox(2.7F, -4.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmBBB = new ModelRenderer(this);
        RArmBBB.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmBBB);
        RArmBBB.texOffs(82, 35).addBox(1.7F, 4.5F, 1.3F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        RArmBBM = new ModelRenderer(this);
        RArmBBM.setPos(-4.0F, 2.0F, 0.0F);
        rightArm.addChild(RArmBBM);
        RArmBBM.texOffs(82, 31).addBox(2.7F, 1.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        Vent12 = new ModelRenderer(this);
        Vent12.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent12);
        Vent12.texOffs(40, 66).addBox(1.7F, 3.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent10 = new ModelRenderer(this);
        Vent10.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent10);
        Vent10.texOffs(40, 60).addBox(1.7F, 2.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent8 = new ModelRenderer(this);
        Vent8.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent8);
        Vent8.texOffs(40, 54).addBox(1.7F, 1.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent6 = new ModelRenderer(this);
        Vent6.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent6);
        Vent6.texOffs(40, 48).addBox(1.7F, 0.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent4 = new ModelRenderer(this);
        Vent4.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent4);
        Vent4.texOffs(40, 42).addBox(1.7F, -1.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent1 = new ModelRenderer(this);
        Vent1.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent1);
        Vent1.texOffs(40, 33).addBox(1.7F, -2.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent2 = new ModelRenderer(this);
        Vent2.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent2);
        Vent2.texOffs(40, 36).addBox(1.7F, -2.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent5 = new ModelRenderer(this);
        Vent5.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent5);
        Vent5.texOffs(40, 45).addBox(1.7F, -0.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent7 = new ModelRenderer(this);
        Vent7.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent7);
        Vent7.texOffs(40, 51).addBox(1.7F, 0.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent9 = new ModelRenderer(this);
        Vent9.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent9);
        Vent9.texOffs(40, 57).addBox(1.7F, 1.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent13 = new ModelRenderer(this);
        Vent13.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent13);
        Vent13.texOffs(40, 69).addBox(1.7F, 3.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent11 = new ModelRenderer(this);
        Vent11.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent11);
        Vent11.texOffs(40, 63).addBox(1.7F, 2.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent3 = new ModelRenderer(this);
        Vent3.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(Vent3);
        Vent3.texOffs(40, 39).addBox(1.7F, -1.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headdetail = new ModelRenderer(this);
        headdetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        HatT = new ModelRenderer(this);
        HatT.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HatT);
        setRotationAngle(HatT, -0.0744F, 0.0F, 0.0F);
        HatT.texOffs(0, 76).addBox(-2.0F, -11.5F, -2.7F, 4.0F, 1.0F, 5.0F, 0.0F, true);

        BrimBa = new ModelRenderer(this);
        BrimBa.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimBa);
        setRotationAngle(BrimBa, -0.0744F, 0.0F, 0.0F);
        BrimBa.texOffs(0, 45).addBox(-5.0F, -9.0F, 3.5F, 10.0F, 1.0F, 1.0F, 0.0F, true);

        BrimR = new ModelRenderer(this);
        BrimR.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimR);
        setRotationAngle(BrimR, -0.0744F, 0.0F, 0.0F);
        BrimR.texOffs(0, 57).addBox(-5.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        BrimL = new ModelRenderer(this);
        BrimL.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimL);
        setRotationAngle(BrimL, -0.0744F, 0.0F, 0.0F);
        BrimL.texOffs(0, 47).addBox(4.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        BrimF = new ModelRenderer(this);
        BrimF.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimF);
        setRotationAngle(BrimF, -0.0744F, 0.0F, 0.0F);
        BrimF.texOffs(0, 43).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 1.0F, 1.0F, 0.0F, true);

        BrimBo = new ModelRenderer(this);
        BrimBo.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(BrimBo);
        setRotationAngle(BrimBo, -0.0744F, 0.0F, 0.0F);
        BrimBo.texOffs(0, 33).addBox(-5.0F, -8.5F, -5.0F, 10.0F, 1.0F, 9.0F, 0.0F, true);

        HatM = new ModelRenderer(this);
        HatM.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HatM);
        setRotationAngle(HatM, -0.0744F, 0.0F, 0.0F);
        HatM.texOffs(0, 67).addBox(-3.5F, -10.5F, -3.7F, 7.0F, 2.0F, 7.0F, 0.0F, true);

        SpecLB = new ModelRenderer(this);
        SpecLB.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(SpecLB);
        setRotationAngle(SpecLB, -0.3346F, 0.0F, 0.0F);
        SpecLB.texOffs(30, 47).addBox(0.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        SpecL = new ModelRenderer(this);
        SpecL.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(SpecL);
        setRotationAngle(SpecL, -0.3346F, 0.0F, 0.0F);
        SpecL.texOffs(30, 50).addBox(1.3F, -9.0F, -7.4F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        SpecMid = new ModelRenderer(this);
        SpecMid.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(SpecMid);
        setRotationAngle(SpecMid, -0.3346F, 0.0F, 0.0F);
        SpecMid.texOffs(30, 44).addBox(-1.0F, -9.0F, -6.3F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        SpecR = new ModelRenderer(this);
        SpecR.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(SpecR);
        setRotationAngle(SpecR, -0.3346F, 0.0F, 0.0F);
        SpecR.texOffs(23, 47).addBox(-2.3F, -9.0F, -6.7F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        SpecRB = new ModelRenderer(this);
        SpecRB.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(SpecRB);
        setRotationAngle(SpecRB, -0.3346F, 0.0F, 0.0F);
        SpecRB.texOffs(23, 44).addBox(-2.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
