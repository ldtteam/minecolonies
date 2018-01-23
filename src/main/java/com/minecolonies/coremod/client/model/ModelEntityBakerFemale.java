package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

/**
 * Class used for the baker entity model.
 */
public class ModelEntityBakerFemale extends ModelBiped
{

    public ModelEntityBakerFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

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

        final ModelRenderer chest = new ModelRenderer(this, 17, 32);
        chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(256, 128);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

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

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        final ModelRenderer ponyTailB = new ModelRenderer(this, 33, 6);
        ponyTailB.addBox(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        ponyTailB.setRotationPoint(0F, 0F, 0F);
        ponyTailB.setTextureSize(256, 128);
        ponyTailB.mirror = true;
        setRotation(ponyTailB, 0.1047198F, 0F, 0F);

        final ModelRenderer ponyTailT = new ModelRenderer(this, 32, 0);
        ponyTailT.addBox(-1F, -2.2F, 3.5F, 2, 5, 1);
        ponyTailT.setRotationPoint(0F, 0F, 0F);
        ponyTailT.setTextureSize(256, 128);
        ponyTailT.mirror = true;
        setRotation(ponyTailT, 0.2268928F, 0F, 0F);

        final ModelRenderer lipR = new ModelRenderer(this, 22, 70);
        lipR.addBox(2F, -6.2F, -7.5F, 2, 1, 9);
        lipR.setRotationPoint(0F, 0F, 0F);
        lipR.setTextureSize(256, 128);
        lipR.mirror = true;
        setRotation(lipR, -0.18422253F, -0.8753831F, -1.2905022F);

        final ModelRenderer baseT = new ModelRenderer(this, 0, 40);
        baseT.addBox(-4.5F, -8.2F, -6.5F, 9, 1, 6);
        baseT.setRotationPoint(0F, 0F, 0F);
        baseT.setTextureSize(256, 128);
        baseT.mirror = true;
        setRotation(baseT, -0.8922867F, 0F, 0F);

        final ModelRenderer lipT = new ModelRenderer(this, 0, 67);
        lipT.addBox(-5F, -9.2F, -1F, 10, 1, 2);
        lipT.setRotationPoint(0F, 0F, 0F);
        lipT.setTextureSize(256, 128);
        lipT.mirror = true;
        setRotation(lipT, 0.2230717F, 0F, 0F);

        final ModelRenderer lipL = new ModelRenderer(this, 0, 70);
        lipL.addBox(-4F, -6.2F, -7.5F, 2, 1, 9);
        lipL.setRotationPoint(0F, 0F, 0F);
        lipL.setTextureSize(256, 128);
        lipL.mirror = true;
        setRotation(lipL, -0.18435909F, 0.87547284F, 1.2903719F);

        final ModelRenderer lipB = new ModelRenderer(this, 0, 80);
        lipB.addBox(-5F, -5.1F, -1.5F, 10, 1, 2);
        lipB.setRotationPoint(0F, 0F, 0F);
        lipB.setTextureSize(256, 128);
        lipB.mirror = true;
        setRotation(lipB, -1.375609F, 0F, 0F);

        final ModelRenderer baseB = new ModelRenderer(this, 0, 57);
        baseB.addBox(-5F, -5.2F, -8F, 10, 1, 9);
        baseB.setRotationPoint(0F, 0F, 0F);
        baseB.setTextureSize(256, 128);
        baseB.mirror = true;
        setRotation(baseB, -0.8922867F, 0F, 0F);

        final ModelRenderer baseM = new ModelRenderer(this, 0, 47);
        baseM.addBox(-4.5F, -7.2F, -7.5F, 9, 2, 8);
        baseM.setRotationPoint(0F, 0F, 0F);
        baseM.setTextureSize(256, 128);
        baseM.mirror = true;
        setRotation(baseM, -0.8922867F, 0F, 0F);

        final ModelRenderer topL = new ModelRenderer(this, 57, 4);
        topL.addBox(2.5F, -7.5F, -4.5F, 2, 1, 5);
        topL.setRotationPoint(0F, 0F, 0F);
        topL.setTextureSize(256, 128);
        topL.mirror = true;
        setRotation(topL, 0F, 0F, 0F);

        final ModelRenderer topF = new ModelRenderer(this, 57, 0);
        topF.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 3);
        topF.setRotationPoint(0F, 0F, 0F);
        topF.setTextureSize(256, 128);
        topF.mirror = true;
        setRotation(topF, 0F, 0F, 0F);

        final ModelRenderer botL = new ModelRenderer(this, 57, 14);
        botL.addBox(1.5F, -5.5F, -1.5F, 3, 2, 1);
        botL.setRotationPoint(0F, 0F, 0F);
        botL.setTextureSize(256, 128);
        botL.mirror = true;
        setRotation(botL, 0F, 0F, 0F);

        final ModelRenderer topR = new ModelRenderer(this, 71, 4);
        topR.addBox(-4.5F, -7.5F, -4.5F, 3, 1, 5);
        topR.setRotationPoint(0F, 0F, 0F);
        topR.setTextureSize(256, 128);
        topR.mirror = true;
        setRotation(topR, 0F, 0F, 0F);

        final ModelRenderer midR = new ModelRenderer(this, 69, 10);
        midR.addBox(-4.5F, -6.5F, -2.5F, 3, 1, 3);
        midR.setRotationPoint(0F, 0F, 0F);
        midR.setTextureSize(256, 128);
        midR.mirror = true;
        setRotation(midR, 0F, 0F, 0F);

        final ModelRenderer midL = new ModelRenderer(this, 57, 10);
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

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
