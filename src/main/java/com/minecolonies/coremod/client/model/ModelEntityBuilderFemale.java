package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBuilderFemale extends BipedModel
{
    RendererModel hatBase;
    RendererModel hatBottomMiddle;
    RendererModel hatBack;
    RendererModel hatFront;
    RendererModel hatTopMiddle;
    RendererModel hatBrimBase;
    RendererModel hatBrimFrontTip;
    RendererModel hatBrimFront;
    RendererModel chest;
    RendererModel ponytailTail;
    RendererModel ponytailBase;
    RendererModel hammerHandle;
    RendererModel hammerHead;
    RendererModel belt;
    RendererModel ruler;

    public ModelEntityBuilderFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        hatBase = new RendererModel(this, 57, 19);
        hatBase.addBox(-4F, -9.7F, -4F, 8, 2, 7);
        hatBase.setRotationPoint(0F, 0F, 0F);
        hatBase.setTextureSize(128, 64);
        hatBase.mirror = true;
        setRotation(hatBase, -0.1396263F, 0F, 0F);

        hatBottomMiddle = new RendererModel(this, 57, 8);
        hatBottomMiddle.addBox(-3F, -10F, -5F, 6, 2, 9);
        hatBottomMiddle.setTextureSize(128, 64);
        hatBottomMiddle.mirror = true;
        setRotation(hatBottomMiddle, 0F, 0F, 0F);

        hatTopMiddle = new RendererModel(this, 61, 0);
        hatTopMiddle.addBox(-2F, -11F, -4F, 4, 1, 7);
        hatTopMiddle.setTextureSize(128, 64);
        hatTopMiddle.mirror = true;
        setRotation(hatTopMiddle, 0F, 0F, 0F);

        hatBack = new RendererModel(this, 64, 31);
        hatBack.addBox(-3.5F, -8F, 4F, 7, 1, 1);
        hatBack.setTextureSize(128, 64);
        hatBack.mirror = true;
        setRotation(hatBack, 0F, 0F, 0F);

        hatFront = new RendererModel(this, 66, 28);
        hatFront.addBox(-2.5F, -9F, -6F, 5, 1, 1);
        hatFront.setTextureSize(128, 64);
        hatFront.mirror = true;
        setRotation(hatFront, 0F, 0F, 0F);

        hatBrimBase = new RendererModel(this, 53, 33);
        hatBrimBase.addBox(-4.5F, -8F, -6F, 9, 1, 10);
        hatBrimBase.setTextureSize(128, 64);
        hatBrimBase.mirror = true;
        setRotation(hatBrimBase, 0F, 0F, 0F);

        hatBrimFront = new RendererModel(this, 64, 44);
        hatBrimFront.addBox(-3.5F, -8F, -7F, 7, 1, 1);
        hatBrimFront.setTextureSize(128, 64);
        hatBrimFront.mirror = true;
        setRotation(hatBrimFront, 0F, 0F, 0F);

        hatBrimFrontTip = new RendererModel(this, 66, 46);
        hatBrimFrontTip.addBox(-2.5F, -8F, -8F, 5, 1, 1);
        hatBrimFrontTip.setTextureSize(128, 64);
        hatBrimFrontTip.mirror = true;
        setRotation(hatBrimFrontTip, 0F, 0F, 0F);

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

        chest = new RendererModel(this, 17, 32);
        chest.addBox(-3.5F, 1.7F, -1F, 7, 3, 4);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

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

        ponytailBase = new RendererModel(this, 32, 0);
        ponytailBase.addBox(-1F, -2.2F, 3.5F, 2, 5, 1);
        ponytailBase.setTextureSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.2268928F, 0F, 0F);

        ponytailTail = new RendererModel(this, 33, 6);
        ponytailTail.addBox(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        ponytailTail.setTextureSize(128, 64);
        ponytailTail.mirror = true;
        setRotation(ponytailTail, -0.122173F, 0F, 0F);

        hammerHandle = new RendererModel(this, 2, 49);
        hammerHandle.addBox(1F, 7.3F, -2.4F, 1, 4, 1);
        hammerHandle.setTextureSize(128, 64);
        hammerHandle.mirror = true;
        setRotation(hammerHandle, 0F, 0F, 0.3141593F);

        hammerHead = new RendererModel(this, 0, 47);
        hammerHead.addBox(0F, 7.5F, -2.5F, 3, 1, 1);
        hammerHead.setTextureSize(128, 64);
        hammerHead.mirror = true;
        setRotation(hammerHead, 0F, 0F, 0F);

        belt = new RendererModel(this, 0, 40);
        belt.addBox(-4.5F, 9F, -2.5F, 9, 1, 5);
        belt.setTextureSize(128, 64);
        belt.mirror = true;
        setRotation(belt, 0F, 0F, 0F);

        ruler = new RendererModel(this, 17, 47);
        ruler.addBox(2F, 7.3F, -2.2F, 1, 4, 1);
        ruler.setTextureSize(128, 64);
        ruler.mirror = true;
        setRotation(ruler, 0F, 0F, 0F);

        bipedBody.addChild(chest);
        bipedBody.addChild(belt);
        bipedBody.addChild(ruler);
        bipedBody.addChild(hammerHandle);
        hammerHandle.addChild(hammerHead);

        bipedHead.addChild(hatBase);
        hatBase.addChild(hatBottomMiddle);
        hatBottomMiddle.addChild(hatBack);
        hatBottomMiddle.addChild(hatFront);
        hatBottomMiddle.addChild(hatTopMiddle);

        hatBase.addChild(hatBrimBase);
        hatBrimBase.addChild(hatBrimFront);
        hatBrimFront.addChild(hatBrimFrontTip);

        bipedHead.addChild(ponytailBase);
        ponytailBase.addChild(ponytailTail);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
