package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

/**
 * Class used for the bakery entity model.
 */
public class ModelEntityBakerFemale extends CitizenModel
{

    public ModelEntityBakerFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

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

        final RendererModel chest = new RendererModel(this, 17, 32);
        chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

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

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        final RendererModel ponyTailB = new RendererModel(this, 33, 6);
        ponyTailB.addBox(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        ponyTailB.setRotationPoint(0F, 0F, 0F);
        ponyTailB.setTextureSize(256, 128);
        ponyTailB.mirror = true;
        setRotation(ponyTailB, 0.1047198F, 0F, 0F);

        final RendererModel ponyTailT = new RendererModel(this, 32, 0);
        ponyTailT.addBox(-1F, -2.2F, 3.5F, 2, 5, 1);
        ponyTailT.setRotationPoint(0F, 0F, 0F);
        ponyTailT.setTextureSize(256, 128);
        ponyTailT.mirror = true;
        setRotation(ponyTailT, 0.2268928F, 0F, 0F);

        final RendererModel lipR = new RendererModel(this, 22, 70);
        lipR.addBox(2F, -6.2F, -7.5F, 2, 1, 9);
        lipR.setRotationPoint(0F, 0F, 0F);
        lipR.setTextureSize(256, 128);
        lipR.mirror = true;
        setRotation(lipR, -0.18422253F, -0.8753831F, -1.2905022F);

        final RendererModel baseT = new RendererModel(this, 0, 40);
        baseT.addBox(-4.5F, -8.2F, -6.5F, 9, 1, 6);
        baseT.setRotationPoint(0F, 0F, 0F);
        baseT.setTextureSize(256, 128);
        baseT.mirror = true;
        setRotation(baseT, -0.8922867F, 0F, 0F);

        final RendererModel lipT = new RendererModel(this, 0, 67);
        lipT.addBox(-5F, -9.2F, -1F, 10, 1, 2);
        lipT.setRotationPoint(0F, 0F, 0F);
        lipT.setTextureSize(256, 128);
        lipT.mirror = true;
        setRotation(lipT, 0.2230717F, 0F, 0F);

        final RendererModel lipL = new RendererModel(this, 0, 70);
        lipL.addBox(-4F, -6.2F, -7.5F, 2, 1, 9);
        lipL.setRotationPoint(0F, 0F, 0F);
        lipL.setTextureSize(256, 128);
        lipL.mirror = true;
        setRotation(lipL, -0.18435909F, 0.87547284F, 1.2903719F);

        final RendererModel lipB = new RendererModel(this, 0, 80);
        lipB.addBox(-5F, -5.1F, -1.5F, 10, 1, 2);
        lipB.setRotationPoint(0F, 0F, 0F);
        lipB.setTextureSize(256, 128);
        lipB.mirror = true;
        setRotation(lipB, -1.375609F, 0F, 0F);

        final RendererModel baseB = new RendererModel(this, 0, 57);
        baseB.addBox(-5F, -5.2F, -8F, 10, 1, 9);
        baseB.setRotationPoint(0F, 0F, 0F);
        baseB.setTextureSize(256, 128);
        baseB.mirror = true;
        setRotation(baseB, -0.8922867F, 0F, 0F);

        final RendererModel baseM = new RendererModel(this, 0, 47);
        baseM.addBox(-4.5F, -7.2F, -7.5F, 9, 2, 8);
        baseM.setRotationPoint(0F, 0F, 0F);
        baseM.setTextureSize(256, 128);
        baseM.mirror = true;
        setRotation(baseM, -0.8922867F, 0F, 0F);

        final RendererModel topL = new RendererModel(this, 57, 4);
        topL.addBox(2.5F, -7.5F, -4.5F, 2, 1, 5);
        topL.setRotationPoint(0F, 0F, 0F);
        topL.setTextureSize(256, 128);
        topL.mirror = true;
        setRotation(topL, 0F, 0F, 0F);

        final RendererModel topF = new RendererModel(this, 57, 0);
        topF.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 3);
        topF.setRotationPoint(0F, 0F, 0F);
        topF.setTextureSize(256, 128);
        topF.mirror = true;
        setRotation(topF, 0F, 0F, 0F);

        final RendererModel botL = new RendererModel(this, 57, 14);
        botL.addBox(1.5F, -5.5F, -1.5F, 3, 2, 1);
        botL.setRotationPoint(0F, 0F, 0F);
        botL.setTextureSize(256, 128);
        botL.mirror = true;
        setRotation(botL, 0F, 0F, 0F);

        final RendererModel topR = new RendererModel(this, 71, 4);
        topR.addBox(-4.5F, -7.5F, -4.5F, 3, 1, 5);
        topR.setRotationPoint(0F, 0F, 0F);
        topR.setTextureSize(256, 128);
        topR.mirror = true;
        setRotation(topR, 0F, 0F, 0F);

        final RendererModel midR = new RendererModel(this, 69, 10);
        midR.addBox(-4.5F, -6.5F, -2.5F, 3, 1, 3);
        midR.setRotationPoint(0F, 0F, 0F);
        midR.setTextureSize(256, 128);
        midR.mirror = true;
        setRotation(midR, 0F, 0F, 0F);

        final RendererModel midL = new RendererModel(this, 57, 10);
        midL.addBox(1.5F, -6.5F, -2.5F, 3, 1, 3);
        midL.setRotationPoint(0F, 0F, 0F);
        midL.setTextureSize(256, 128);
        midL.mirror = true;
        setRotation(midL, 0F, 0F, 0F);

        bipedBody.addChild(chest);

        bipedHead.addChild(ponyTailB);
        bipedHead.addChild(ponyTailT);

        bipedHead.addChild(topL);
        bipedHead.addChild(topF);
        bipedHead.addChild(topR);
        bipedHead.addChild(botL);
        bipedHead.addChild(midR);
        bipedHead.addChild(midL);
        bipedHead.addChild(lipR);
        bipedHead.addChild(lipT);
        bipedHead.addChild(lipL);
        bipedHead.addChild(lipB);

        bipedHead.addChild(baseT);
        bipedHead.addChild(baseB);
        bipedHead.addChild(baseM);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
