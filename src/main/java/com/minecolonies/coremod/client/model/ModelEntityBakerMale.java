package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelEntityBakerMale extends CitizenModel
{
    //fields
    RendererModel top;
    RendererModel base;
    RendererModel middle;

    public ModelEntityBakerMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        top = new RendererModel(this, 0, 53);
        top.addBox(-2.5F, -11F, -4.6F, 5, 1, 7);
        top.setRotationPoint(0F, 0F, 0F);
        top.setTextureSize(128, 64);
        top.mirror = true;
        setRotation(top, -0.1858931F, 0F, 0F);

        base = new RendererModel(this, 0, 33);
        base.addBox(-4.5F, -9F, -5.8F, 9, 2, 9);
        base.setRotationPoint(0F, 0F, 0F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, -0.1858931F, 0F, 0F);

        middle = new RendererModel(this, 0, 44);
        middle.addBox(-3.5F, -10F, -5F, 7, 1, 8);
        middle.setRotationPoint(0F, 0F, 0F);
        middle.setTextureSize(128, 64);
        middle.mirror = true;
        setRotation(middle, -0.1858931F, 0F, 0F);

        bipedHead.addChild(base);
        bipedHead.addChild(middle);
        bipedHead.addChild(top);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(
      final AbstractEntityCitizen entityIn,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        final float bodyX = bipedBody.rotateAngleX;
        final float headX = bipedHead.rotateAngleX;
        bipedBody.rotateAngleX = bodyX;
        bipedHead.rotateAngleX = headX;
        base.rotateAngleX = headX;
        middle.rotateAngleX = headX;
        top.rotateAngleX = headX;
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }
}
