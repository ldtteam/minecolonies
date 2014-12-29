package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelEntityLumberjackFemale extends ModelBiped
{
    ModelRenderer LeftArm;
    ModelRenderer Chest;
    ModelRenderer RightLeg;
    ModelRenderer LeftLeg;
    ModelRenderer Head;
    ModelRenderer Body;
    ModelRenderer PonytailB;
    ModelRenderer PonytailT;
    ModelRenderer BasketBL;
    ModelRenderer BasketTB;
    ModelRenderer BasketBR;
    ModelRenderer BasketTML;
    ModelRenderer BasketBF;
    ModelRenderer BasketMFR;
    ModelRenderer BasketMFL;
    ModelRenderer BasketMBL;
    ModelRenderer BasketMBR;
    ModelRenderer BasketTMR;
    ModelRenderer BasketBB;
    ModelRenderer LogT;
    ModelRenderer LogM;
    ModelRenderer LogB;

    public ModelEntityLumberjackFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);
        LeftArm.mirror = true;
        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        LeftArm.setRotationPoint(5F, 2F, 0F);
        LeftArm.setTextureSize(128, 64);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, 0F);
        LeftArm.mirror = false;
        Chest = new ModelRenderer(this, 17, 33);
        Chest.addBox(-3.5F, 1.7F, -1F, 7, 4, 4);
        Chest.setRotationPoint(0F, 0F, 0F);
        Chest.setTextureSize(128, 64);
        Chest.mirror = true;
        setRotation(Chest, -0.5934119F, 0F, 0F);
        RightLeg = new ModelRenderer(this, 0, 16);
        RightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        RightLeg.setRotationPoint(-2F, 12F, 0F);
        RightLeg.setTextureSize(128, 64);
        RightLeg.mirror = true;
        setRotation(RightLeg, 0F, 0F, 0F);
        LeftLeg.mirror = true;
        LeftLeg = new ModelRenderer(this, 0, 16);
        LeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        LeftLeg.setRotationPoint(2F, 12F, 0F);
        LeftLeg.setTextureSize(128, 64);
        LeftLeg.mirror = true;
        setRotation(LeftLeg, 0F, 0F, 0F);
        LeftLeg.mirror = false;
        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -8F, -4F, 8, 8, 8);
        Head.setRotationPoint(0F, 0F, 0F);
        Head.setTextureSize(128, 64);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);
        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 0F, -2F, 8, 12, 4);
        Body.setRotationPoint(0F, 0F, 0F);
        Body.setTextureSize(128, 64);
        Body.mirror = true;
        setRotation(Body, 0F, 0F, 0F);
        PonytailB = new ModelRenderer(this, 33, 6);
        PonytailB.addBox(-0.5F, 3.2F, 3.8F, 1, 5, 1);
        PonytailB.setRotationPoint(0F, 0F, 0F);
        PonytailB.setTextureSize(128, 64);
        PonytailB.mirror = true;
        setRotation(PonytailB, 0.5585054F, 0F, 0F);
        PonytailT = new ModelRenderer(this, 32, 0);
        PonytailT.addBox(-1F, -2F, 4.2F, 2, 5, 1);
        PonytailT.setRotationPoint(0F, 0F, 0F);
        PonytailT.setTextureSize(128, 64);
        PonytailT.mirror = true;
        setRotation(PonytailT, 0.418879F, 0F, 0F);
        BasketBL = new ModelRenderer(this, 0, 33);
        BasketBL.addBox(2F, 11F, 3F, 1, 1, 3);
        BasketBL.setRotationPoint(0F, 0F, 0F);
        BasketBL.setTextureSize(128, 64);
        BasketBL.mirror = true;
        setRotation(BasketBL, 0F, 0F, 0F);
        BasketTB = new ModelRenderer(this, 0, 38);
        BasketTB.addBox(-2F, 4F, 6F, 4, 1, 1);
        BasketTB.setRotationPoint(0F, 0F, 0F);
        BasketTB.setTextureSize(128, 64);
        BasketTB.mirror = true;
        setRotation(BasketTB, 0F, 0F, 0F);
        BasketBR = new ModelRenderer(this, 0, 33);
        BasketBR.addBox(-3F, 11F, 3F, 1, 1, 3);
        BasketBR.setRotationPoint(0F, 0F, 0F);
        BasketBR.setTextureSize(128, 64);
        BasketBR.mirror = true;
        setRotation(BasketBR, 0F, 0F, 0F);
        BasketTML = new ModelRenderer(this, 11, 33);
        BasketTML.addBox(3.1F, 1.4F, 0.6F, 1, 6, 1);
        BasketTML.setRotationPoint(0F, 0F, 0F);
        BasketTML.setTextureSize(128, 64);
        BasketTML.mirror = true;
        setRotation(BasketTML, 0.8080874F, -0.1745329F, 0F);
        BasketBF = new ModelRenderer(this, 0, 38);
        BasketBF.addBox(-2F, 11F, 2F, 4, 1, 1);
        BasketBF.setRotationPoint(0F, 0F, 0F);
        BasketBF.setTextureSize(128, 64);
        BasketBF.mirror = true;
        setRotation(BasketBF, 0F, 0F, 0F);
        BasketMFR = new ModelRenderer(this, 11, 41);
        BasketMFR.addBox(-3F, 0F, 2F, 1, 12, 1);
        BasketMFR.setRotationPoint(0F, 0F, 0F);
        BasketMFR.setTextureSize(128, 64);
        BasketMFR.mirror = true;
        setRotation(BasketMFR, 0F, 0F, 0F);
        BasketMFL = new ModelRenderer(this, 11, 41);
        BasketMFL.addBox(2F, 0F, 2F, 1, 12, 1);
        BasketMFL.setRotationPoint(0F, 0F, 0F);
        BasketMFL.setTextureSize(128, 64);
        BasketMFL.mirror = true;
        setRotation(BasketMFL, 0F, 0F, 0F);
        BasketMBL = new ModelRenderer(this, 6, 41);
        BasketMBL.addBox(2F, 4F, 6F, 1, 8, 1);
        BasketMBL.setRotationPoint(0F, 0F, 0F);
        BasketMBL.setTextureSize(128, 64);
        BasketMBL.mirror = true;
        setRotation(BasketMBL, 0F, 0F, 0F);
        BasketMBR = new ModelRenderer(this, 6, 41);
        BasketMBR.addBox(-3F, 4F, 6F, 1, 8, 1);
        BasketMBR.setRotationPoint(0F, 0F, 0F);
        BasketMBR.setTextureSize(128, 64);
        BasketMBR.mirror = true;
        setRotation(BasketMBR, 0F, 0F, 0F);
        BasketTMR = new ModelRenderer(this, 11, 33);
        BasketTMR.addBox(-4.1F, 1.4F, 0.5F, 1, 6, 1);
        BasketTMR.setRotationPoint(0F, 0F, 0F);
        BasketTMR.setTextureSize(128, 64);
        BasketTMR.mirror = true;
        setRotation(BasketTMR, 0.8080874F, 0.1745329F, 0F);
        BasketBB = new ModelRenderer(this, 0, 38);
        BasketBB.addBox(-2F, 11F, 6F, 4, 1, 1);
        BasketBB.setRotationPoint(0F, 0F, 0F);
        BasketBB.setTextureSize(128, 64);
        BasketBB.mirror = true;
        setRotation(BasketBB, 0F, 0F, 0F);
        LogT = new ModelRenderer(this, 17, 41);
        LogT.addBox(-4.2F, 2F, 0.7F, 3, 7, 3);
        LogT.setRotationPoint(0F, 0F, 0F);
        LogT.setTextureSize(128, 64);
        LogT.mirror = true;
        setRotation(LogT, 0F, 0.7853982F, 0.2094395F);
        LogM = new ModelRenderer(this, 17, 51);
        LogM.addBox(-1.3F, 6.7F, -1F, 5, 3, 3);
        LogM.setRotationPoint(0F, 0F, 0F);
        LogM.setTextureSize(128, 64);
        LogM.mirror = true;
        setRotation(LogM, 0.6457718F, 0.296706F, 0F);
        LogB = new ModelRenderer(this, 17, 58);
        LogB.addBox(-5.3F, 8.5F, 2.5F, 10, 3, 3);
        LogB.setRotationPoint(0F, 0F, 0F);
        LogB.setTextureSize(128, 64);
        LogB.mirror = true;
        setRotation(LogB, 0.0698132F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        bipedRightArm.render(f5);
        LeftArm.render(f5);
        Chest.render(f5);
        RightLeg.render(f5);
        LeftLeg.render(f5);
        Head.render(f5);
        Body.render(f5);
        PonytailB.render(f5);
        PonytailT.render(f5);
        BasketBL.render(f5);
        BasketTB.render(f5);
        BasketBR.render(f5);
        BasketTML.render(f5);
        BasketBF.render(f5);
        BasketMFR.render(f5);
        BasketMFL.render(f5);
        BasketMBL.render(f5);
        BasketMBR.render(f5);
        BasketTMR.render(f5);
        BasketBB.render(f5);
        LogT.render(f5);
        LogM.render(f5);
        LogB.render(f5);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)//used for animations
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
