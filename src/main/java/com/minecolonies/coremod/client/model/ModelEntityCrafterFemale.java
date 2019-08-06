package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCrafterFemale extends BipedModel
{
    public ModelEntityCrafterFemale()
    {
        RendererModel core;
        RendererModel frontBeam;
        RendererModel SideBeam;
        RendererModel skirtBa;
        RendererModel skirtF;
        RendererModel skirtR;
        RendererModel skirtBo1;
        RendererModel skirtT1;
        RendererModel skirtL;
        RendererModel skirtBo2;
        RendererModel chest;
        RendererModel HairBack1;
        RendererModel HairBack3;
        RendererModel HairBack2;
        RendererModel HairBack6;
        RendererModel HairBack5;
        RendererModel HairBack4;
        RendererModel HairBack7;
        RendererModel HairBack8;
        RendererModel HairBack9;
        RendererModel HairBack10;
        RendererModel HairBack11;
        RendererModel HairBack12;
        RendererModel HairBack13;
        RendererModel HairBack14;
        RendererModel lens2;
        RendererModel strap;
        RendererModel lens1;
        RendererModel back;

        textureWidth = 256;
        textureHeight = 128;

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        core = new RendererModel(this, 38, 93);
        core.addBox(3F, 8.6F, -2F, 2, 2, 2);
        core.setRotationPoint(-4.6F, 2F, 0F);
        core.setTextureSize(256, 128);
        core.mirror = true;
        setRotation(core, 0F, 0.7853982F, 0F);

        frontBeam = new RendererModel(this, 25, 78);
        frontBeam.addBox(3.5F, -4.5F, -2.5F, 1, 13, 5);
        frontBeam.setRotationPoint(-5F, 2F, 0F);
        frontBeam.setTextureSize(256, 128);
        frontBeam.mirror = true;
        setRotation(frontBeam, 0F, 0F, 0F);

        SideBeam = new RendererModel(this, 38, 78);
        SideBeam.addBox(1.5F, -4.5F, -0.5F, 5, 13, 1);
        SideBeam.setRotationPoint(-5F, 2F, 0F);
        SideBeam.setTextureSize(256, 128);
        SideBeam.mirror = true;
        setRotation(SideBeam, 0F, 0F, 0F);

        skirtBa = new RendererModel(this, 47, 41);
        skirtBa.addBox(-4.5F, 11.1F, -4.5F, 9, 6, 2);
        skirtBa.setRotationPoint(0F, 0F, 0F);
        skirtBa.setTextureSize(256, 128);
        skirtBa.mirror = true;
        setRotation(skirtBa, 0.4712389F, 0F, 0F);

        skirtF = new RendererModel(this, 25, 41);
        skirtF.addBox(-4.5F, 11.1F, 2.5F, 9, 6, 2);
        skirtF.setRotationPoint(0F, 0F, 0F);
        skirtF.setTextureSize(256, 128);
        skirtF.mirror = true;
        setRotation(skirtF, -0.4712389F, 0F, 0F);

        skirtR = new RendererModel(this, 53, 49);
        skirtR.addBox(-17.9F, -2.2F, -3F, 6, 1, 6);
        skirtR.setRotationPoint(0F, 0F, 0F);
        skirtR.setTextureSize(256, 128);
        skirtR.mirror = true;
        setRotation(skirtR, 0F, 0F, -1.396263F);

        skirtBo1 = new RendererModel(this, 29, 56);
        skirtBo1.addBox(-4.5F, 15.4F, -4F, 9, 1, 8);
        skirtBo1.setRotationPoint(0F, 0F, 0F);
        skirtBo1.setTextureSize(256, 128);
        skirtBo1.mirror = true;
        setRotation(skirtBo1, 0F, 0F, 0F);
        
        skirtT1 = new RendererModel(this, 25, 33);
        skirtT1.addBox(-4.5F, 11F, -3F, 9, 2, 6);
        skirtT1.setRotationPoint(0F, 0F, 0F);
        skirtT1.setTextureSize(256, 128);
        skirtT1.mirror = true;
        setRotation(skirtT1, 0F, 0F, 0F);

        skirtL = new RendererModel(this, 29, 49);
        skirtL.addBox(11.9F, -2.2F, -3F, 6, 1, 6);
        skirtL.setRotationPoint(0F, 0F, 0F);
        skirtL.setTextureSize(256, 128);
        skirtL.mirror = true;
        setRotation(skirtL, 0F, 0F, 1.396263F);

        skirtBo2 = new RendererModel(this, 29, 65);
        skirtBo2.addBox(-4.5F, 16.4F, -5.5F, 9, 1, 11);
        skirtBo2.setRotationPoint(0F, 0F, 0F);
        skirtBo2.setTextureSize(256, 128);
        skirtBo2.mirror = true;
        setRotation(skirtBo2, 0F, 0F, 0F);

        chest = new RendererModel(this, 0, 32);
        chest.addBox(-3.5F, -0.5F, -5.5F, 7, 3, 3);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, 0.9341126F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        HairBack1 = new RendererModel(this, 0, 80);
        HairBack1.addBox(-4.5F, -5.5F, -4.5F, 1, 1, 1);
        HairBack1.setRotationPoint(0F, 0F, 0F);
        HairBack1.setTextureSize(256, 128);
        HairBack1.mirror = true;
        setRotation(HairBack1, 0F, 0F, 0F);

        HairBack3 = new RendererModel(this, 0, 69);
        HairBack3.addBox(-4.5F, -8.5F, -4.5F, 9, 2, 4);
        HairBack3.setRotationPoint(0F, 0F, 0F);
        HairBack3.setTextureSize(256, 128);
        HairBack3.mirror = true;
        setRotation(HairBack3, 0F, 0F, 0F);

        HairBack2 = new RendererModel(this, 5, 80);
        HairBack2.addBox(3.5F, -7.1F, -0.55F, 1, 3, 1);
        HairBack2.setRotationPoint(0F, 0F, 0F);
        HairBack2.setTextureSize(256, 128);
        HairBack2.mirror = true;
        setRotation(HairBack2, 0.6108652F, 0F, 0F);

        HairBack6 = new RendererModel(this, 0, 48);
        HairBack6.addBox(-0.1F, -1F, 5.1F, 1, 2, 1);
        HairBack6.setRotationPoint(0F, 1F, 0F);
        HairBack6.setTextureSize(256, 128);
        HairBack6.mirror = true;
        setRotation(HairBack6, 0F, 1.933288F, 0F);

        HairBack5 = new RendererModel(this, 0, 75);
        HairBack5.addBox(2.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack5.setRotationPoint(0F, 0F, 0F);
        HairBack5.setTextureSize(256, 128);
        HairBack5.mirror = true;
        setRotation(HairBack5, 0F, 0F, 0F);

        HairBack4 = new RendererModel(this, 12, 75);
        HairBack4.addBox(-4.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack4.setRotationPoint(0F, 0F, 0F);
        HairBack4.setTextureSize(256, 128);
        HairBack4.mirror = true;
        setRotation(HairBack4, 0F, 0F, 0F);

        HairBack7 = new RendererModel(this, 0, 62);
        HairBack7.addBox(-4.5F, -8.5F, -0.5F, 9, 5, 2);
        HairBack7.setRotationPoint(0F, 0F, 0F);
        HairBack7.setTextureSize(256, 128);
        HairBack7.mirror = true;
        setRotation(HairBack7, 0F, 0F, 0F);

        HairBack8 = new RendererModel(this, 4, 48);
        HairBack8.addBox(0.5F, -0.5F, 2.6F, 4, 1, 2);
        HairBack8.setRotationPoint(0F, 0F, 0F);
        HairBack8.setTextureSize(256, 128);
        HairBack8.mirror = true;
        setRotation(HairBack8, 0F, 0F, 0F);

        HairBack9 = new RendererModel(this, 0, 39);
        HairBack9.addBox(-4.5F, -8.5F, 1.5F, 9, 6, 3);
        HairBack9.setRotationPoint(0F, 0F, 0F);
        HairBack9.setTextureSize(256, 128);
        HairBack9.mirror = true;
        setRotation(HairBack9, 0F, 0F, 0F);

        HairBack10 = new RendererModel(this, 0, 51);
        HairBack10.addBox(-2.5F, -2.5F, 1.5F, 7, 1, 3);
        HairBack10.setRotationPoint(0F, 0F, 0F);
        HairBack10.setTextureSize(256, 128);
        HairBack10.mirror = true;
        setRotation(HairBack10, 0F, 0F, 0F);

        HairBack11 = new RendererModel(this, 0, 55);
        HairBack11.addBox(-0.5F, -1.5F, 1.55F, 5, 1, 3);
        HairBack11.setRotationPoint(0F, 0F, 0F);
        HairBack11.setTextureSize(256, 128);
        HairBack11.mirror = true;
        setRotation(HairBack11, 0F, 0F, 0F);

        HairBack12 = new RendererModel(this, 16, 55);
        HairBack12.addBox(0.5F, -2.5F, 3.35F, 4, 2, 2);
        HairBack12.setRotationPoint(0F, 1F, 0F);
        HairBack12.setTextureSize(256, 128);
        HairBack12.mirror = true;
        setRotation(HairBack12, 0F, 0.4833219F, 0F);

        HairBack13 = new RendererModel(this, 0, 59);
        HairBack13.addBox(-1F, -2F, 4.8F, 4, 1, 2);
        HairBack13.setRotationPoint(0F, 1F, 0F);
        HairBack13.setTextureSize(256, 128);
        HairBack13.mirror = true;
        setRotation(HairBack13, 0F, 1.33843F, 0F);

        HairBack14 = new RendererModel(this, 12, 59);
        HairBack14.addBox(-1.1F, -2F, 5.1F, 2, 1, 1);
        HairBack14.setRotationPoint(0F, 1F, 0F);
        HairBack14.setTextureSize(256, 128);
        HairBack14.mirror = true;
        setRotation(HairBack14, 0F, 1.933288F, 0F);

        lens2 = new RendererModel(this, 0, 98);
        lens2.addBox(1.25F, -5F, -5F, 1, 1, 1);
        lens2.setRotationPoint(0F, 0F, 0F);
        lens2.setTextureSize(256, 128);
        lens2.mirror = true;
        setRotation(lens2, 0F, 0F, 0F);

        strap = new RendererModel(this, 0, 85);
        strap.addBox(-1.5F, -7.5F, -4.2F, 1, 2, 9);
        strap.setRotationPoint(0F, 0F, 0F);
        strap.setTextureSize(256, 128);
        strap.mirror = true;
        setRotation(strap, 0F, 0F, 0.7853982F);

        lens1 = new RendererModel(this, 0, 96);
        lens1.addBox(1F, -5F, -4.6F, 2, 1, 1);
        lens1.setRotationPoint(0F, 0F, 0F);
        lens1.setTextureSize(256, 128);
        lens1.mirror = true;
        setRotation(lens1, 0F, 0F, 0F);

        back = new RendererModel(this, 12, 96);
        back.addBox(0.5F, -5.5F, -4.4F, 3, 2, 1);
        back.setRotationPoint(0F, 0F, 0F);
        back.setTextureSize(256, 128);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);

        this.bipedRightArm.addChild(core);
        this.bipedRightArm.addChild(frontBeam);
        this.bipedRightArm.addChild(SideBeam);

        this.bipedBody.addChild(skirtBa);
        this.bipedBody.addChild(skirtF);
        this.bipedBody.addChild(skirtR);
        this.bipedBody.addChild(skirtBo1);
        this.bipedBody.addChild(skirtT1);
        this.bipedBody.addChild(skirtL);
        this.bipedBody.addChild(skirtBo2);

        this.bipedBody.addChild(chest);

        this.bipedHead.addChild(strap);
        this.bipedHead.addChild(back);

        this.bipedHead.addChild(lens1);
        this.bipedHead.addChild(lens2);
        this.bipedHead.addChild(HairBack1);
        this.bipedHead.addChild(HairBack2);
        this.bipedHead.addChild(HairBack3);
        this.bipedHead.addChild(HairBack4);
        this.bipedHead.addChild(HairBack5);
        this.bipedHead.addChild(HairBack6);
        this.bipedHead.addChild(HairBack7);
        this.bipedHead.addChild(HairBack8);
        this.bipedHead.addChild(HairBack9);
        this.bipedHead.addChild(HairBack10);
        this.bipedHead.addChild(HairBack11);
        this.bipedHead.addChild(HairBack12);
        this.bipedHead.addChild(HairBack13);
        this.bipedHead.addChild(HairBack14);
    }

    @Override
    public void render(
      @NotNull final LivingEntity entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
