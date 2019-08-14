package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityFishermanFemale extends CitizenModel
{
    //fields
    private final RendererModel string;
    private final RendererModel string2;
    private final RendererModel hookTie1;
    private final RendererModel hookTie2;
    private final RendererModel hookTie3;
    private final RendererModel fish1;
    private final RendererModel fish2;
    private final RendererModel fish3;
    private final RendererModel reel;
    private final RendererModel line;
    private final RendererModel pole;
    private final RendererModel chest;
    private final RendererModel rightBoot;
    private final RendererModel leftBoot;
    private final RendererModel hairBack1;
    private final RendererModel hairBack2;
    private final RendererModel hairBack3;
    private final RendererModel hairBack4;
    private final RendererModel hairBack5;
    private final RendererModel hairBack6;
    private final RendererModel hairBack7;
    private final RendererModel hairBack8;
    private final RendererModel hairBack9;
    private final RendererModel hairBack10;

    public ModelEntityFishermanFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

        fish3 = new RendererModel(this, 61, 46);
        fish3.addBox(0.9F, 11F, -2.2F, 2, 3, 0);
        fish3.setRotationPoint(0F, 0F, 0F);
        fish3.setTextureSize(256, 128);
        fish3.mirror = true;
        setRotation(fish3, 0F, 0F, 0F);

        hookTie3 = new RendererModel(this, 58, 46);
        hookTie3.addBox(1F, 9F, -2.2F, 1, 2, 0);
        hookTie3.setRotationPoint(0F, 0F, 0F);
        hookTie3.setTextureSize(256, 128);
        hookTie3.mirror = true;
        setRotation(hookTie3, 0F, 0F, 0F);

        string = new RendererModel(this, 53, 38);
        string.addBox(-8F, -0.5F, -2.3F, 1, 5, 1);
        string.setRotationPoint(0F, 0F, 0F);
        string.setTextureSize(256, 128);
        string.mirror = true;
        setRotation(string, 0F, 0F, -1.041001F);

        hookTie1 = new RendererModel(this, 58, 38);
        hookTie1.addBox(-3.5F, 7F, -2.2F, 1, 2, 0);
        hookTie1.setRotationPoint(0F, 0F, 0F);
        hookTie1.setTextureSize(256, 128);
        hookTie1.mirror = true;
        setRotation(hookTie1, 0F, 0F, 0F);

        hookTie2 = new RendererModel(this, 58, 42);
        hookTie2.addBox(-1.5F, 8.5F, -2.2F, 1, 2, 0);
        hookTie2.setRotationPoint(0F, 0F, 0F);
        hookTie2.setTextureSize(256, 128);
        hookTie2.mirror = true;
        setRotation(hookTie2, 0F, 0F, 0F);

        fish1 = new RendererModel(this, 61, 38);
        fish1.addBox(-4.4F, 9F, -2.2F, 2, 3, 0);
        fish1.setRotationPoint(0F, 0F, 0F);
        fish1.setTextureSize(256, 128);
        fish1.mirror = true;
        setRotation(fish1, 0F, 0F, 0F);

        fish2 = new RendererModel(this, 61, 42);
        fish2.addBox(-2F, 10.5F, -2.2F, 2, 3, 0);
        fish2.setRotationPoint(0F, 0F, 0F);
        fish2.setTextureSize(256, 128);
        fish2.mirror = true;
        setRotation(fish2, 0F, 0F, 0F);

        string2 = new RendererModel(this, 53, 44);
        string2.addBox(-9.05F, 1.65F, -2.3F, 1, 4, 1);
        string2.setRotationPoint(0F, 0F, 0F);
        string2.setTextureSize(256, 128);
        string2.mirror = true;
        setRotation(string2, 0F, 0F, -1.375609F);

        reel = new RendererModel(this, 62, 64);
        reel.addBox(-6F, 6F, 2F, 2, 2, 1);
        reel.setRotationPoint(0F, 0F, 0F);
        reel.setTextureSize(256, 128);
        reel.mirror = true;
        setRotation(reel, 0F, 0F, -0.7435722F);

        line = new RendererModel(this, 62, 52);
        line.addBox(-4.5F, -4.75F, 2.5F, 1, 11, 0);
        line.setRotationPoint(0F, 0F, 0F);
        line.setTextureSize(256, 128);
        line.mirror = true;
        setRotation(line, 0F, 0F, -0.7435722F);

        pole = new RendererModel(this, 57, 52);
        pole.addBox(-4F, -5F, 2F, 1, 16, 1);
        pole.setRotationPoint(0F, 0F, 0F);
        pole.setTextureSize(256, 128);
        pole.mirror = true;
        setRotation(pole, 0F, 0F, -0.7435722F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

        chest = new RendererModel(this, 25, 32);
        chest.addBox(-3.5F, 3.5F, 0F, 7, 3, 3);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, -0.6320364F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);
        bipedLeftLeg.mirror = false;

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        rightBoot = new RendererModel(this, 20, 102);
        rightBoot.addBox(-0.5F, -7F, -2.5F, 5, 2, 5);
        rightBoot.setRotationPoint(-2F, 12F, 0F);
        rightBoot.setTextureSize(256, 128);
        rightBoot.mirror = true;
        setRotation(rightBoot, 0F, 0F, 0F);

        leftBoot = new RendererModel(this, 0, 102);
        leftBoot.addBox(-4.5F, -7F, -2.5F, 5, 2, 5);
        leftBoot.setRotationPoint(2F, 12F, 0F);
        leftBoot.setTextureSize(256, 128);
        leftBoot.mirror = true;
        setRotation(leftBoot, 0F, 0F, 0F);

        hairBack1 = new RendererModel(this, 0, 74);
        hairBack1.addBox(-4.5F, -5.5F, -4.5F, 1, 2, 1);
        hairBack1.setRotationPoint(0F, 0F, 0F);
        hairBack1.setTextureSize(256, 128);
        hairBack1.mirror = true;
        setRotation(hairBack1, 0F, 0F, 0F);

        hairBack10 = new RendererModel(this, 0, 53);
        hairBack10.addBox(-1.5F, 6.5F, 3.5F, 3, 1, 1);
        hairBack10.setRotationPoint(0F, 0F, 0F);
        hairBack10.setTextureSize(256, 128);
        hairBack10.mirror = true;
        setRotation(hairBack10, 0F, 0F, 0F);

        hairBack7 = new RendererModel(this, 0, 56);
        hairBack7.addBox(-4.5F, -8.5F, -0.5F, 9, 5, 2);
        hairBack7.setRotationPoint(0F, 0F, 0F);
        hairBack7.setTextureSize(256, 128);
        hairBack7.mirror = true;
        setRotation(hairBack7, 0F, 0F, 0F);

        hairBack6 = new RendererModel(this, 0, 33);
        hairBack6.addBox(-4.5F, -8.5F, 1.5F, 9, 9, 3);
        hairBack6.setRotationPoint(0F, 0F, 0F);
        hairBack6.setTextureSize(256, 128);
        hairBack6.mirror = true;
        setRotation(hairBack6, 0F, 0F, 0F);

        hairBack8 = new RendererModel(this, 0, 45);
        hairBack8.addBox(-3.5F, 0.5F, 3.5F, 7, 4, 1);
        hairBack8.setRotationPoint(0F, 0F, 0F);
        hairBack8.setTextureSize(256, 128);
        hairBack8.mirror = true;
        setRotation(hairBack8, 0F, 0F, 0F);

        hairBack3 = new RendererModel(this, 0, 63);
        hairBack3.addBox(-4.5F, -8.5F, -4.5F, 9, 2, 4);
        hairBack3.setRotationPoint(0F, 0F, 0F);
        hairBack3.setTextureSize(256, 128);
        hairBack3.mirror = true;
        setRotation(hairBack3, 0F, 0F, 0F);

        hairBack4 = new RendererModel(this, 12, 69);
        hairBack4.addBox(-4.5F, -6.5F, -4.5F, 2, 1, 4);
        hairBack4.setRotationPoint(0F, 0F, 0F);
        hairBack4.setTextureSize(256, 128);
        hairBack4.mirror = true;
        setRotation(hairBack4, 0F, 0F, 0F);

        hairBack5 = new RendererModel(this, 0, 69);
        hairBack5.addBox(2.5F, -6.5F, -4.5F, 2, 1, 4);
        hairBack5.setRotationPoint(0F, 0F, 0F);
        hairBack5.setTextureSize(256, 128);
        hairBack5.mirror = true;
        setRotation(hairBack5, 0F, 0F, 0F);

        hairBack2 = new RendererModel(this, 5, 74);
        hairBack2.addBox(3.5F, -5.5F, -4.5F, 1, 3, 1);
        hairBack2.setRotationPoint(0F, 0F, 0F);
        hairBack2.setTextureSize(256, 128);
        hairBack2.mirror = true;
        setRotation(hairBack2, 0F, 0F, 0F);

        hairBack9 = new RendererModel(this, 0, 50);
        hairBack9.addBox(-2.5F, 4.5F, 3.5F, 5, 2, 1);
        hairBack9.setRotationPoint(0F, 0F, 0F);
        hairBack9.setTextureSize(256, 128);
        hairBack9.mirror = true;
        setRotation(hairBack9, 0F, 0F, 0F);

        bipedHeadwear.isHidden = true;

        bipedBody.addChild(string);
        bipedBody.addChild(string2);

        bipedBody.addChild(hookTie1);
        bipedBody.addChild(hookTie2);
        bipedBody.addChild(hookTie3);

        bipedBody.addChild(fish1);
        bipedBody.addChild(fish2);
        bipedBody.addChild(fish3);

        bipedBody.addChild(reel);
        bipedBody.addChild(line);
        bipedBody.addChild(pole);

        bipedBody.addChild(chest);

        bipedRightLeg.addChild(rightBoot);
        bipedLeftLeg.addChild(leftBoot);

        bipedHead.addChild(hairBack1);
        bipedHead.addChild(hairBack2);
        bipedHead.addChild(hairBack3);
        bipedHead.addChild(hairBack4);
        bipedHead.addChild(hairBack5);
        bipedHead.addChild(hairBack6);
        bipedHead.addChild(hairBack7);
        bipedHead.addChild(hairBack8);
        bipedHead.addChild(hairBack9);
        bipedHead.addChild(hairBack10);
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void render(
      @NotNull final AbstractEntityCitizen entity,
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
}
