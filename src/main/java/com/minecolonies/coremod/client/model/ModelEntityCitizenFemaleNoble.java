package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleNoble extends CitizenModel
{
    RendererModel breast;
    RendererModel hair;
    RendererModel dressPart1;
    RendererModel dressPart2;
    RendererModel dressPart3;
    RendererModel dressPart4;
    RendererModel dressPart5;
    RendererModel hat1;
    RendererModel hat2;
    RendererModel bag;
    RendererModel bagHand1;
    RendererModel bagHand2;

    public ModelEntityCitizenFemaleNoble()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(128, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new RendererModel(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 1F);
        bipedHeadwear.setTextureSize(128, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 12, 17);
        bipedBody.addBox(-4F, 0F, -4F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 3F);
        bipedBody.setTextureSize(128, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 34, 17);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1F, 0F, -1F, 3, 12, 3);
        bipedLeftArm.setRotationPoint(4F, 0F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        setRotation(bipedLeftArm, 0F, 0F, -0.1396263F);

        bipedRightArm = new RendererModel(this, 34, 17);
        bipedRightArm.addBox(-2F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-5F, 0F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 17);
        bipedRightLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(128, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 17);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(128, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        breast = new RendererModel(this, 0, 33);
        breast.addBox(-3F, 2F, -4.5F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(128, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair = new RendererModel(this, 46, 17);
        hair.addBox(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        hair.setRotationPoint(0F, 0F, 1F);
        hair.setTextureSize(128, 64);
        setRotation(hair, 0F, 0F, 0F);

        dressPart1 = new RendererModel(this, 65, 48);
        dressPart1.addBox(-8F, 9F, -9F, 16, 3, 13);
        dressPart1.setRotationPoint(0F, 11F, 0F);
        dressPart1.setTextureSize(128, 64);
        setRotation(dressPart1, 0F, 0F, 0F);

        dressPart2 = new RendererModel(this, 65, 34);
        dressPart2.addBox(-7F, 6F, -8F, 14, 3, 11);
        dressPart2.setRotationPoint(0F, 11F, 0F);
        dressPart2.setTextureSize(128, 64);
        setRotation(dressPart2, 0F, 0F, 0F);

        dressPart3 = new RendererModel(this, 65, 23);
        dressPart3.addBox(-6F, 4F, -7F, 12, 2, 9);
        dressPart3.setRotationPoint(0F, 11F, 0F);
        dressPart3.setTextureSize(128, 64);
        setRotation(dressPart3, 0F, 0F, 0F);

        dressPart4 = new RendererModel(this, 65, 14);
        dressPart4.addBox(-5F, 2F, -6F, 10, 2, 7);
        dressPart4.setRotationPoint(0F, 11F, 0F);
        dressPart4.setTextureSize(128, 64);
        setRotation(dressPart4, 0F, 0F, 0F);

        dressPart5 = new RendererModel(this, 65, 7);
        dressPart5.addBox(-4F, 0F, -5F, 8, 2, 5);
        dressPart5.setRotationPoint(0F, 11F, 0F);
        dressPart5.setTextureSize(128, 64);
        setRotation(dressPart5, 0F, 0F, 0F);

        hat1 = new RendererModel(this, 0, 48);
        hat1.addBox(-5F, -8F, -6F, 10, 2, 10, 0.1F);
        hat1.setRotationPoint(0F, 0F, 1F);
        hat1.setTextureSize(128, 64);
        setRotation(hat1, 0F, 0F, 0F);

        hat2 = new RendererModel(this, 0, 40);
        hat2.addBox(-3F, -10F, -4F, 6, 2, 6, 0.3F);
        hat2.setRotationPoint(0F, 0F, 1F);
        hat2.setTextureSize(128, 64);
        setRotation(hat2, 0F, 0F, 0F);

        bag = new RendererModel(this, 24, 32);
        bag.addBox(0F, 6F, -6F, 1, 4, 7);
        bag.setRotationPoint(4F, 0F, 0F);
        bag.setTextureSize(128, 64);
        setRotation(bag, 0F, 0F, 0F);

        bagHand1 = new RendererModel(this, 40, 32);
        bagHand1.addBox(0F, 1F, -3F, 1, 7, 0);
        bagHand1.setRotationPoint(4F, 0F, 0F);
        bagHand1.setTextureSize(128, 64);
        setRotation(bagHand1, -0.4014257F, 0F, 0F);

        bagHand2 = new RendererModel(this, 40, 32);
        bagHand2.addBox(0F, 1F, -2F, 1, 7, 0);
        bagHand2.setRotationPoint(4F, 0F, 0F);
        bagHand2.setTextureSize(128, 64);
        setRotation(bagHand2, 0.4014257F, 0F, 0F);

        bipedHeadwear.addChild(hat1);
        bipedHeadwear.addChild(hat2);

        bipedBody.addChild(breast);

        bipedBody.addChild(dressPart1);
        bipedBody.addChild(dressPart2);
        bipedBody.addChild(dressPart3);
        bipedBody.addChild(dressPart4);
        bipedBody.addChild(dressPart5);

        bipedBody.addChild(bagHand1);
        bipedBody.addChild(bagHand2);

        bipedBody.addChild(bag);
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