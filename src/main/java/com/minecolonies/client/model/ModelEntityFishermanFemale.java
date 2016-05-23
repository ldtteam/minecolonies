package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelEntityFishermanFemale extends ModelBiped
{
    //fields
    private ModelRenderer Fish3;
    private ModelRenderer HookTie3;
    private ModelRenderer String;
    private ModelRenderer HookTie1;
    private ModelRenderer HookTie2;
    private ModelRenderer Fish1;
    private ModelRenderer Fish2;
    private ModelRenderer String2;
    private ModelRenderer Reel;
    private ModelRenderer Line;
    private ModelRenderer Pole;
    private ModelRenderer RightArm;
    private ModelRenderer LeftArm;
    private ModelRenderer Chest;
    private ModelRenderer RightLeg;
    private ModelRenderer LeftLeg;
    private ModelRenderer Head;
    private ModelRenderer Body;
    private ModelRenderer RightBoot;
    private ModelRenderer LeftBoot;
    private ModelRenderer HairBack1;
    private ModelRenderer HairBack10;
    private ModelRenderer HairBack7;
    private ModelRenderer HairBack6;
    private ModelRenderer HairBack8;
    private ModelRenderer HairBack3;
    private ModelRenderer HairBack4;
    private ModelRenderer HairBack5;
    private ModelRenderer HairBack2;
    private ModelRenderer HairBack9;

    public ModelEntityFishermanFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

        Fish3 = new ModelRenderer(this, 61, 46);
        Fish3.addBox(0.9F, 11F, -2.2F, 2, 3, 0);
        Fish3.setRotationPoint(0F, 0F, 0F);
        Fish3.setTextureSize(256, 128);
        Fish3.mirror = true;
        setRotation(Fish3, 0F, 0F, 0F);

        HookTie3 = new ModelRenderer(this, 58, 46);
        HookTie3.addBox(1F, 9F, -2.2F, 1, 2, 0);
        HookTie3.setRotationPoint(0F, 0F, 0F);
        HookTie3.setTextureSize(256, 128);
        HookTie3.mirror = true;
        setRotation(HookTie3, 0F, 0F, 0F);

        String = new ModelRenderer(this, 53, 38);
        String.addBox(-8F, -0.5F, -2.3F, 1, 5, 1);
        String.setRotationPoint(0F, 0F, 0F);
        String.setTextureSize(256, 128);
        String.mirror = true;
        setRotation(String, 0F, 0F, -1.041001F);

        HookTie1 = new ModelRenderer(this, 58, 38);
        HookTie1.addBox(-3.5F, 7F, -2.2F, 1, 2, 0);
        HookTie1.setRotationPoint(0F, 0F, 0F);
        HookTie1.setTextureSize(256, 128);
        HookTie1.mirror = true;
        setRotation(HookTie1, 0F, 0F, 0F);

        HookTie2 = new ModelRenderer(this, 58, 42);
        HookTie2.addBox(-1.5F, 8.5F, -2.2F, 1, 2, 0);
        HookTie2.setRotationPoint(0F, 0F, 0F);
        HookTie2.setTextureSize(256, 128);
        HookTie2.mirror = true;
        setRotation(HookTie2, 0F, 0F, 0F);

        Fish1 = new ModelRenderer(this, 61, 38);
        Fish1.addBox(-4.4F, 9F, -2.2F, 2, 3, 0);
        Fish1.setRotationPoint(0F, 0F, 0F);
        Fish1.setTextureSize(256, 128);
        Fish1.mirror = true;
        setRotation(Fish1, 0F, 0F, 0F);

        Fish2 = new ModelRenderer(this, 61, 42);
        Fish2.addBox(-2F, 10.5F, -2.2F, 2, 3, 0);
        Fish2.setRotationPoint(0F, 0F, 0F);
        Fish2.setTextureSize(256, 128);
        Fish2.mirror = true;
        setRotation(Fish2, 0F, 0F, 0F);

        String2 = new ModelRenderer(this, 53, 44);
        String2.addBox(-9.05F, 1.65F, -2.3F, 1, 4, 1);
        String2.setRotationPoint(0F, 0F, 0F);
        String2.setTextureSize(256, 128);
        String2.mirror = true;
        setRotation(String2, 0F, 0F, -1.375609F);

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

        Chest = new ModelRenderer(this, 25, 32);
        Chest.addBox(-3.5F, 3.5F, 0F, 7, 3, 3);
        Chest.setRotationPoint(0F, 0F, 0F);
        Chest.setTextureSize(256, 128);
        Chest.mirror = true;
        setRotation(Chest, -0.6320364F, 0F, 0F);

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

        HairBack1 = new ModelRenderer(this, 0, 74);
        HairBack1.addBox(-4.5F, -5.5F, -4.5F, 1, 2, 1);
        HairBack1.setRotationPoint(0F, 0F, 0F);
        HairBack1.setTextureSize(256, 128);
        HairBack1.mirror = true;
        setRotation(HairBack1, 0F, 0F, 0F);

        HairBack10 = new ModelRenderer(this, 0, 53);
        HairBack10.addBox(-1.5F, 6.5F, 3.5F, 3, 1, 1);
        HairBack10.setRotationPoint(0F, 0F, 0F);
        HairBack10.setTextureSize(256, 128);
        HairBack10.mirror = true;
        setRotation(HairBack10, 0F, 0F, 0F);

        HairBack7 = new ModelRenderer(this, 0, 56);
        HairBack7.addBox(-4.5F, -8.5F, -0.5F, 9, 5, 2);
        HairBack7.setRotationPoint(0F, 0F, 0F);
        HairBack7.setTextureSize(256, 128);
        HairBack7.mirror = true;
        setRotation(HairBack7, 0F, 0F, 0F);

        HairBack6 = new ModelRenderer(this, 0, 33);
        HairBack6.addBox(-4.5F, -8.5F, 1.5F, 9, 9, 3);
        HairBack6.setRotationPoint(0F, 0F, 0F);
        HairBack6.setTextureSize(256, 128);
        HairBack6.mirror = true;
        setRotation(HairBack6, 0F, 0F, 0F);

        HairBack8 = new ModelRenderer(this, 0, 45);
        HairBack8.addBox(-3.5F, 0.5F, 3.5F, 7, 4, 1);
        HairBack8.setRotationPoint(0F, 0F, 0F);
        HairBack8.setTextureSize(256, 128);
        HairBack8.mirror = true;
        setRotation(HairBack8, 0F, 0F, 0F);

        HairBack3 = new ModelRenderer(this, 0, 63);
        HairBack3.addBox(-4.5F, -8.5F, -4.5F, 9, 2, 4);
        HairBack3.setRotationPoint(0F, 0F, 0F);
        HairBack3.setTextureSize(256, 128);
        HairBack3.mirror = true;
        setRotation(HairBack3, 0F, 0F, 0F);

        HairBack4 = new ModelRenderer(this, 12, 69);
        HairBack4.addBox(-4.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack4.setRotationPoint(0F, 0F, 0F);
        HairBack4.setTextureSize(256, 128);
        HairBack4.mirror = true;
        setRotation(HairBack4, 0F, 0F, 0F);

        HairBack5 = new ModelRenderer(this, 0, 69);
        HairBack5.addBox(2.5F, -6.5F, -4.5F, 2, 1, 4);
        HairBack5.setRotationPoint(0F, 0F, 0F);
        HairBack5.setTextureSize(256, 128);
        HairBack5.mirror = true;
        setRotation(HairBack5, 0F, 0F, 0F);

        HairBack2 = new ModelRenderer(this, 5, 74);
        HairBack2.addBox(3.5F, -5.5F, -4.5F, 1, 3, 1);
        HairBack2.setRotationPoint(0F, 0F, 0F);
        HairBack2.setTextureSize(256, 128);
        HairBack2.mirror = true;
        setRotation(HairBack2, 0F, 0F, 0F);

        HairBack9 = new ModelRenderer(this, 0, 50);
        HairBack9.addBox(-2.5F, 4.5F, 3.5F, 5, 2, 1);
        HairBack9.setRotationPoint(0F, 0F, 0F);
        HairBack9.setTextureSize(256, 128);
        HairBack9.mirror = true;
        setRotation(HairBack9, 0F, 0F, 0F);
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        Fish3.render(scaleFactor);
        HookTie3.render(scaleFactor);
        String.render(scaleFactor);
        HookTie1.render(scaleFactor);
        HookTie2.render(scaleFactor);
        Fish1.render(scaleFactor);
        Fish2.render(scaleFactor);
        String2.render(scaleFactor);
        Reel.render(scaleFactor);
        Line.render(scaleFactor);
        Pole.render(scaleFactor);
        RightArm.render(scaleFactor);
        LeftArm.render(scaleFactor);
        Chest.render(scaleFactor);
        RightLeg.render(scaleFactor);
        LeftLeg.render(scaleFactor);
        Head.render(scaleFactor);
        Body.render(scaleFactor);
        RightBoot.render(scaleFactor);
        LeftBoot.render(scaleFactor);
        HairBack1.render(scaleFactor);
        HairBack10.render(scaleFactor);
        HairBack7.render(scaleFactor);
        HairBack6.render(scaleFactor);
        HairBack8.render(scaleFactor);
        HairBack3.render(scaleFactor);
        HairBack4.render(scaleFactor);
        HairBack5.render(scaleFactor);
        HairBack2.render(scaleFactor);
        HairBack9.render(scaleFactor);
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
