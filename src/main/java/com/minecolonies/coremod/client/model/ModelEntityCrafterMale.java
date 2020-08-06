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

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        RArmRot = new ModelRenderer(this);
        RArmRot.setRotationPoint(-5.2F, 1.65F, 0.0F);
        bipedRightArm.addChild(RArmRot);
        setRotationAngle(RArmRot, 0.0F, 0.0F, 0.7854F);
        RArmRot.setTextureOffset(58, 27).addBox(-0.2F, -6.5F, -1.5F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        FingerL = new ModelRenderer(this);
        FingerL.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(FingerL);
        FingerL.setTextureOffset(60, 48).addBox(4.8F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        FingerM = new ModelRenderer(this);
        FingerM.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(FingerM);
        FingerM.setTextureOffset(56, 48).addBox(3.5F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        FingerR = new ModelRenderer(this);
        FingerR.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(FingerR);
        FingerR.setTextureOffset(52, 48).addBox(2.2F, 4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmB2 = new ModelRenderer(this);
        RArmB2.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmB2);
        RArmB2.setTextureOffset(52, 45).addBox(1.7F, 3.5F, -2.3F, 4.0F, 1.0F, 1.0F, 0.0F, true);

        RArmB = new ModelRenderer(this);
        RArmB.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmB);
        RArmB.setTextureOffset(48, 40).addBox(1.7F, -2.5F, 1.3F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        RArmL = new ModelRenderer(this);
        RArmL.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmL);
        RArmL.setTextureOffset(48, 33).addBox(1.7F, -2.5F, -2.3F, 1.0F, 6.0F, 1.0F, 0.0F, true);

        RArmM = new ModelRenderer(this);
        RArmM.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmM);
        RArmM.setTextureOffset(52, 33).addBox(1.7F, -1.5F, -2.3F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        CoreF = new ModelRenderer(this);
        CoreF.setRotationPoint(-4.0F, 2.7F, 0.0F);
        bipedRightArm.addChild(CoreF);
        setRotationAngle(CoreF, 0.0F, 0.0F, 0.7854F);
        CoreF.setTextureOffset(75, 32).addBox(0.8F, -3.7F, 1.7F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        CoreB = new ModelRenderer(this);
        CoreB.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(CoreB);
        setRotationAngle(CoreB, 0.0F, 0.0F, 0.7854F);
        CoreB.setTextureOffset(67, 31).addBox(0.8F, -3.7F, 1.5F, 3.0F, 3.0F, 1.0F, 0.0F, true);

        RArmFT = new ModelRenderer(this);
        RArmFT.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmFT);
        RArmFT.setTextureOffset(58, 31).addBox(2.7F, -4.5F, -2.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmBT = new ModelRenderer(this);
        RArmBT.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmBT);
        RArmBT.setTextureOffset(62, 31).addBox(2.7F, -4.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        RArmBBB = new ModelRenderer(this);
        RArmBBB.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmBBB);
        RArmBBB.setTextureOffset(82, 35).addBox(1.7F, 4.5F, 1.3F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        RArmBBM = new ModelRenderer(this);
        RArmBBM.setRotationPoint(-4.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(RArmBBM);
        RArmBBM.setTextureOffset(82, 31).addBox(2.7F, 1.5F, 1.3F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        Vent12 = new ModelRenderer(this);
        Vent12.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent12);
        Vent12.setTextureOffset(40, 66).addBox(1.7F, 3.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent10 = new ModelRenderer(this);
        Vent10.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent10);
        Vent10.setTextureOffset(40, 60).addBox(1.7F, 2.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent8 = new ModelRenderer(this);
        Vent8.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent8);
        Vent8.setTextureOffset(40, 54).addBox(1.7F, 1.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent6 = new ModelRenderer(this);
        Vent6.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent6);
        Vent6.setTextureOffset(40, 48).addBox(1.7F, 0.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent4 = new ModelRenderer(this);
        Vent4.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent4);
        Vent4.setTextureOffset(40, 42).addBox(1.7F, -1.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent1 = new ModelRenderer(this);
        Vent1.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent1);
        Vent1.setTextureOffset(40, 33).addBox(1.7F, -2.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent2 = new ModelRenderer(this);
        Vent2.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent2);
        Vent2.setTextureOffset(40, 36).addBox(1.7F, -2.0F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent5 = new ModelRenderer(this);
        Vent5.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent5);
        Vent5.setTextureOffset(40, 45).addBox(1.7F, -0.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent7 = new ModelRenderer(this);
        Vent7.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent7);
        Vent7.setTextureOffset(40, 51).addBox(1.7F, 0.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent9 = new ModelRenderer(this);
        Vent9.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent9);
        Vent9.setTextureOffset(40, 57).addBox(1.7F, 1.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent13 = new ModelRenderer(this);
        Vent13.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent13);
        Vent13.setTextureOffset(40, 69).addBox(1.7F, 3.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent11 = new ModelRenderer(this);
        Vent11.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent11);
        Vent11.setTextureOffset(40, 63).addBox(1.7F, 2.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        Vent3 = new ModelRenderer(this);
        Vent3.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(Vent3);
        Vent3.setTextureOffset(40, 39).addBox(1.7F, -1.5F, -1.5F, 1.0F, 0.0F, 3.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headdetail);
        headdetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        HatT = new ModelRenderer(this);
        HatT.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HatT);
        setRotationAngle(HatT, -0.0744F, 0.0F, 0.0F);
        HatT.setTextureOffset(0, 76).addBox(-2.0F, -11.5F, -2.7F, 4.0F, 1.0F, 5.0F, 0.0F, true);

        BrimBa = new ModelRenderer(this);
        BrimBa.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimBa);
        setRotationAngle(BrimBa, -0.0744F, 0.0F, 0.0F);
        BrimBa.setTextureOffset(0, 45).addBox(-5.0F, -9.0F, 3.5F, 10.0F, 1.0F, 1.0F, 0.0F, true);

        BrimR = new ModelRenderer(this);
        BrimR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimR);
        setRotationAngle(BrimR, -0.0744F, 0.0F, 0.0F);
        BrimR.setTextureOffset(0, 57).addBox(-5.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        BrimL = new ModelRenderer(this);
        BrimL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimL);
        setRotationAngle(BrimL, -0.0744F, 0.0F, 0.0F);
        BrimL.setTextureOffset(0, 47).addBox(4.5F, -9.0F, -5.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        BrimF = new ModelRenderer(this);
        BrimF.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimF);
        setRotationAngle(BrimF, -0.0744F, 0.0F, 0.0F);
        BrimF.setTextureOffset(0, 43).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 1.0F, 1.0F, 0.0F, true);

        BrimBo = new ModelRenderer(this);
        BrimBo.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(BrimBo);
        setRotationAngle(BrimBo, -0.0744F, 0.0F, 0.0F);
        BrimBo.setTextureOffset(0, 33).addBox(-5.0F, -8.5F, -5.0F, 10.0F, 1.0F, 9.0F, 0.0F, true);

        HatM = new ModelRenderer(this);
        HatM.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HatM);
        setRotationAngle(HatM, -0.0744F, 0.0F, 0.0F);
        HatM.setTextureOffset(0, 67).addBox(-3.5F, -10.5F, -3.7F, 7.0F, 2.0F, 7.0F, 0.0F, true);

        SpecLB = new ModelRenderer(this);
        SpecLB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(SpecLB);
        setRotationAngle(SpecLB, -0.3346F, 0.0F, 0.0F);
        SpecLB.setTextureOffset(30, 47).addBox(0.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        SpecL = new ModelRenderer(this);
        SpecL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(SpecL);
        setRotationAngle(SpecL, -0.3346F, 0.0F, 0.0F);
        SpecL.setTextureOffset(30, 50).addBox(1.3F, -9.0F, -7.4F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        SpecMid = new ModelRenderer(this);
        SpecMid.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(SpecMid);
        setRotationAngle(SpecMid, -0.3346F, 0.0F, 0.0F);
        SpecMid.setTextureOffset(30, 44).addBox(-1.0F, -9.0F, -6.3F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        SpecR = new ModelRenderer(this);
        SpecR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(SpecR);
        setRotationAngle(SpecR, -0.3346F, 0.0F, 0.0F);
        SpecR.setTextureOffset(23, 47).addBox(-2.3F, -9.0F, -6.7F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        SpecRB = new ModelRenderer(this);
        SpecRB.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(SpecRB);
        setRotationAngle(SpecRB, -0.3346F, 0.0F, 0.0F);
        SpecRB.setTextureOffset(23, 44).addBox(-2.8F, -9.5F, -6.6F, 2.0F, 2.0F, 1.0F, 0.0F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
