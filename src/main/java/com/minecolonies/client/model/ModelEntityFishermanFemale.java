package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityFishermanFemale extends ModelBiped
{
    //fields
    private ModelRenderer string;
    private ModelRenderer string2;
    private ModelRenderer hookTie1;
    private ModelRenderer hookTie2;
    private ModelRenderer hookTie3;
    private ModelRenderer fish1;
    private ModelRenderer fish2;
    private ModelRenderer fish3;
    private ModelRenderer reel;
    private ModelRenderer line;
    private ModelRenderer pole;
    private ModelRenderer chest;
    private ModelRenderer rightBoot;
    private ModelRenderer leftBoot;
    private ModelRenderer hairBack1;
    private ModelRenderer hairBack2;
    private ModelRenderer hairBack3;
    private ModelRenderer hairBack4;
    private ModelRenderer hairBack5;
    private ModelRenderer hairBack6;
    private ModelRenderer hairBack7;
    private ModelRenderer hairBack8;
    private ModelRenderer hairBack9;
    private ModelRenderer hairBack10;

    public ModelEntityFishermanFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

        fish3 = new ModelRenderer(this, 61, 46);
        fish3.addBox(0.9F, 11F, -2.2F, 2, 3, 0);
        fish3.setRotationPoint(0F, 0F, 0F);
        fish3.setTextureSize(256, 128);
        fish3.mirror = true;
        setRotation(fish3, 0F, 0F, 0F);

        hookTie3 = new ModelRenderer(this, 58, 46);
        hookTie3.addBox(1F, 9F, -2.2F, 1, 2, 0);
        hookTie3.setRotationPoint(0F, 0F, 0F);
        hookTie3.setTextureSize(256, 128);
        hookTie3.mirror = true;
        setRotation(hookTie3, 0F, 0F, 0F);

        string = new ModelRenderer(this, 53, 38);
        string.addBox(-8F, -0.5F, -2.3F, 1, 5, 1);
        string.setRotationPoint(0F, 0F, 0F);
        string.setTextureSize(256, 128);
        string.mirror = true;
        setRotation(string, 0F, 0F, -1.041001F);

        hookTie1 = new ModelRenderer(this, 58, 38);
        hookTie1.addBox(-3.5F, 7F, -2.2F, 1, 2, 0);
        hookTie1.setRotationPoint(0F, 0F, 0F);
        hookTie1.setTextureSize(256, 128);
        hookTie1.mirror = true;
        setRotation(hookTie1, 0F, 0F, 0F);

        hookTie2 = new ModelRenderer(this, 58, 42);
        hookTie2.addBox(-1.5F, 8.5F, -2.2F, 1, 2, 0);
        hookTie2.setRotationPoint(0F, 0F, 0F);
        hookTie2.setTextureSize(256, 128);
        hookTie2.mirror = true;
        setRotation(hookTie2, 0F, 0F, 0F);

        fish1 = new ModelRenderer(this, 61, 38);
        fish1.addBox(-4.4F, 9F, -2.2F, 2, 3, 0);
        fish1.setRotationPoint(0F, 0F, 0F);
        fish1.setTextureSize(256, 128);
        fish1.mirror = true;
        setRotation(fish1, 0F, 0F, 0F);

        fish2 = new ModelRenderer(this, 61, 42);
        fish2.addBox(-2F, 10.5F, -2.2F, 2, 3, 0);
        fish2.setRotationPoint(0F, 0F, 0F);
        fish2.setTextureSize(256, 128);
        fish2.mirror = true;
        setRotation(fish2, 0F, 0F, 0F);

        string2 = new ModelRenderer(this, 53, 44);
        string2.addBox(-9.05F, 1.65F, -2.3F, 1, 4, 1);
        string2.setRotationPoint(0F, 0F, 0F);
        string2.setTextureSize(256, 128);
        string2.mirror = true;
        setRotation(string2, 0F, 0F, -1.375609F);

        reel = new ModelRenderer(this, 62, 64);
        reel.addBox(-6F, 6F, 2F, 2, 2, 1);
        reel.setRotationPoint(0F, 0F, 0F);
        reel.setTextureSize(256, 128);
        reel.mirror = true;
        setRotation(reel, 0F, 0F, -0.7435722F);

        line = new ModelRenderer(this, 62, 52);
        line.addBox(-4.5F, -4.75F, 2.5F, 1, 11, 0);
        line.setRotationPoint(0F, 0F, 0F);
        line.setTextureSize(256, 128);
        line.mirror = true;
        setRotation(line, 0F, 0F, -0.7435722F);

        pole = new ModelRenderer(this, 57, 52);
        pole.addBox(-4F, -5F, 2F, 1, 16, 1);
        pole.setRotationPoint(0F, 0F, 0F);
        pole.setTextureSize(256, 128);
        pole.mirror = true;
        setRotation(pole, 0F, 0F, -0.7435722F);

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
        bipedLeftArm.mirror = false;

        chest = new ModelRenderer(this, 25, 32);
        chest.addBox(-3.5F, 3.5F, 0F, 7, 3, 3);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, -0.6320364F, 0F, 0F);

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
        bipedLeftLeg.mirror = false;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        rightBoot = new ModelRenderer(this, 20, 102);
        rightBoot.addBox(-2.7F, 4F, -2.5F, 5, 2, 5);
        rightBoot.setRotationPoint(-2F, 12F, 0F);
        rightBoot.setTextureSize(256, 128);
        rightBoot.mirror = true;
        setRotation(rightBoot, 0F, 0F, 0F);

        leftBoot = new ModelRenderer(this, 0, 102);
        leftBoot.addBox(-2.3F, 4F, -2.49F, 5, 2, 5);
        leftBoot.setRotationPoint(2F, 12F, 0F);
        leftBoot.setTextureSize(256, 128);
        leftBoot.mirror = true;
        setRotation(leftBoot, 0F, 0F, 0F);

        hairBack1 = new ModelRenderer(this, 0, 74);
        hairBack1.addBox(-4.5F, -5.5F, -4.5F, 1, 2, 1);
        hairBack1.setRotationPoint(0F, 0F, 0F);
        hairBack1.setTextureSize(256, 128);
        hairBack1.mirror = true;
        setRotation(hairBack1, 0F, 0F, 0F);

        hairBack10 = new ModelRenderer(this, 0, 53);
        hairBack10.addBox(-1.5F, 6.5F, 3.5F, 3, 1, 1);
        hairBack10.setRotationPoint(0F, 0F, 0F);
        hairBack10.setTextureSize(256, 128);
        hairBack10.mirror = true;
        setRotation(hairBack10, 0F, 0F, 0F);

        hairBack7 = new ModelRenderer(this, 0, 56);
        hairBack7.addBox(-4.5F, -8.5F, -0.5F, 9, 5, 2);
        hairBack7.setRotationPoint(0F, 0F, 0F);
        hairBack7.setTextureSize(256, 128);
        hairBack7.mirror = true;
        setRotation(hairBack7, 0F, 0F, 0F);

        hairBack6 = new ModelRenderer(this, 0, 33);
        hairBack6.addBox(-4.5F, -8.5F, 1.5F, 9, 9, 3);
        hairBack6.setRotationPoint(0F, 0F, 0F);
        hairBack6.setTextureSize(256, 128);
        hairBack6.mirror = true;
        setRotation(hairBack6, 0F, 0F, 0F);

        hairBack8 = new ModelRenderer(this, 0, 45);
        hairBack8.addBox(-3.5F, 0.5F, 3.5F, 7, 4, 1);
        hairBack8.setRotationPoint(0F, 0F, 0F);
        hairBack8.setTextureSize(256, 128);
        hairBack8.mirror = true;
        setRotation(hairBack8, 0F, 0F, 0F);

        hairBack3 = new ModelRenderer(this, 0, 63);
        hairBack3.addBox(-4.5F, -8.5F, -4.5F, 9, 2, 4);
        hairBack3.setRotationPoint(0F, 0F, 0F);
        hairBack3.setTextureSize(256, 128);
        hairBack3.mirror = true;
        setRotation(hairBack3, 0F, 0F, 0F);

        hairBack4 = new ModelRenderer(this, 12, 69);
        hairBack4.addBox(-4.5F, -6.5F, -4.5F, 2, 1, 4);
        hairBack4.setRotationPoint(0F, 0F, 0F);
        hairBack4.setTextureSize(256, 128);
        hairBack4.mirror = true;
        setRotation(hairBack4, 0F, 0F, 0F);

        hairBack5 = new ModelRenderer(this, 0, 69);
        hairBack5.addBox(2.5F, -6.5F, -4.5F, 2, 1, 4);
        hairBack5.setRotationPoint(0F, 0F, 0F);
        hairBack5.setTextureSize(256, 128);
        hairBack5.mirror = true;
        setRotation(hairBack5, 0F, 0F, 0F);

        hairBack2 = new ModelRenderer(this, 5, 74);
        hairBack2.addBox(3.5F, -5.5F, -4.5F, 1, 3, 1);
        hairBack2.setRotationPoint(0F, 0F, 0F);
        hairBack2.setTextureSize(256, 128);
        hairBack2.mirror = true;
        setRotation(hairBack2, 0F, 0F, 0F);

        hairBack9 = new ModelRenderer(this, 0, 50);
        hairBack9.addBox(-2.5F, 4.5F, 3.5F, 5, 2, 1);
        hairBack9.setRotationPoint(0F, 0F, 0F);
        hairBack9.setTextureSize(256, 128);
        hairBack9.mirror = true;
        setRotation(hairBack9, 0F, 0F, 0F);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

        string.render(scaleFactor);
        string2.render(scaleFactor);

        hookTie1.render(scaleFactor);
        hookTie2.render(scaleFactor);
        hookTie3.render(scaleFactor);
        fish1.render(scaleFactor);
        fish2.render(scaleFactor);
        fish3.render(scaleFactor);

        reel.render(scaleFactor);
        line.render(scaleFactor);
        pole.render(scaleFactor);

        chest.render(scaleFactor);

        rightBoot.render(scaleFactor);
        leftBoot.render(scaleFactor);

        hairBack1.render(scaleFactor);
        hairBack2.render(scaleFactor);
        hairBack3.render(scaleFactor);
        hairBack4.render(scaleFactor);
        hairBack5.render(scaleFactor);
        hairBack6.render(scaleFactor);
        hairBack7.render(scaleFactor);
        hairBack8.render(scaleFactor);
        hairBack9.render(scaleFactor);
        hairBack10.render(scaleFactor);
    }
}
