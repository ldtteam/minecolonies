package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCrafterMale extends ModelBiped
{
    //fields
    ModelRenderer RightArm;
    ModelRenderer LeftArm;
    ModelRenderer RightLeg;
    ModelRenderer LeftLeg;
    ModelRenderer Body;
    ModelRenderer Head;
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

    public ModelEntityCrafterMale()
    {
        textureWidth = 256;
        textureHeight = 128;

        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        RightArm.setRotationPoint(-5F, 2F, 0F);
        RightArm.setTextureSize(256, 128);
        RightArm.mirror = true;
        setRotation(RightArm, 0F, 0F, 0F);

        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        LeftArm.setRotationPoint(5F, 2F, 0F);
        LeftArm.setTextureSize(256, 128);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, 0F);

        RightLeg = new ModelRenderer(this, 0, 16);
        RightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        RightLeg.setRotationPoint(-2F, 12F, 0F);
        RightLeg.setTextureSize(256, 128);
        RightLeg.mirror = true;
        setRotation(RightLeg, 0F, 0F, 0F);

        LeftLeg = new ModelRenderer(this, 0, 16);
        LeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        LeftLeg.setRotationPoint(2F, 12F, 0F);
        LeftLeg.setTextureSize(256, 128);
        LeftLeg.mirror = true;
        setRotation(LeftLeg, 0F, 0F, 0F);

        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 0F, -2F, 8, 12, 4);
        Body.setRotationPoint(0F, 0F, 0F);
        Body.setTextureSize(256, 128);
        Body.mirror = true;
        setRotation(Body, 0F, 0F, 0F);

        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -8F, -4F, 8, 8, 8);
        Head.setRotationPoint(0F, 0F, 0F);
        Head.setTextureSize(256, 128);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);

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

        FingerL = new ModelRenderer(this, 60, 48);
        FingerL.addBox(-0.2F, 6.5F, -2.3F, 1, 3, 1);
        FingerL.setRotationPoint(-5F, 2F, 0F);
        FingerL.setTextureSize(256, 128);
        FingerL.mirror = true;
        setRotation(FingerL, 0F, 0F, 0F);

        RArmRot = new ModelRenderer(this, 58, 27);
        RArmRot.addBox(-2.3F, -1.5F, -1.5F, 1, 1, 3);
        RArmRot.setRotationPoint(-5.2F, 1.65F, 0F);
        RArmRot.setTextureSize(256, 128);
        RArmRot.mirror = true;
        setRotation(RArmRot, 0F, 0F, 0.7853982F);

        FingerM = new ModelRenderer(this, 56, 48);
        FingerM.addBox(-1.5F, 6.5F, -2.3F, 1, 3, 1);
        FingerM.setRotationPoint(-5F, 2F, 0F);
        FingerM.setTextureSize(256, 128);
        FingerM.mirror = true;
        setRotation(FingerM, 0F, 0F, 0F);

        FingerR = new ModelRenderer(this, 52, 48);
        FingerR.addBox(-2.8F, 6.5F, -2.3F, 1, 3, 1);
        FingerR.setRotationPoint(-5F, 2F, 0F);
        FingerR.setTextureSize(256, 128);
        FingerR.mirror = true;
        setRotation(FingerR, 0F, 0F, 0F);

        Vent13 = new ModelRenderer(this, 40, 69);
        Vent13.addBox(-3.3F, 5.49F, -1.5F, 1, 0, 3);
        Vent13.setRotationPoint(-5F, 2F, 0F);
        Vent13.setTextureSize(256, 128);
        Vent13.mirror = true;
        setRotation(Vent13, 0F, 0F, 0F);

        RArmB2 = new ModelRenderer(this, 52, 45);
        RArmB2.addBox(-3.3F, 5.5F, -2.3F, 4, 1, 1);
        RArmB2.setRotationPoint(-5F, 2F, 0F);
        RArmB2.setTextureSize(256, 128);
        RArmB2.mirror = true;
        setRotation(RArmB2, 0F, 0F, 0F);

        RArmB = new ModelRenderer(this, 48, 40);
        RArmB.addBox(-3.3F, -0.5F, 1.3F, 1, 6, 1);
        RArmB.setRotationPoint(-5F, 2F, 0F);
        RArmB.setTextureSize(256, 128);
        RArmB.mirror = true;
        setRotation(RArmB, 0F, 0F, 0F);

        Vent5 = new ModelRenderer(this, 40, 45);
        Vent5.addBox(-3.3F, 1.5F, -1.5F, 1, 0, 3);
        Vent5.setRotationPoint(-5F, 2F, 0F);
        Vent5.setTextureSize(256, 128);
        Vent5.mirror = true;
        setRotation(Vent5, 0F, 0F, 0F);

        Vent7 = new ModelRenderer(this, 40, 51);
        Vent7.addBox(-3.3F, 2.5F, -1.5F, 1, 0, 3);
        Vent7.setRotationPoint(-5F, 2F, 0F);
        Vent7.setTextureSize(256, 128);
        Vent7.mirror = true;
        setRotation(Vent7, 0F, 0F, 0F);

        Vent9 = new ModelRenderer(this, 40, 57);
        Vent9.addBox(-3.3F, 3.5F, -1.5F, 1, 0, 3);
        Vent9.setRotationPoint(-5F, 2F, 0F);
        Vent9.setTextureSize(256, 128);
        Vent9.mirror = true;
        setRotation(Vent9, 0F, 0F, 0F);

        Vent11 = new ModelRenderer(this, 40, 63);
        Vent11.addBox(-3.3F, 4.5F, -1.5F, 1, 0, 3);
        Vent11.setRotationPoint(-5F, 2F, 0F);
        Vent11.setTextureSize(256, 128);
        Vent11.mirror = true;
        setRotation(Vent11, 0F, 0F, 0F);

        RArmL = new ModelRenderer(this, 48, 33);
        RArmL.addBox(-3.3F, -0.5F, -2.3F, 1, 6, 1);
        RArmL.setRotationPoint(-5F, 2F, 0F);
        RArmL.setTextureSize(256, 128);
        RArmL.mirror = true;
        setRotation(RArmL, 0F, 0F, 0F);

        RArmM = new ModelRenderer(this, 52, 33);
        RArmM.addBox(-3.3F, 0.5F, -2.3F, 2, 1, 1);
        RArmM.setRotationPoint(-4F, 2F, 0F);
        RArmM.setTextureSize(256, 128);
        RArmM.mirror = true;
        setRotation(RArmM, 0F, 0F, 0F);

        CoreF = new ModelRenderer(this, 75, 32);
        CoreF.addBox(-1.3F, 1.2F, 1.7F, 2, 2, 1);
        CoreF.setRotationPoint(-4F, 2.7F, 0F);
        CoreF.setTextureSize(256, 128);
        CoreF.mirror = true;
        setRotation(CoreF, 0F, 0F, 0.7853982F);

        RArmFT = new ModelRenderer(this, 58, 31);
        RArmFT.addBox(-2.3F, -2.5F, -2.3F, 1, 3, 1);
        RArmFT.setRotationPoint(-4F, 2F, 0F);
        RArmFT.setTextureSize(256, 128);
        RArmFT.mirror = true;
        setRotation(RArmFT, 0F, 0F, 0F);

        Vent3 = new ModelRenderer(this, 40, 39);
        Vent3.addBox(-3.3F, 0.5F, -1.5F, 1, 0, 3);
        Vent3.setRotationPoint(-5F, 2F, 0F);
        Vent3.setTextureSize(256, 128);
        Vent3.mirror = true;
        setRotation(Vent3, 0F, 0F, 0F);

        RArmBT = new ModelRenderer(this, 62, 31);
        RArmBT.addBox(-2.3F, -2.5F, 1.3F, 1, 3, 1);
        RArmBT.setRotationPoint(-4F, 2F, 0F);
        RArmBT.setTextureSize(256, 128);
        RArmBT.mirror = true;
        setRotation(RArmBT, 0F, 0F, 0F);

        RArmBBB = new ModelRenderer(this, 82, 35);
        RArmBBB.addBox(-3.3F, 6.5F, 1.3F, 3, 1, 1);
        RArmBBB.setRotationPoint(-4F, 2F, 0F);
        RArmBBB.setTextureSize(256, 128);
        RArmBBB.mirror = true;
        setRotation(RArmBBB, 0F, 0F, 0F);

        RArmBBM = new ModelRenderer(this, 82, 31);
        RArmBBM.addBox(-2.3F, 3.5F, 1.3F, 1, 3, 1);
        RArmBBM.setRotationPoint(-4F, 2F, 0F);
        RArmBBM.setTextureSize(256, 128);
        RArmBBM.mirror = true;
        setRotation(RArmBBM, 0F, 0F, 0F);

        CoreB = new ModelRenderer(this, 67, 31);
        CoreB.addBox(-1.3F, 1.2F, 1.5F, 3, 3, 1);
        CoreB.setRotationPoint(-4F, 2F, 0F);
        CoreB.setTextureSize(256, 128);
        CoreB.mirror = true;
        setRotation(CoreB, 0F, 0F, 0.7853982F);

        Vent12 = new ModelRenderer(this, 40, 66);
        Vent12.addBox(-3.3F, 5F, -1.5F, 1, 0, 3);
        Vent12.setRotationPoint(-5F, 2F, 0F);
        Vent12.setTextureSize(256, 128);
        Vent12.mirror = true;
        setRotation(Vent12, 0F, 0F, 0F);

        Vent10 = new ModelRenderer(this, 40, 60);
        Vent10.addBox(-3.3F, 4F, -1.5F, 1, 0, 3);
        Vent10.setRotationPoint(-5F, 2F, 0F);
        Vent10.setTextureSize(256, 128);
        Vent10.mirror = true;
        setRotation(Vent10, 0F, 0F, 0F);

        Vent8 = new ModelRenderer(this, 40, 54);
        Vent8.addBox(-3.3F, 3F, -1.5F, 1, 0, 3);
        Vent8.setRotationPoint(-5F, 2F, 0F);
        Vent8.setTextureSize(256, 128);
        Vent8.mirror = true;
        setRotation(Vent8, 0F, 0F, 0F);

        Vent6 = new ModelRenderer(this, 40, 48);
        Vent6.addBox(-3.3F, 2F, -1.5F, 1, 0, 3);
        Vent6.setRotationPoint(-5F, 2F, 0F);
        Vent6.setTextureSize(256, 128);
        Vent6.mirror = true;
        setRotation(Vent6, 0F, 0F, 0F);

        Vent4 = new ModelRenderer(this, 40, 42);
        Vent4.addBox(-3.3F, 1F, -1.5F, 1, 0, 3);
        Vent4.setRotationPoint(-5F, 2F, 0F);
        Vent4.setTextureSize(256, 128);
        Vent4.mirror = true;
        setRotation(Vent4, 0F, 0F, 0F);

        Vent1 = new ModelRenderer(this, 40, 33);
        Vent1.addBox(-3.3F, -0.49F, -1.5F, 1, 0, 3);
        Vent1.setRotationPoint(-5F, 2F, 0F);
        Vent1.setTextureSize(256, 128);
        Vent1.mirror = true;
        setRotation(Vent1, 0F, 0F, 0F);

        Vent2 = new ModelRenderer(this, 40, 36);
        Vent2.addBox(-3.3F, 0F, -1.5F, 1, 0, 3);
        Vent2.setRotationPoint(-5F, 2F, 0F);
        Vent2.setTextureSize(256, 128);
        Vent2.mirror = true;
        setRotation(Vent2, 0F, 0F, 0F);
    }

    @Override
    public void render(
            final Entity entity,
            final float limbSwing,
            final float limbSwingAmount,
            final float ageInTicks,
            final float netHeadYaw,
            final float headPitch,
            final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        RightArm.render(scaleFactor);
        LeftArm.render(scaleFactor);
        RightLeg.render(scaleFactor);
        LeftLeg.render(scaleFactor);
        Body.render(scaleFactor);
        Head.render(scaleFactor);
        HatT.render(scaleFactor);
        BrimBa.render(scaleFactor);
        BrimR.render(scaleFactor);
        BrimL.render(scaleFactor);
        BrimF.render(scaleFactor);
        BrimBo.render(scaleFactor);
        HatM.render(scaleFactor);
        SpecLB.render(scaleFactor);
        SpecL.render(scaleFactor);
        SpecMid.render(scaleFactor);
        SpecR.render(scaleFactor);
        SpecRB.render(scaleFactor);
        FingerL.render(scaleFactor);
        RArmRot.render(scaleFactor);
        FingerM.render(scaleFactor);
        FingerR.render(scaleFactor);
        Vent13.render(scaleFactor);
        RArmB2.render(scaleFactor);
        RArmB.render(scaleFactor);
        Vent5.render(scaleFactor);
        Vent7.render(scaleFactor);
        Vent9.render(scaleFactor);
        Vent11.render(scaleFactor);
        RArmL.render(scaleFactor);
        RArmM.render(scaleFactor);
        CoreF.render(scaleFactor);
        RArmFT.render(scaleFactor);
        Vent3.render(scaleFactor);
        RArmBT.render(scaleFactor);
        RArmBBB.render(scaleFactor);
        RArmBBM.render(scaleFactor);
        CoreB.render(scaleFactor);
        Vent12.render(scaleFactor);
        Vent10.render(scaleFactor);
        Vent8.render(scaleFactor);
        Vent6.render(scaleFactor);
        Vent4.render(scaleFactor);
        Vent1.render(scaleFactor);
        Vent2.render(scaleFactor);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(
            final float limbSwing,
            final float limbSwingAmount,
            final float ageInTicks,
            final float netHeadYaw,
            final float headPitch,
            final float scaleFactor,
            final Entity entityIn)
    {
        bipedHead.rotateAngleY = netHeadYaw / 57.29578F;
        bipedHead.rotateAngleX = headPitch / 57.29578F;
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;

        HatT.rotateAngleY = bipedHead.rotateAngleY;
        HatT.rotateAngleX = bipedHead.rotateAngleX;
        HatM.rotateAngleY = bipedHead.rotateAngleY;
        HatM.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 2.0F * limbSwingAmount * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.73F * limbSwingAmount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 0.73F * limbSwingAmount;

        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
