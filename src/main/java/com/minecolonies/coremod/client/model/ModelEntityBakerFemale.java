package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Class used for the baker entity model.
 */
public class ModelEntityBakerFemale extends ModelBiped
{
    //fields
    ModelRenderer RightArm;
    ModelRenderer LeftArm;
    ModelRenderer Chest;
    ModelRenderer RightLeg;
    ModelRenderer LeftLeg;
    ModelRenderer Body;
    ModelRenderer Head;
    ModelRenderer PonytailB;
    ModelRenderer PonytailT;
    ModelRenderer lipR;
    ModelRenderer baseT;
    ModelRenderer lipT;
    ModelRenderer lipL;
    ModelRenderer lipB;
    ModelRenderer baseB;
    ModelRenderer baseM;
    ModelRenderer topL;
    ModelRenderer topF;
    ModelRenderer botL;
    ModelRenderer topR;
    ModelRenderer midR;
    ModelRenderer midL;

    public ModelEntityBakerFemale()
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

        Chest = new ModelRenderer(this, 17, 32);
        Chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        Chest.setRotationPoint(0F, 0F, 0F);
        Chest.setTextureSize(256, 128);
        Chest.mirror = true;
        setRotation(Chest, -0.5934119F, 0F, 0F);

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

        PonytailB = new ModelRenderer(this, 33, 6);
        PonytailB.addBox(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        PonytailB.setRotationPoint(0F, 0F, 0F);
        PonytailB.setTextureSize(256, 128);
        PonytailB.mirror = true;
        setRotation(PonytailB, 0.1047198F, 0F, 0F);

        PonytailT = new ModelRenderer(this, 32, 0);
        PonytailT.addBox(-1F, -2.2F, 3.5F, 2, 5, 1);
        PonytailT.setRotationPoint(0F, 0F, 0F);
        PonytailT.setTextureSize(256, 128);
        PonytailT.mirror = true;
        setRotation(PonytailT, 0.2268928F, 0F, 0F);

        lipR = new ModelRenderer(this, 22, 70);
        lipR.addBox(2F, -6.2F, -7.5F, 2, 1, 9);
        lipR.setRotationPoint(0F, 0F, 0F);
        lipR.setTextureSize(256, 128);
        lipR.mirror = true;
        setRotation(lipR, -1.396263F, -1.343904F, -0.6632251F);

        baseT = new ModelRenderer(this, 0, 40);
        baseT.addBox(-4.5F, -8.2F, -6.5F, 9, 1, 6);
        baseT.setRotationPoint(0F, 0F, 0F);
        baseT.setTextureSize(256, 128);
        baseT.mirror = true;
        setRotation(baseT, -0.8922867F, 0F, 0F);

        lipT = new ModelRenderer(this, 0, 67);
        lipT.addBox(-5F, -9.2F, -1F, 10, 1, 2);
        lipT.setRotationPoint(0F, 0F, 0F);
        lipT.setTextureSize(256, 128);
        lipT.mirror = true;
        setRotation(lipT, 0.2230717F, 0F, 0F);

        lipL = new ModelRenderer(this, 0, 70);
        lipL.addBox(-4F, -6.2F, -7.5F, 2, 1, 9);
        lipL.setRotationPoint(0F, 0F, 0F);
        lipL.setTextureSize(256, 128);
        lipL.mirror = true;
        setRotation(lipL, -1.396263F, 1.343904F, 0.6632251F);

        lipB = new ModelRenderer(this, 0, 80);
        lipB.addBox(-5F, -5.1F, -1.5F, 10, 1, 2);
        lipB.setRotationPoint(0F, 0F, 0F);
        lipB.setTextureSize(256, 128);
        lipB.mirror = true;
        setRotation(lipB, -1.375609F, 0F, 0F);

        baseB = new ModelRenderer(this, 0, 57);
        baseB.addBox(-5F, -5.2F, -8F, 10, 1, 9);
        baseB.setRotationPoint(0F, 0F, 0F);
        baseB.setTextureSize(256, 128);
        baseB.mirror = true;
        setRotation(baseB, -0.8922867F, 0F, 0F);

        baseM = new ModelRenderer(this, 0, 47);
        baseM.addBox(-4.5F, -7.2F, -7.5F, 9, 2, 8);
        baseM.setRotationPoint(0F, 0F, 0F);
        baseM.setTextureSize(256, 128);
        baseM.mirror = true;
        setRotation(baseM, -0.8922867F, 0F, 0F);

        topL = new ModelRenderer(this, 57, 4);
        topL.addBox(2.5F, -7.5F, -4.5F, 2, 1, 5);
        topL.setRotationPoint(0F, 0F, 0F);
        topL.setTextureSize(256, 128);
        topL.mirror = true;
        setRotation(topL, 0F, 0F, 0F);

        topF = new ModelRenderer(this, 57, 0);
        topF.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 3);
        topF.setRotationPoint(0F, 0F, 0F);
        topF.setTextureSize(256, 128);
        topF.mirror = true;
        setRotation(topF, 0F, 0F, 0F);

        botL = new ModelRenderer(this, 57, 14);
        botL.addBox(1.5F, -5.5F, -1.5F, 3, 2, 1);
        botL.setRotationPoint(0F, 0F, 0F);
        botL.setTextureSize(256, 128);
        botL.mirror = true;
        setRotation(botL, 0F, 0F, 0F);

        topR = new ModelRenderer(this, 71, 4);
        topR.addBox(-4.5F, -7.5F, -4.5F, 3, 1, 5);
        topR.setRotationPoint(0F, 0F, 0F);
        topR.setTextureSize(256, 128);
        topR.mirror = true;
        setRotation(topR, 0F, 0F, 0F);

        midR = new ModelRenderer(this, 69, 10);
        midR.addBox(-4.5F, -6.5F, -2.5F, 3, 1, 3);
        midR.setRotationPoint(0F, 0F, 0F);
        midR.setTextureSize(256, 128);
        midR.mirror = true;
        setRotation(midR, 0F, 0F, 0F);

        midL = new ModelRenderer(this, 57, 10);
        midL.addBox(1.5F, -6.5F, -2.5F, 3, 1, 3);
        midL.setRotationPoint(0F, 0F, 0F);
        midL.setTextureSize(256, 128);
        midL.mirror = true;
        setRotation(midL, 0F, 0F, 0F);
    }

    public void render(final Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        RightArm.render(f5);
        LeftArm.render(f5);
        Chest.render(f5);
        RightLeg.render(f5);
        LeftLeg.render(f5);
        Body.render(f5);
        Head.render(f5);
        PonytailB.render(f5);
        PonytailT.render(f5);
        lipR.render(f5);
        baseT.render(f5);
        lipT.render(f5);
        lipL.render(f5);
        lipB.render(f5);
        baseB.render(f5);
        baseM.render(f5);
        topL.render(f5);
        topF.render(f5);
        botL.render(f5);
        topR.render(f5);
        midR.render(f5);
        midL.render(f5);
    }

    private void setRotation(final ModelRenderer model, float x, float y, float z)
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
        final float bodyX = bipedBody.rotateAngleX;
        final float headX = bipedHead.rotateAngleX;

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        bipedBody.rotateAngleX = bodyX;
        bipedHead.rotateAngleX = headX;
    }
}
