package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelEntityFishermanMale extends ModelBiped
{
    //fields
    private ModelRenderer Fish3;
    private ModelRenderer HookTie3;
    private ModelRenderer String;
    private ModelRenderer HookTie1;
    private ModelRenderer HookTie2;
    private ModelRenderer Fish1;
    private ModelRenderer Fish2;
    private ModelRenderer Reel;
    private ModelRenderer Line;
    private ModelRenderer Pole;
    private ModelRenderer RightArm;
    private ModelRenderer LeftArm;
    private ModelRenderer RightLeg;
    private ModelRenderer LeftLeg;
    private ModelRenderer Head;
    private ModelRenderer Body;
    private ModelRenderer hatPiece1;
    private ModelRenderer hatPiece2;
    private ModelRenderer hatPiece3;
    private ModelRenderer hatPiece4;
    private ModelRenderer hatPiece5;
    private ModelRenderer hatPiece6;
    private ModelRenderer hatPiece7;
    private ModelRenderer RightBoot;
    private ModelRenderer LeftBoot;

    public ModelEntityFishermanMale()
    {
        textureWidth = 256;
        textureHeight = 128;

        Fish3 = new ModelRenderer(this, 61, 46);
        Fish3.addBox(0.4F, 10F, -2.2F, 2, 4, 0);
        Fish3.setRotationPoint(0F, 0F, 0F);
        Fish3.setTextureSize(256, 128);
        Fish3.mirror = true;
        setRotation(Fish3, 0F, 0F, 0F);

        HookTie3 = new ModelRenderer(this, 58, 46);
        HookTie3.addBox(0.5F, 8F, -2.2F, 1, 2, 0);
        HookTie3.setRotationPoint(0F, 0F, 0F);
        HookTie3.setTextureSize(256, 128);
        HookTie3.mirror = true;
        setRotation(HookTie3, 0F, 0F, 0F);

        String = new ModelRenderer(this, 53, 38);
        String.addBox(-5F, -0.5F, -2.3F, 1, 12, 1);
        String.setRotationPoint(0F, 0F, 0F);
        String.setTextureSize(256, 128);
        String.mirror = true;
        setRotation(String, 0F, 0F, -0.7435722F);

        HookTie1 = new ModelRenderer(this, 58, 38);
        HookTie1.addBox(-3.5F, 3.5F, -2.2F, 1, 2, 0);
        HookTie1.setRotationPoint(0F, 0F, 0F);
        HookTie1.setTextureSize(256, 128);
        HookTie1.mirror = true;
        setRotation(HookTie1, 0F, 0F, 0F);

        HookTie2 = new ModelRenderer(this, 58, 42);
        HookTie2.addBox(-1.5F, 5.5F, -2.2F, 1, 2, 0);
        HookTie2.setRotationPoint(0F, 0F, 0F);
        HookTie2.setTextureSize(256, 128);
        HookTie2.mirror = true;
        setRotation(HookTie2, 0F, 0F, 0F);

        Fish1 = new ModelRenderer(this, 61, 38);
        Fish1.addBox(-4.4F, 5.5F, -2.2F, 2, 4, 0);
        Fish1.setRotationPoint(0F, 0F, 0F);
        Fish1.setTextureSize(256, 128);
        Fish1.mirror = true;
        setRotation(Fish1, 0F, 0F, 0F);

        Fish2 = new ModelRenderer(this, 61, 42);
        Fish2.addBox(-2F, 7.5F, -2.2F, 2, 4, 0);
        Fish2.setRotationPoint(0F, 0F, 0F);
        Fish2.setTextureSize(256, 128);
        Fish2.mirror = true;
        setRotation(Fish2, 0F, 0F, 0F);

        Reel = new ModelRenderer(this, 62, 64);
        Reel.addBox(-6F, 6F, 2F, 2, 2, 1);
        Reel.setRotationPoint(0F, 0F, 0F);
        Reel.setTextureSize(256, 128);
        Reel.mirror = true;
        setRotation(Reel, 0F, 0F, -0.7435722F);

        Line = new ModelRenderer(this, 62, 52);
        Line.addBox(-4.5F, -4.75F, 2.5F, 1, 11, 0);
        Line.setRotationPoint(0F, 0F, 0F);
        Line.setTextureSize(256, 128);
        Line.mirror = true;
        setRotation(Line, 0F, 0F, -0.7435722F);

        Pole = new ModelRenderer(this, 57, 52);
        Pole.addBox(-4F, -5F, 2F, 1, 16, 1);
        Pole.setRotationPoint(0F, 0F, 0F);
        Pole.setTextureSize(256, 128);
        Pole.mirror = true;
        setRotation(Pole, 0F, 0F, -0.7435722F);

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
        LeftArm.mirror = false;

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
        LeftLeg.mirror = false;

        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -8F, -4F, 8, 8, 8);
        Head.setRotationPoint(0F, 0F, 0F);
        Head.setTextureSize(256, 128);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);

        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 0F, -2F, 8, 12, 4);
        Body.setRotationPoint(0F, 0F, 0F);
        Body.setTextureSize(256, 128);
        Body.mirror = true;
        setRotation(Body, 0F, 0F, 0F);

        hatPiece1 = new ModelRenderer(this, 24, 45);
        hatPiece1.addBox(-5F, -8.6F, 3.2F, 10, 1, 2);
        hatPiece1.setRotationPoint(0F, 0F, 0F);
        hatPiece1.setTextureSize(256, 128);
        hatPiece1.mirror = true;
        setRotation(hatPiece1, -0.2230717F, 0F, 0F);

        hatPiece2 = new ModelRenderer(this, 0, 48);
        hatPiece2.addBox(3.7F, -8.65F, -5.5F, 2, 1, 10);
        hatPiece2.setRotationPoint(0F, 0F, 0F);
        hatPiece2.setTextureSize(256, 128);
        hatPiece2.mirror = true;
        setRotation(hatPiece2, -0.074351F, 0F, 0.1487195F);

        hatPiece3 = new ModelRenderer(this, 0, 45);
        hatPiece3.addBox(-5F, -8.7F, -6.2F, 10, 1, 2);
        hatPiece3.setRotationPoint(0F, 0F, 0F);
        hatPiece3.setTextureSize(256, 128);
        hatPiece3.mirror = true;
        setRotation(hatPiece3, 0.0743572F, 0F, 0F);

        hatPiece4 = new ModelRenderer(this, 0, 69);
        hatPiece4.addBox(-3F, -13F, -3.5F, 6, 1, 6);
        hatPiece4.setRotationPoint(0F, 1F, 0F);
        hatPiece4.setTextureSize(256, 128);
        hatPiece4.mirror = true;
        setRotation(hatPiece4, -0.0743572F, 0F, 0F);

        hatPiece5 = new ModelRenderer(this, 24, 48);
        hatPiece5.addBox(-5.7F, -8.65F, -5.5F, 2, 1, 10);
        hatPiece5.setRotationPoint(0F, 0F, 0F);
        hatPiece5.setTextureSize(256, 128);
        hatPiece5.mirror = true;
        setRotation(hatPiece5, -0.074351F, 0F, -0.1487144F);

        hatPiece6 = new ModelRenderer(this, 0, 33);
        hatPiece6.addBox(-5F, -9F, -5.5F, 10, 2, 10);
        hatPiece6.setRotationPoint(0F, 0F, 0F);
        hatPiece6.setTextureSize(256, 128);
        hatPiece6.mirror = true;
        setRotation(hatPiece6, -0.0743572F, 0F, 0F);

        hatPiece7 = new ModelRenderer(this, 0, 59);
        hatPiece7.addBox(-4F, -11F, -4.5F, 8, 2, 8);
        hatPiece7.setRotationPoint(0F, 0F, 0F);
        hatPiece7.setTextureSize(256, 128);
        hatPiece7.mirror = true;
        setRotation(hatPiece7, -0.0743572F, 0F, 0F);

        RightBoot = new ModelRenderer(this, 20, 102);
        RightBoot.addBox(-2.7F, 4F, -2.5F, 5, 2, 5);
        RightBoot.setRotationPoint(-2F, 12F, 0F);
        RightBoot.setTextureSize(256, 128);
        RightBoot.mirror = true;
        setRotation(RightBoot, 0F, 0F, 0F);

        LeftBoot = new ModelRenderer(this, 0, 102);
        LeftBoot.addBox(-2.3F, 4F, -2.49F, 5, 2, 5);
        LeftBoot.setRotationPoint(2F, 12F, 0F);
        LeftBoot.setTextureSize(256, 128);
        LeftBoot.mirror = true;
        setRotation(LeftBoot, 0F, 0F, 0F);
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,entity);
        Fish3.render(scaleFactor);
        HookTie3.render(scaleFactor);
        String.render(scaleFactor);
        HookTie1.render(scaleFactor);
        HookTie2.render(scaleFactor);
        Fish1.render(scaleFactor);
        Fish2.render(scaleFactor);
        Reel.render(scaleFactor);
        Line.render(scaleFactor);
        Pole.render(scaleFactor);
        RightArm.render(scaleFactor);
        LeftArm.render(scaleFactor);
        RightLeg.render(scaleFactor);
        LeftLeg.render(scaleFactor);
        Head.render(scaleFactor);
        Body.render(scaleFactor);
        hatPiece1.render(scaleFactor);
        hatPiece2.render(scaleFactor);
        hatPiece3.render(scaleFactor);
        hatPiece4.render(scaleFactor);
        hatPiece5.render(scaleFactor);
        hatPiece6.render(scaleFactor);
        hatPiece7.render(scaleFactor);
        RightBoot.render(scaleFactor);
        LeftBoot.render(scaleFactor);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    }
}

