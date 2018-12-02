package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCrafterFemale extends ModelBiped
{
    //fields
    ModelRenderer Core;
    ModelRenderer FrontBeam;
    ModelRenderer SideBeam;
    ModelRenderer skirtBa;
    ModelRenderer skirtF;
    ModelRenderer skirtR;
    ModelRenderer skirtBo1;
    ModelRenderer skirtT1;
    ModelRenderer skirtL;
    ModelRenderer skirtBo2;
    ModelRenderer head;
    ModelRenderer chest;
    ModelRenderer leftarm;
    ModelRenderer rightleg;
    ModelRenderer body;
    ModelRenderer leftleg;
    ModelRenderer rightarm;
    ModelRenderer HairBack1;
    ModelRenderer HairBack3;
    ModelRenderer HairBack2;
    ModelRenderer HairBack6;
    ModelRenderer HairBack5;
    ModelRenderer HairBack4;
    ModelRenderer HairBack7;
    ModelRenderer HairBack8;
    ModelRenderer HairBack9;
    ModelRenderer HairBack10;
    ModelRenderer HairBack11;
    ModelRenderer HairBack12;
    ModelRenderer HairBack13;
    ModelRenderer HairBack14;
    ModelRenderer Lens2;
    ModelRenderer Strap;
    ModelRenderer Lens1;
    ModelRenderer Back;

    public ModelEntityCrafterFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

        Core = new ModelRenderer(this, 38, 93);
        Core.addBox(-2F, 8.6F, -2F, 2, 2, 2);
        Core.setRotationPoint(-4.6F, 2F, 0F);
        Core.setTextureSize(256, 128);
        Core.mirror = true;
        setRotation(Core, 0F, 0.7853982F, 0F);

        FrontBeam = new ModelRenderer(this, 25, 78);
        FrontBeam.addBox(-1.5F, -2.49F, -2.5F, 1, 13, 5);
        FrontBeam.setRotationPoint(-5F, 2F, 0F);
        FrontBeam.setTextureSize(256, 128);
        FrontBeam.mirror = true;
        setRotation(FrontBeam, 0F, 0F, 0F);

        SideBeam = new ModelRenderer(this, 38, 78);
        SideBeam.addBox(-3.5F, -2.5F, -0.5F, 5, 13, 1);
        SideBeam.setRotationPoint(-5F, 2F, 0F);
        SideBeam.setTextureSize(256, 128);
        SideBeam.mirror = true;
        setRotation(SideBeam, 0F, 0F, 0F);

        skirtBa = new ModelRenderer(this, 47, 41);
        skirtBa.addBox(-4.5F, 11.1F, -4.5F, 9, 6, 2);
        skirtBa.setRotationPoint(0F, 0F, 0F);
        skirtBa.setTextureSize(256, 128);
        skirtBa.mirror = true;
        setRotation(skirtBa, 0.4712389F, 0F, 0F);

        skirtF = new ModelRenderer(this, 25, 41);
        skirtF.addBox(-4.5F, 11.1F, 2.5F, 9, 6, 2);
        skirtF.setRotationPoint(0F, 0F, 0F);
        skirtF.setTextureSize(256, 128);
        skirtF.mirror = true;
        setRotation(skirtF, -0.4712389F, 0F, 0F);

        skirtR = new ModelRenderer(this, 53, 49);
        skirtR.addBox(-17.9F, -2.2F, -3F, 6, 1, 6);
        skirtR.setRotationPoint(0F, 0F, 0F);
        skirtR.setTextureSize(256, 128);
        skirtR.mirror = true;
        setRotation(skirtR, 0F, 0F, -1.396263F);

        skirtBo1 = new ModelRenderer(this, 29, 56);
        skirtBo1.addBox(-4.5F, 15.4F, -4F, 9, 1, 8);
        skirtBo1.setRotationPoint(0F, 0F, 0F);
        skirtBo1.setTextureSize(256, 128);
        skirtBo1.mirror = true;
        setRotation(skirtBo1, 0F, 0F, 0F);
        
        skirtT1 = new ModelRenderer(this, 25, 33);
        skirtT1.addBox(-4.5F, 11F, -3F, 9, 2, 6);
        skirtT1.setRotationPoint(0F, 0F, 0F);
        skirtT1.setTextureSize(256, 128);
        skirtT1.mirror = true;
        setRotation(skirtT1, 0F, 0F, 0F);

        skirtL = new ModelRenderer(this, 29, 49);
        skirtL.addBox(11.9F, -2.2F, -3F, 6, 1, 6);
        skirtL.setRotationPoint(0F, 0F, 0F);
        skirtL.setTextureSize(256, 128);
        skirtL.mirror = true;
        setRotation(skirtL, 0F, 0F, 1.396263F);

        skirtBo2 = new ModelRenderer(this, 29, 65);
        skirtBo2.addBox(-4.5F, 16.4F, -5.5F, 9, 1, 11);
        skirtBo2.setRotationPoint(0F, 0F, 0F);
        skirtBo2.setTextureSize(256, 128);
        skirtBo2.mirror = true;
        setRotation(skirtBo2, 0F, 0F, 0F);

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setRotationPoint(0F, 0F, 0F);
        head.setTextureSize(256, 128);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 0, 32);
        chest.addBox(-3.5F, -0.5F, -5.5F, 7, 3, 3);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, 0.9341126F, 0F, 0F);

        leftarm = new ModelRenderer(this, 40, 16);
        leftarm.addBox(-1F, -2F, -2F, 4, 12, 4);
        leftarm.setRotationPoint(5F, 2F, 0F);
        leftarm.setTextureSize(256, 128);
        leftarm.mirror = true;
        setRotation(leftarm, 0F, 0F, 0F);

        rightleg = new ModelRenderer(this, 0, 16);
        rightleg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightleg.setRotationPoint(-2F, 12F, 0F);
        rightleg.setTextureSize(256, 128);
        rightleg.mirror = true;
        setRotation(rightleg, 0F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -2F, 8, 12, 4);
        body.setRotationPoint(0F, 0F, 0F);
        body.setTextureSize(256, 128);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);

        leftleg = new ModelRenderer(this, 0, 16);
        leftleg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftleg.setRotationPoint(2F, 12F, 0F);
        leftleg.setTextureSize(256, 128);
        leftleg.mirror = true;
        setRotation(leftleg, 0F, 0F, 0F);

        rightarm = new ModelRenderer(this, 40, 16);
        rightarm.addBox(-3F, -2F, -2F, 4, 12, 4);
        rightarm.setRotationPoint(-5F, 2F, 0F);
        rightarm.setTextureSize(256, 128);
        rightarm.mirror = true;
        setRotation(rightarm, 0F, 0F, 0F);

        HairBack1 = new ModelRenderer(this, 0, 80);
        HairBack1.addBox(-4.5F, -5.5F, -4.5F, 1, 1, 1);
        HairBack1.setRotationPoint(0F, 0F, 0F);
        HairBack1.setTextureSize(256, 128);
        HairBack1.mirror = true;
        setRotation(HairBack1, 0F, 0F, 0F);

        HairBack3 = new ModelRenderer(this, 0, 69);
        HairBack3.addBox(-4.5F, -8.5F, -4.5F, 9, 2, 4);
        HairBack3.setRotationPoint(0F, 0F, 0F);
        HairBack3.setTextureSize(256, 128);
        HairBack3.mirror = true;
        setRotation(HairBack3, 0F, 0F, 0F);

        HairBack2 = new ModelRenderer(this, 5, 80);
        HairBack2.addBox(3.5F, -7.1F, -0.55F, 1, 3, 1);
        HairBack2.setRotationPoint(0F, 0F, 0F);
        HairBack2.setTextureSize(256, 128);
        HairBack2.mirror = true;
        setRotation(HairBack2, 0.6108652F, 0F, 0F);

        HairBack6 = new ModelRenderer(this, 0, 48);
        HairBack6.addBox(-0.1F, -1F, 5.1F, 1, 2, 1);
        HairBack6.setRotationPoint(0F, 1F, 0F);
        HairBack6.setTextureSize(256, 128);
        HairBack6.mirror = true;
        setRotation(HairBack6, 0F, 1.933288F, 0F);

        HairBack5 = new ModelRenderer(this, 0, 75);
        HairBack5.addBox(2.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack5.setRotationPoint(0F, 0F, 0F);
        HairBack5.setTextureSize(256, 128);
        HairBack5.mirror = true;
        setRotation(HairBack5, 0F, 0F, 0F);

        HairBack4 = new ModelRenderer(this, 12, 75);
        HairBack4.addBox(-4.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack4.setRotationPoint(0F, 0F, 0F);
        HairBack4.setTextureSize(256, 128);
        HairBack4.mirror = true;
        setRotation(HairBack4, 0F, 0F, 0F);

        HairBack7 = new ModelRenderer(this, 0, 62);
        HairBack7.addBox(-4.5F, -8.5F, -0.5F, 9, 5, 2);
        HairBack7.setRotationPoint(0F, 0F, 0F);
        HairBack7.setTextureSize(256, 128);
        HairBack7.mirror = true;
        setRotation(HairBack7, 0F, 0F, 0F);

        HairBack8 = new ModelRenderer(this, 4, 48);
        HairBack8.addBox(0.5F, -0.5F, 2.6F, 4, 1, 2);
        HairBack8.setRotationPoint(0F, 0F, 0F);
        HairBack8.setTextureSize(256, 128);
        HairBack8.mirror = true;
        setRotation(HairBack8, 0F, 0F, 0F);

        HairBack9 = new ModelRenderer(this, 0, 39);
        HairBack9.addBox(-4.5F, -8.5F, 1.5F, 9, 6, 3);
        HairBack9.setRotationPoint(0F, 0F, 0F);
        HairBack9.setTextureSize(256, 128);
        HairBack9.mirror = true;
        setRotation(HairBack9, 0F, 0F, 0F);

        HairBack10 = new ModelRenderer(this, 0, 51);
        HairBack10.addBox(-2.5F, -2.5F, 1.5F, 7, 1, 3);
        HairBack10.setRotationPoint(0F, 0F, 0F);
        HairBack10.setTextureSize(256, 128);
        HairBack10.mirror = true;
        setRotation(HairBack10, 0F, 0F, 0F);

        HairBack11 = new ModelRenderer(this, 0, 55);
        HairBack11.addBox(-0.5F, -1.5F, 1.55F, 5, 1, 3);
        HairBack11.setRotationPoint(0F, 0F, 0F);
        HairBack11.setTextureSize(256, 128);
        HairBack11.mirror = true;
        setRotation(HairBack11, 0F, 0F, 0F);

        HairBack12 = new ModelRenderer(this, 16, 55);
        HairBack12.addBox(0.5F, -2.5F, 3.35F, 4, 2, 2);
        HairBack12.setRotationPoint(0F, 1F, 0F);
        HairBack12.setTextureSize(256, 128);
        HairBack12.mirror = true;
        setRotation(HairBack12, 0F, 0.4833219F, 0F);

        HairBack13 = new ModelRenderer(this, 0, 59);
        HairBack13.addBox(-1F, -2F, 4.8F, 4, 1, 2);
        HairBack13.setRotationPoint(0F, 1F, 0F);
        HairBack13.setTextureSize(256, 128);
        HairBack13.mirror = true;
        setRotation(HairBack13, 0F, 1.33843F, 0F);

        HairBack14 = new ModelRenderer(this, 12, 59);
        HairBack14.addBox(-1.1F, -2F, 5.1F, 2, 1, 1);
        HairBack14.setRotationPoint(0F, 1F, 0F);
        HairBack14.setTextureSize(256, 128);
        HairBack14.mirror = true;
        setRotation(HairBack14, 0F, 1.933288F, 0F);

        Lens2 = new ModelRenderer(this, 0, 98);
        Lens2.addBox(1.25F, -5F, -5F, 1, 1, 1);
        Lens2.setRotationPoint(0F, 0F, 0F);
        Lens2.setTextureSize(256, 128);
        Lens2.mirror = true;
        setRotation(Lens2, 0F, 0F, 0F);

        Strap = new ModelRenderer(this, 0, 85);
        Strap.addBox(-1.5F, -7.5F, -4.2F, 1, 2, 9);
        Strap.setRotationPoint(0F, 0F, 0F);
        Strap.setTextureSize(256, 128);
        Strap.mirror = true;
        setRotation(Strap, 0F, 0F, 0.7853982F);

        Lens1 = new ModelRenderer(this, 0, 96);
        Lens1.addBox(1F, -5F, -4.6F, 2, 1, 1);
        Lens1.setRotationPoint(0F, 0F, 0F);
        Lens1.setTextureSize(256, 128);
        Lens1.mirror = true;
        setRotation(Lens1, 0F, 0F, 0F);

        Back = new ModelRenderer(this, 12, 96);
        Back.addBox(0.5F, -5.5F, -4.4F, 3, 2, 1);
        Back.setRotationPoint(0F, 0F, 0F);
        Back.setTextureSize(256, 128);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);
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
        Core.render(scaleFactor);
        FrontBeam.render(scaleFactor);
        SideBeam.render(scaleFactor);
        skirtBa.render(scaleFactor);
        skirtF.render(scaleFactor);
        skirtR.render(scaleFactor);
        skirtBo1.render(scaleFactor);
        skirtT1.render(scaleFactor);
        skirtL.render(scaleFactor);
        skirtBo2.render(scaleFactor);
        head.render(scaleFactor);
        chest.render(scaleFactor);
        leftarm.render(scaleFactor);
        rightleg.render(scaleFactor);
        body.render(scaleFactor);
        leftleg.render(scaleFactor);
        rightarm.render(scaleFactor);
        HairBack1.render(scaleFactor);
        HairBack3.render(scaleFactor);
        HairBack2.render(scaleFactor);
        HairBack6.render(scaleFactor);
        HairBack5.render(scaleFactor);
        HairBack4.render(scaleFactor);
        HairBack7.render(scaleFactor);
        HairBack8.render(scaleFactor);
        HairBack9.render(scaleFactor);
        HairBack10.render(scaleFactor);
        HairBack11.render(scaleFactor);
        HairBack12.render(scaleFactor);
        HairBack13.render(scaleFactor);
        HairBack14.render(scaleFactor);
        Lens2.render(scaleFactor);
        Strap.render(scaleFactor);
        Lens1.render(scaleFactor);
        Back.render(scaleFactor);
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

        HairBack1.rotateAngleY = bipedHead.rotateAngleY;
        HairBack1.rotateAngleX = bipedHead.rotateAngleX;
        HairBack2.rotateAngleY = bipedHead.rotateAngleY;
        HairBack2.rotateAngleX = bipedHead.rotateAngleX;
        HairBack3.rotateAngleY = bipedHead.rotateAngleY;
        HairBack3.rotateAngleX = bipedHead.rotateAngleX;
        HairBack4.rotateAngleY = bipedHead.rotateAngleY;
        HairBack4.rotateAngleX = bipedHead.rotateAngleX;
        HairBack5.rotateAngleY = bipedHead.rotateAngleY;
        HairBack5.rotateAngleX = bipedHead.rotateAngleX;
        HairBack6.rotateAngleY = bipedHead.rotateAngleY;
        HairBack6.rotateAngleX = bipedHead.rotateAngleX;
        HairBack7.rotateAngleY = bipedHead.rotateAngleY;
        HairBack7.rotateAngleX = bipedHead.rotateAngleX;
        HairBack8.rotateAngleY = bipedHead.rotateAngleY;
        HairBack8.rotateAngleX = bipedHead.rotateAngleX;
        HairBack9.rotateAngleY = bipedHead.rotateAngleY;
        HairBack9.rotateAngleX = bipedHead.rotateAngleX;
        HairBack10.rotateAngleY = bipedHead.rotateAngleY;
        HairBack10.rotateAngleX = bipedHead.rotateAngleX;
        HairBack11.rotateAngleY = bipedHead.rotateAngleY;
        HairBack11.rotateAngleX = bipedHead.rotateAngleX;
        HairBack12.rotateAngleY = bipedHead.rotateAngleY;
        HairBack12.rotateAngleX = bipedHead.rotateAngleX;
        HairBack13.rotateAngleY = bipedHead.rotateAngleY;
        HairBack13.rotateAngleX = bipedHead.rotateAngleX;
        HairBack14.rotateAngleY = bipedHead.rotateAngleY;
        HairBack14.rotateAngleX = bipedHead.rotateAngleX;

        bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 2.0F * limbSwingAmount * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 0.73F * limbSwingAmount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 0.73F * limbSwingAmount;

        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }

}
