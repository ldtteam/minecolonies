package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelEntityMinerFemale extends ModelBiped
{
    //fields
    ModelRenderer GoggleL;
    ModelRenderer GoggleM;
    ModelRenderer GoggleR;
    ModelRenderer GoggleRM;
    ModelRenderer GoggleLM;
    ModelRenderer RightArm;
    ModelRenderer LeftArm;
    ModelRenderer Chest;
    ModelRenderer RightLeg;
    ModelRenderer LeftLeg;
    ModelRenderer Body;
    ModelRenderer Head;
    ModelRenderer HelmetLi;
    ModelRenderer HelmetB;
    ModelRenderer HelmetFM;
    ModelRenderer HelmetT;
    ModelRenderer HelmetRLM;
    ModelRenderer Bag;
    ModelRenderer Belt;
    ModelRenderer Torch;
    ModelRenderer Lock;
    ModelRenderer Buckle;
    ModelRenderer PonytailB;
    ModelRenderer PonytailT;

    public ModelEntityMinerFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        GoggleL = new ModelRenderer(this, 20, 39);
        GoggleL.addBox(1F, -5F, -4.5F, 2, 2, 1);
        GoggleL.setRotationPoint(0F, 0F, 0F);
        GoggleL.setTextureSize(128, 64);
        GoggleL.mirror = true;
        setRotation(GoggleL, 0F, 0F, 0F);

        GoggleM = new ModelRenderer(this, 0, 33);
        GoggleM.addBox(-4.5F, -4.9F, -4.35F, 9, 1, 4);
        GoggleM.setRotationPoint(0F, 0F, 0F);
        GoggleM.setTextureSize(128, 64);
        GoggleM.mirror = true;
        setRotation(GoggleM, 0F, 0F, 0F);

        GoggleR = new ModelRenderer(this, 0, 39);
        GoggleR.addBox(-3F, -5F, -4.5F, 2, 2, 1);
        GoggleR.setRotationPoint(0F, 0F, 0F);
        GoggleR.setTextureSize(128, 64);
        GoggleR.mirror = true;
        setRotation(GoggleR, 0F, 0F, 0F);

        GoggleRM = new ModelRenderer(this, 7, 39);
        GoggleRM.addBox(-2.3F, -4.5F, -4.6F, 1, 1, 1);
        GoggleRM.setRotationPoint(0F, 0F, 0F);
        GoggleRM.setTextureSize(128, 64);
        GoggleRM.mirror = true;
        setRotation(GoggleRM, 0F, 0F, 0F);

        GoggleLM = new ModelRenderer(this, 15, 39);
        GoggleLM.addBox(1.3F, -4.5F, -4.6F, 1, 1, 1);
        GoggleLM.setRotationPoint(0F, 0F, 0F);
        GoggleLM.setTextureSize(128, 64);
        GoggleLM.mirror = true;
        setRotation(GoggleLM, 0F, 0F, 0F);

        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        RightArm.setRotationPoint(-5F, 2F, 0F);
        RightArm.setTextureSize(128, 64);
        RightArm.mirror = true;
        setRotation(RightArm, 0F, 0F, 0F);

        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        LeftArm.setRotationPoint(5F, 2F, 0F);
        LeftArm.setTextureSize(128, 64);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, 0F);
        LeftArm.mirror = false;

        Chest = new ModelRenderer(this, 0, 55);
        Chest.addBox(-3.5F, 2.7F, -0.6F, 7, 3, 4);
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

        LeftLeg = new ModelRenderer(this, 0, 16);
        LeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        LeftLeg.setRotationPoint(2F, 12F, 0F);
        LeftLeg.setTextureSize(128, 64);
        LeftLeg.mirror = true;
        setRotation(LeftLeg, 0F, 0F, 0F);
        LeftLeg.mirror = false;

        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 0F, -2F, 8, 12, 4);
        Body.setRotationPoint(0F, 0F, 0F);
        Body.setTextureSize(128, 64);
        Body.mirror = true;
        setRotation(Body, 0F, 0F, 0F);

        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -7F, -4F, 8, 7, 8);
        Head.setRotationPoint(0F, 0F, 0F);
        Head.setTextureSize(128, 64);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);

        HelmetLi = new ModelRenderer(this, 23, 54);
        HelmetLi.addBox(-1F, -8F, -6F, 2, 2, 2);
        HelmetLi.setRotationPoint(0F, 0F, 0F);
        HelmetLi.setTextureSize(128, 64);
        HelmetLi.mirror = true;
        setRotation(HelmetLi, -0.122173F, 0F, 0F);

        HelmetB = new ModelRenderer(this, 32, 49);
        HelmetB.addBox(-4.5F, -7.5F, -5.5F, 9, 2, 9);
        HelmetB.setRotationPoint(0F, 0F, 0F);
        HelmetB.setTextureSize(128, 64);
        HelmetB.mirror = true;
        setRotation(HelmetB, -0.122173F, 0F, 0F);

        HelmetFM = new ModelRenderer(this, 1, 43);
        HelmetFM.addBox(-3F, -8.5F, -5.5F, 6, 1, 9);
        HelmetFM.setRotationPoint(0F, 0F, 0F);
        HelmetFM.setTextureSize(128, 64);
        HelmetFM.mirror = true;
        setRotation(HelmetFM, -0.122173F, 0F, 0F);

        HelmetT = new ModelRenderer(this, 32, 33);
        HelmetT.addBox(-3F, -9.5F, -3.5F, 6, 1, 6);
        HelmetT.setRotationPoint(0F, 0F, 0F);
        HelmetT.setTextureSize(128, 64);
        HelmetT.mirror = true;
        setRotation(HelmetT, -0.122173F, 0F, 0F);

        HelmetRLM = new ModelRenderer(this, 32, 41);
        HelmetRLM.addBox(-4.5F, -8.5F, -3.5F, 9, 1, 6);
        HelmetRLM.setRotationPoint(0F, 0F, 0F);
        HelmetRLM.setTextureSize(128, 64);
        HelmetRLM.mirror = true;
        setRotation(HelmetRLM, -0.122173F, 0F, 0F);

        Bag = new ModelRenderer(this, 57, 16);
        Bag.addBox(-3.5F, 7.5F, 1.4F, 4, 4, 1);
        Bag.setRotationPoint(0F, 0F, 0F);
        Bag.setTextureSize(128, 64);
        Bag.mirror = true;
        setRotation(Bag, 0F, 0F, 0F);

        Belt = new ModelRenderer(this, 57, 22);
        Belt.addBox(-4.5F, 9.5F, -2.5F, 9, 1, 5);
        Belt.setRotationPoint(0F, 0F, 0F);
        Belt.setTextureSize(128, 64);
        Belt.mirror = true;
        setRotation(Belt, 0F, 0F, 0F);

        Torch = new ModelRenderer(this, 57, 29);
        Torch.addBox(-3.5F, 7.4F, -2.4F, 1, 4, 1);
        Torch.setRotationPoint(0F, 0F, 0F);
        Torch.setTextureSize(128, 64);
        Torch.mirror = true;
        setRotation(Torch, 0F, 0F, 0F);

        Lock = new ModelRenderer(this, 68, 16);
        Lock.addBox(-2.5F, 8F, 1.5F, 2, 1, 1);
        Lock.setRotationPoint(0F, 0F, 0F);
        Lock.setTextureSize(128, 64);
        Lock.mirror = true;
        setRotation(Lock, 0F, 0F, 0F);

        Buckle = new ModelRenderer(this, 68, 29);
        Buckle.addBox(-1F, 9F, -2.7F, 2, 2, 1);
        Buckle.setRotationPoint(0F, 0F, 0F);
        Buckle.setTextureSize(128, 64);
        Buckle.mirror = true;
        setRotation(Buckle, 0F, 0F, 0F);

        PonytailB = new ModelRenderer(this, 80, 40);
        PonytailB.addBox(-0.5F, 2.4F, 3.7F, 1, 5, 1);
        PonytailB.setRotationPoint(0F, 0F, 0F);
        PonytailB.setTextureSize(128, 64);
        PonytailB.mirror = true;
        setRotation(PonytailB, 0.1047198F, 0F, 0F);

        PonytailT = new ModelRenderer(this, 79, 33);
        PonytailT.addBox(-1F, -2F, 3.4F, 2, 5, 1);
        PonytailT.setRotationPoint(0F, 0F, 0F);
        PonytailT.setTextureSize(128, 64);
        PonytailT.mirror = true;
        setRotation(PonytailT, 0.2268928F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        GoggleL.render(f5);
        GoggleM.render(f5);
        GoggleR.render(f5);
        GoggleRM.render(f5);
        GoggleLM.render(f5);
        RightArm.render(f5);
        LeftArm.render(f5);
        Chest.render(f5);
        RightLeg.render(f5);
        LeftLeg.render(f5);
        Body.render(f5);
        Head.render(f5);
        HelmetLi.render(f5);
        HelmetB.render(f5);
        HelmetFM.render(f5);
        HelmetT.render(f5);
        HelmetRLM.render(f5);
        Bag.render(f5);
        Belt.render(f5);
        Torch.render(f5);
        Lock.render(f5);
        Buckle.render(f5);
        PonytailB.render(f5);
        PonytailT.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

}
