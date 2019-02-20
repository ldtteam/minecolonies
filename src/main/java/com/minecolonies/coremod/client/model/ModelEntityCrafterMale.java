package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCrafterMale extends ModelBiped
{
    public ModelEntityCrafterMale()
    {
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

        ModelRenderer FingerL;
        ModelRenderer RArmRot;
        ModelRenderer FingerM;
        ModelRenderer FingerR;
        ModelRenderer Vent13;
        ModelRenderer RArmB2;
        ModelRenderer RArmB;
        ModelRenderer Vent5;
        ModelRenderer Vent7;
        ModelRenderer Vent9;
        ModelRenderer Vent11;
        ModelRenderer RArmL;
        ModelRenderer RArmM;
        ModelRenderer CoreF;
        ModelRenderer RArmFT;
        ModelRenderer Vent3;
        ModelRenderer RArmBT;
        ModelRenderer RArmBBB;
        ModelRenderer RArmBBM;
        ModelRenderer CoreB;
        ModelRenderer Vent12;
        ModelRenderer Vent10;
        ModelRenderer Vent8;
        ModelRenderer Vent6;
        ModelRenderer Vent4;
        ModelRenderer Vent1;
        ModelRenderer Vent2;

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        HatT = new ModelRenderer(this, 0, 76);
        HatT.addBox(-2F, -11.5F, -2.7F, 4, 1, 5);
        HatT.setRotationPoint(0F, 0F, 0F);
        HatT.setTextureSize(256, 128);
        HatT.mirror = true;
        setRotation(HatT, -0.0743572F, 0F, 0F);

        BrimBa = new ModelRenderer(this, 0, 45);
        BrimBa.addBox(-5F, -9F, 3.5F, 10, 1, 1);
        BrimBa.setRotationPoint(0F, 0F, 0F);
        BrimBa.setTextureSize(256, 128);
        BrimBa.mirror = true;
        setRotation(BrimBa, -0.0743572F, 0F, 0F);

        BrimR = new ModelRenderer(this, 0, 57);
        BrimR.addBox(-5.5F, -9F, -5F, 1, 1, 9);
        BrimR.setRotationPoint(0F, 0F, 0F);
        BrimR.setTextureSize(256, 128);
        BrimR.mirror = true;
        setRotation(BrimR, -0.0743572F, 0F, 0F);

        BrimL = new ModelRenderer(this, 0, 47);
        BrimL.addBox(4.5F, -9F, -5F, 1, 1, 9);
        BrimL.setRotationPoint(0F, 0F, 0F);
        BrimL.setTextureSize(256, 128);
        BrimL.mirror = true;
        setRotation(BrimL, -0.0743572F, 0F, 0F);

        BrimF = new ModelRenderer(this, 0, 43);
        BrimF.addBox(-5F, -9F, -5.5F, 10, 1, 1);
        BrimF.setRotationPoint(0F, 0F, 0F);
        BrimF.setTextureSize(256, 128);
        BrimF.mirror = true;
        setRotation(BrimF, -0.0743572F, 0F, 0F);

        BrimBo = new ModelRenderer(this, 0, 33);
        BrimBo.addBox(-5F, -8.5F, -5F, 10, 1, 9);
        BrimBo.setRotationPoint(0F, 0F, 0F);
        BrimBo.setTextureSize(256, 128);
        BrimBo.mirror = true;
        setRotation(BrimBo, -0.0743572F, 0F, 0F);

        HatM = new ModelRenderer(this, 0, 67);
        HatM.addBox(-3.5F, -10.5F, -3.7F, 7, 2, 7);
        HatM.setRotationPoint(0F, 0F, 0F);
        HatM.setTextureSize(256, 128);
        HatM.mirror = true;
        setRotation(HatM, -0.0743572F, 0F, 0F);

        SpecLB = new ModelRenderer(this, 30, 47);
        SpecLB.addBox(0.8F, -9.5F, -6.6F, 2, 2, 1);
        SpecLB.setRotationPoint(0F, 0F, 0F);
        SpecLB.setTextureSize(256, 128);
        SpecLB.mirror = true;
        setRotation(SpecLB, -0.3346075F, 0F, 0F);

        SpecL = new ModelRenderer(this, 30, 50);
        SpecL.addBox(1.3F, -9F, -7.4F, 1, 1, 2);
        SpecL.setRotationPoint(0F, 0F, 0F);
        SpecL.setTextureSize(256, 128);
        SpecL.mirror = true;
        setRotation(SpecL, -0.3346075F, 0F, 0F);

        SpecMid = new ModelRenderer(this, 30, 44);
        SpecMid.addBox(-1F, -9F, -6.3F, 2, 1, 1);
        SpecMid.setRotationPoint(0F, 0F, 0F);
        SpecMid.setTextureSize(256, 128);
        SpecMid.mirror = true;
        setRotation(SpecMid, -0.3346075F, 0F, 0F);

        SpecR = new ModelRenderer(this, 23, 47);
        SpecR.addBox(-2.3F, -9F, -6.7F, 1, 1, 1);
        SpecR.setRotationPoint(0F, 0F, 0F);
        SpecR.setTextureSize(256, 128);
        SpecR.mirror = true;
        setRotation(SpecR, -0.3346075F, 0F, 0F);

        SpecRB = new ModelRenderer(this, 23, 44);
        SpecRB.addBox(-2.8F, -9.5F, -6.6F, 2, 2, 1);
        SpecRB.setRotationPoint(0F, 0F, 0F);
        SpecRB.setTextureSize(256, 128);
        SpecRB.mirror = true;
        setRotation(SpecRB, -0.3346075F, 0F, 0F);

        // Start arm decoration

        FingerL = new ModelRenderer(this, 60, 48);
        FingerL.addBox(4.8F, 4.5F, -2.3F, 1, 3, 1);
        FingerL.setRotationPoint(-5F, 2F, 0F);
        FingerL.setTextureSize(256, 128);
        FingerL.mirror = true;
        setRotation(FingerL, 0F, 0F, 0F);

        FingerM = new ModelRenderer(this, 56, 48);
        FingerM.addBox(3.5F, 4.5F, -2.3F, 1, 3, 1);
        FingerM.setRotationPoint(-5F, 2F, 0F);
        FingerM.setTextureSize(256, 128);
        FingerM.mirror = true;
        setRotation(FingerM, 0F, 0F, 0F);

        RArmRot = new ModelRenderer(this, 58, 27);
        RArmRot.addBox(-0.2F, -6.5F, -1.5F, 1, 1, 3);
        RArmRot.setRotationPoint(-5.2F, 1.65F, 0F);
        RArmRot.setTextureSize(256, 128);
        RArmRot.mirror = true;
        setRotation(RArmRot, 0F, 0F, 0.7853982F);

        FingerR = new ModelRenderer(this, 52, 48);
        FingerR.addBox(2.2F, 4.5F, -2.3F, 1, 3, 1);
        FingerR.setRotationPoint(-5F, 2F, 0F);
        FingerR.setTextureSize(256, 128);
        FingerR.mirror = true;
        setRotation(FingerR, 0F, 0F, 0F);

        Vent13 = new ModelRenderer(this, 40, 69);
        Vent13.addBox(1.7F, 3.5F, -1.5F, 1, 0, 3);
        Vent13.setRotationPoint(-5F, 2F, 0F);
        Vent13.setTextureSize(256, 128);
        Vent13.mirror = true;
        setRotation(Vent13, 0F, 0F, 0F);

        RArmB2 = new ModelRenderer(this, 52, 45);
        RArmB2.addBox(1.7F, 3.5F, -2.3F, 4, 1, 1);
        RArmB2.setRotationPoint(-5F, 2F, 0F);
        RArmB2.setTextureSize(256, 128);
        RArmB2.mirror = true;
        setRotation(RArmB2, 0F, 0F, 0F);

        RArmB = new ModelRenderer(this, 48, 40);
        RArmB.addBox(1.7F, -2.5F, 1.3F, 1, 6, 1);
        RArmB.setRotationPoint(-5F, 2F, 0F);
        RArmB.setTextureSize(256, 128);
        RArmB.mirror = true;
        setRotation(RArmB, 0F, 0F, 0F);

        Vent5 = new ModelRenderer(this, 40, 45);
        Vent5.addBox(1.7F, -0.5F, -1.5F, 1, 0, 3);
        Vent5.setRotationPoint(-5F, 2F, 0F);
        Vent5.setTextureSize(256, 128);
        Vent5.mirror = true;
        setRotation(Vent5, 0F, 0F, 0F);

        Vent7 = new ModelRenderer(this, 40, 51);
        Vent7.addBox(1.7F, 0.5F, -1.5F, 1, 0, 3);
        Vent7.setRotationPoint(-5F, 2F, 0F);
        Vent7.setTextureSize(256, 128);
        Vent7.mirror = true;
        setRotation(Vent7, 0F, 0F, 0F);

        Vent9 = new ModelRenderer(this, 40, 57);
        Vent9.addBox(1.7F, 1.5F, -1.5F, 1, 0, 3);
        Vent9.setRotationPoint(-5F, 2F, 0F);
        Vent9.setTextureSize(256, 128);
        Vent9.mirror = true;
        setRotation(Vent9, 0F, 0F, 0F);

        Vent11 = new ModelRenderer(this, 40, 63);
        Vent11.addBox(1.7F, 2.5F, -1.5F, 1, 0, 3);
        Vent11.setRotationPoint(-5F, 2F, 0F);
        Vent11.setTextureSize(256, 128);
        Vent11.mirror = true;
        setRotation(Vent11, 0F, 0F, 0F);

        RArmL = new ModelRenderer(this, 48, 33);
        RArmL.addBox(1.7F, -2.5F, -2.3F, 1, 6, 1);
        RArmL.setRotationPoint(-5F, 2F, 0F);
        RArmL.setTextureSize(256, 128);
        RArmL.mirror = true;
        setRotation(RArmL, 0F, 0F, 0F);

        RArmM = new ModelRenderer(this, 52, 33);
        RArmM.addBox(1.7F, -1.5F, -2.3F, 2, 1, 1);
        RArmM.setRotationPoint(-4F, 2F, 0F);
        RArmM.setTextureSize(256, 128);
        RArmM.mirror = true;
        setRotation(RArmM, 0F, 0F, 0F);

        CoreF = new ModelRenderer(this, 75, 32);
        CoreF.addBox(0.8F, -3.7F, 1.7F, 2, 2, 1);
        CoreF.setRotationPoint(-4F, 2.7F, 0F);
        CoreF.setTextureSize(256, 128);
        CoreF.mirror = true;
        setRotation(CoreF, 0F, 0F, 0.7853982F);

        CoreB = new ModelRenderer(this, 67, 31);
        CoreB.addBox(0.8F, -3.7F, 1.5F, 3, 3, 1);
        CoreB.setRotationPoint(-4F, 2F, 0F);
        CoreB.setTextureSize(256, 128);
        CoreB.mirror = true;
        setRotation(CoreB, 0F, 0F, 0.7853982F);

        RArmFT = new ModelRenderer(this, 58, 31);
        RArmFT.addBox(2.7F, -4.5F, -2.3F, 1, 3, 1);
        RArmFT.setRotationPoint(-4F, 2F, 0F);
        RArmFT.setTextureSize(256, 128);
        RArmFT.mirror = true;
        setRotation(RArmFT, 0F, 0F, 0F);

        Vent3 = new ModelRenderer(this, 40, 39);
        Vent3.addBox(1.7F, -1.5F, -1.5F, 1, 0, 3);
        Vent3.setRotationPoint(-5F, 2F, 0F);
        Vent3.setTextureSize(256, 128);
        Vent3.mirror = true;
        setRotation(Vent3, 0F, 0F, 0F);

        RArmBT = new ModelRenderer(this, 62, 31);
        RArmBT.addBox(2.7F, -4.5F, 1.3F, 1, 3, 1);
        RArmBT.setRotationPoint(-4F, 2F, 0F);
        RArmBT.setTextureSize(256, 128);
        RArmBT.mirror = true;
        setRotation(RArmBT, 0F, 0F, 0F);

        RArmBBB = new ModelRenderer(this, 82, 35);
        RArmBBB.addBox(1.7F, 4.5F, 1.3F, 3, 1, 1);
        RArmBBB.setRotationPoint(-4F, 2F, 0F);
        RArmBBB.setTextureSize(256, 128);
        RArmBBB.mirror = true;
        setRotation(RArmBBB, 0F, 0F, 0F);

        RArmBBM = new ModelRenderer(this, 82, 31);
        RArmBBM.addBox(2.7F, 1.5F, 1.3F, 1, 3, 1);
        RArmBBM.setRotationPoint(-4F, 2F, 0F);
        RArmBBM.setTextureSize(256, 128);
        RArmBBM.mirror = true;
        setRotation(RArmBBM, 0F, 0F, 0F);

        Vent12 = new ModelRenderer(this, 40, 66);
        Vent12.addBox(1.7F, 3F, -1.5F, 1, 0, 3);
        Vent12.setRotationPoint(-5F, 2F, 0F);
        Vent12.setTextureSize(256, 128);
        Vent12.mirror = true;
        setRotation(Vent12, 0F, 0F, 0F);

        Vent10 = new ModelRenderer(this, 40, 60);
        Vent10.addBox(1.7F, 2F, -1.5F, 1, 0, 3);
        Vent10.setRotationPoint(-5F, 2F, 0F);
        Vent10.setTextureSize(256, 128);
        Vent10.mirror = true;
        setRotation(Vent10, 0F, 0F, 0F);

        Vent8 = new ModelRenderer(this, 40, 54);
        Vent8.addBox(1.7F, 1F, -1.5F, 1, 0, 3);
        Vent8.setRotationPoint(-5F, 2F, 0F);
        Vent8.setTextureSize(256, 128);
        Vent8.mirror = true;
        setRotation(Vent8, 0F, 0F, 0F);

        Vent6 = new ModelRenderer(this, 40, 48);
        Vent6.addBox(1.7F, 0F, -1.5F, 1, 0, 3);
        Vent6.setRotationPoint(-5F, 2F, 0F);
        Vent6.setTextureSize(256, 128);
        Vent6.mirror = true;
        setRotation(Vent6, 0F, 0F, 0F);

        Vent4 = new ModelRenderer(this, 40, 42);
        Vent4.addBox(1.7F, -1F, -1.5F, 1, 0, 3);
        Vent4.setRotationPoint(-5F, 2F, 0F);
        Vent4.setTextureSize(256, 128);
        Vent4.mirror = true;
        setRotation(Vent4, 0F, 0F, 0F);

        Vent1 = new ModelRenderer(this, 40, 33);
        Vent1.addBox(1.7F, -2.5F, -1.5F, 1, 0, 3);
        Vent1.setRotationPoint(-5F, 2F, 0F);
        Vent1.setTextureSize(256, 128);
        Vent1.mirror = true;
        setRotation(Vent1, 0F, 0F, 0F);

        Vent2 = new ModelRenderer(this, 40, 36);
        Vent2.addBox(1.7F, -2F, -1.5F, 1, 0, 3);
        Vent2.setRotationPoint(-5F, 2F, 0F);
        Vent2.setTextureSize(256, 128);
        Vent2.mirror = true;
        setRotation(Vent2, 0F, 0F, 0F);

        this.bipedHead.addChild(HatT);
        this.bipedHead.addChild(BrimBa);
        this.bipedHead.addChild(BrimR);
        this.bipedHead.addChild(BrimL);
        this.bipedHead.addChild(BrimF);
        this.bipedHead.addChild(BrimBo);
        this.bipedHead.addChild(HatM);
        this.bipedHead.addChild(SpecLB);
        this.bipedHead.addChild(SpecL);
        this.bipedHead.addChild(SpecMid);
        this.bipedHead.addChild(SpecR);
        this.bipedHead.addChild(SpecRB);


        this.bipedRightArm.addChild(RArmRot);

        this.bipedRightArm.addChild(FingerL);
        this.bipedRightArm.addChild(FingerM);
        this.bipedRightArm.addChild(FingerR);

        this.bipedRightArm.addChild(RArmB2);
        this.bipedRightArm.addChild(RArmB);
        this.bipedRightArm.addChild(RArmL);
        this.bipedRightArm.addChild(RArmM);

        this.bipedRightArm.addChild(CoreF);
        this.bipedRightArm.addChild(CoreB);

        this.bipedRightArm.addChild(RArmFT);
        this.bipedRightArm.addChild(RArmBT);
        this.bipedRightArm.addChild(RArmBBB);
        this.bipedRightArm.addChild(RArmBBM);

        this.bipedRightArm.addChild(Vent12);
        this.bipedRightArm.addChild(Vent10);
        this.bipedRightArm.addChild(Vent8);
        this.bipedRightArm.addChild(Vent6);
        this.bipedRightArm.addChild(Vent4);
        this.bipedRightArm.addChild(Vent1);
        this.bipedRightArm.addChild(Vent2);
        this.bipedRightArm.addChild(Vent5);
        this.bipedRightArm.addChild(Vent7);
        this.bipedRightArm.addChild(Vent9);
        this.bipedRightArm.addChild(Vent13);
        this.bipedRightArm.addChild(Vent11);
        this.bipedRightArm.addChild(Vent3);
    }

    @Override
    public void render(
      @NotNull final Entity entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
