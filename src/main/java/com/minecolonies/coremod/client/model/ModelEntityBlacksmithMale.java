package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBlacksmithMale extends CitizenModel
{
    public ModelEntityBlacksmithMale()
    {
        RendererModel beardLM;
        RendererModel beardB;
        RendererModel beardMMB;
        RendererModel beardMBB;
        RendererModel beardBBB1;
        RendererModel beardBBB2;
        RendererModel beardMB;
        RendererModel moustacheLB;
        RendererModel beardRM;
        RendererModel beardTM;
        RendererModel beardTT;
        RendererModel moustacheTM;
        RendererModel moustacheLM;
        RendererModel moustacheRM;
        RendererModel moustacheRB;
        RendererModel hairTMB1;
        RendererModel hairTMB2;
        RendererModel hairTMB3;
        RendererModel hairTMB4;
        RendererModel hairTMB5;
        RendererModel hairTMB6;

        RendererModel hairM;
        RendererModel hairMM;
        RendererModel hairTFFF1;
        RendererModel hairTF1;
        RendererModel hairTFF;
        RendererModel hairTB1;
        RendererModel hairTM;
        RendererModel hairTFFF2;
        RendererModel hairTF2;
        RendererModel hairTBB;
        RendererModel hairTB2;
        RendererModel hairTFFF3;
        RendererModel hairTFFF4;

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 11, 4);
        bipedRightArm.setRotationPoint(-5F, 6F, 0F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 11, 4);
        bipedLeftArm.setRotationPoint(5F, 6F, 0F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 10, 4);
        bipedRightLeg.setRotationPoint(-2F, 14F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 10, 4);
        bipedLeftLeg.setRotationPoint(2F, 14F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 10, 4);
        bipedBody.setRotationPoint(0F, 4F, 0F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -4F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 4F, 0F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        beardLM = new RendererModel(this, 57, 0);
        beardLM.addBox(1.5F, -1.5F, -4.5F, 3, 1, 3);
        beardLM.setRotationPoint(0F, 4F, 0F);
        beardLM.setTextureSize(256, 128);
        beardLM.mirror = true;
        setRotation(beardLM, 0F, 0F, 0F);

        beardB = new RendererModel(this, 57, 0);
        beardB.addBox(-0.5F, 5.5F, -4.5F, 1, 1, 1);
        beardB.setRotationPoint(0F, 4F, 0F);
        beardB.setTextureSize(256, 128);
        beardB.mirror = true;
        setRotation(beardB, 0F, 0F, 0F);

        beardMMB = new RendererModel(this, 57, 0);
        beardMMB.addBox(-2.5F, 1.5F, -4.5F, 5, 1, 1);
        beardMMB.setRotationPoint(0F, 4F, 0F);
        beardMMB.setTextureSize(256, 128);
        beardMMB.mirror = true;
        setRotation(beardMMB, 0F, 0F, 0F);

        beardMBB = new RendererModel(this, 57, 0);
        beardMBB.addBox(-3.5F, 0.5F, -4.5F, 7, 1, 1);
        beardMBB.setRotationPoint(0F, 4F, 0F);
        beardMBB.setTextureSize(256, 128);
        beardMBB.mirror = true;
        setRotation(beardMBB, 0F, 0F, 0F);

        beardBBB1 = new RendererModel(this, 57, 11);
        beardBBB1.addBox(-1.5F, 2.5F, -5F, 3, 1, 2);
        beardBBB1.setRotationPoint(0F, 5F, 0F);
        beardBBB1.setTextureSize(256, 128);
        beardBBB1.mirror = true;
        setRotation(beardBBB1, 0F, 0F, 0F);

        beardBBB2 = new RendererModel(this, 57, 0);
        beardBBB2.addBox(-1F, 2.5F, -4.5F, 2, 3, 1);
        beardBBB2.setRotationPoint(0F, 4F, 0F);
        beardBBB2.setTextureSize(256, 128);
        beardBBB2.mirror = true;
        setRotation(beardBBB2, 0F, 0F, 0F);

        beardMB = new RendererModel(this, 57, 0);
        beardMB.addBox(-4.5F, -0.5F, -4.5F, 9, 1, 2);
        beardMB.setRotationPoint(0F, 4F, 0F);
        beardMB.setTextureSize(256, 128);
        beardMB.mirror = true;
        setRotation(beardMB, 0F, 0F, 0F);

        moustacheLB = new RendererModel(this, 83, 0);
        moustacheLB.addBox(2.7F, 1F, -5F, 1, 3, 1);
        moustacheLB.setRotationPoint(0F, 4F, 0F);
        moustacheLB.setTextureSize(256, 128);
        moustacheLB.mirror = true;
        setRotation(moustacheLB, 0F, 0F, 0F);

        beardRM = new RendererModel(this, 57, 0);
        beardRM.addBox(-4.5F, -1.5F, -4.5F, 3, 1, 3);
        beardRM.setRotationPoint(0F, 4F, 0F);
        beardRM.setTextureSize(256, 128);
        beardRM.mirror = true;
        setRotation(beardRM, 0F, 0F, 0F);

        beardTM = new RendererModel(this, 57, 0);
        beardTM.addBox(-4.5F, -2.5F, -4.5F, 9, 1, 4);
        beardTM.setRotationPoint(0F, 4F, 0F);
        beardTM.setTextureSize(256, 128);
        beardTM.mirror = true;
        setRotation(beardTM, 0F, 0F, 0F);

        beardTT = new RendererModel(this, 57, 0);
        beardTT.addBox(-3.5F, -3.5F, -4.5F, 7, 1, 3);
        beardTT.setRotationPoint(0F, 4F, 0F);
        beardTT.setTextureSize(256, 128);
        beardTT.mirror = true;
        setRotation(beardTT, 0F, 0F, 0F);

        moustacheTM = new RendererModel(this, 84, 0);
        moustacheTM.addBox(-2.5F, -3F, -5F, 5, 1, 1);
        moustacheTM.setRotationPoint(0F, 4F, 0F);
        moustacheTM.setTextureSize(256, 128);
        moustacheTM.mirror = true;
        setRotation(moustacheTM, 0F, 0F, 0F);

        moustacheLM = new RendererModel(this, 86, 0);
        moustacheLM.addBox(2F, -2F, -5F, 1, 3, 1);
        moustacheLM.setRotationPoint(0F, 4F, 0F);
        moustacheLM.setTextureSize(256, 128);
        moustacheLM.mirror = true;
        setRotation(moustacheLM, 0F, 0F, 0F);

        moustacheRM = new RendererModel(this, 85, 0);
        moustacheRM.addBox(-3F, -2F, -5F, 1, 3, 1);
        moustacheRM.setRotationPoint(0F, 4F, 0F);
        moustacheRM.setTextureSize(256, 128);
        moustacheRM.mirror = true;
        setRotation(moustacheRM, 0F, 0F, 0F);

        moustacheRB = new RendererModel(this, 84, 0);
        moustacheRB.addBox(-3.733333F, 1F, -5F, 1, 3, 1);
        moustacheRB.setRotationPoint(0F, 4F, 0F);
        moustacheRB.setTextureSize(256, 128);
        moustacheRB.mirror = true;
        setRotation(moustacheRB, 0F, 0F, 0F);

        hairTMB1 = new RendererModel(this, 57, 1);
        hairTMB1.addBox(1.5F, -2.5F, 3.5F, 1, 2, 1);
        hairTMB1.setRotationPoint(0F, 8F, 0F);
        hairTMB1.setTextureSize(256, 128);
        hairTMB1.mirror = true;
        setRotation(hairTMB1, 0F, 0F, 0F);

        hairM = new RendererModel(this, 57, 0);
        hairM.addBox(-4.5F, -3.5F, -2.5F, 9, 1, 4);
        hairM.setRotationPoint(0F, 4F, 0F);
        hairM.setTextureSize(256, 128);
        hairM.mirror = true;
        setRotation(hairM, 0F, 0F, 0F);

        hairMM = new RendererModel(this, 57, 0);
        hairMM.addBox(-4.5F, -6.5F, -0.5F, 9, 1, 1);
        hairMM.setRotationPoint(0F, 4F, 0F);
        hairMM.setTextureSize(256, 128);
        hairMM.mirror = true;
        setRotation(hairMM, 0F, 0F, 0F);

        hairTFFF1 = new RendererModel(this, 57, 3);
        hairTFFF1.addBox(3.3F, -7.5F, -4.5F, 1, 2, 1);
        hairTFFF1.setRotationPoint(0F, 4F, 0F);
        hairTFFF1.setTextureSize(256, 128);
        hairTFFF1.mirror = true;
        setRotation(hairTFFF1, 0F, 0F, 0F);

        hairTF1 = new RendererModel(this, 57, 2);
        hairTF1.addBox(-3.5F, -9.15F, -3.75F, 7, 2, 7);
        hairTF1.setRotationPoint(0F, 4F, 0F);
        hairTF1.setTextureSize(256, 128);
        hairTF1.mirror = true;
        setRotation(hairTF1, 0F, 0F, 0F);

        hairTFF = new RendererModel(this, 57, 0);
        hairTFF.addBox(-4.5F, -8.5F, -2.5F, 9, 2, 1);
        hairTFF.setRotationPoint(0F, 4F, 0F);
        hairTFF.setTextureSize(256, 128);
        hairTFF.mirror = true;
        setRotation(hairTFF, 0F, 0F, 0F);

        hairTB1 = new RendererModel(this, 57, 0);
        hairTB1.addBox(-4.5F, -8.5F, 2.5F, 9, 1, 1);
        hairTB1.setRotationPoint(0F, 4F, 0F);
        hairTB1.setTextureSize(256, 128);
        hairTB1.mirror = true;
        setRotation(hairTB1, 0F, 0F, 0F);

        hairTM = new RendererModel(this, 57, 0);
        hairTM.addBox(-4.5F, -4.5F, -0.5F, 9, 1, 3);
        hairTM.setRotationPoint(0F, 4F, 0F);
        hairTM.setTextureSize(256, 128);
        hairTM.mirror = true;
        setRotation(hairTM, 0F, 0F, 0F);

        hairTFFF2 = new RendererModel(this, 57, 3);
        hairTFFF2.addBox(-4.5F, -8.5F, -3.5F, 9, 2, 1);
        hairTFFF2.setRotationPoint(0F, 4F, 0F);
        hairTFFF2.setTextureSize(256, 128);
        hairTFFF2.mirror = true;
        setRotation(hairTFFF2, 0F, 0F, 0F);

        hairTF2 = new RendererModel(this, 57, 4);
        hairTF2.addBox(-4.5F, -8.5F, -1.5F, 9, 2, 2);
        hairTF2.setRotationPoint(0F, 4F, 0F);
        hairTF2.setTextureSize(256, 128);
        hairTF2.mirror = true;
        setRotation(hairTF2, 0F, 0F, 0F);

        hairTMB2 = new RendererModel(this, 57, 0);
        hairTMB2.addBox(-3.5F, -4.5F, 3.5F, 7, 1, 1);
        hairTMB2.setRotationPoint(0F, 8F, 0F);
        hairTMB2.setTextureSize(256, 128);
        hairTMB2.mirror = true;
        setRotation(hairTMB2, 0F, 0F, 0F);

        hairTMB3 = new RendererModel(this, 57, 2);
        hairTMB3.addBox(-2.5F, -2.5F, 3.5F, 1, 1, 1);
        hairTMB3.setRotationPoint(0F, 8F, 0F);
        hairTMB3.setTextureSize(256, 128);
        hairTMB3.mirror = true;
        setRotation(hairTMB3, 0F, 0F, 0F);

        hairTMB4 = new RendererModel(this, 57, 0);
        hairTMB4.addBox(0.5F, -3.5F, 3.5F, 3, 1, 1);
        hairTMB4.setRotationPoint(0F, 8F, 0F);
        hairTMB4.setTextureSize(256, 128);
        hairTMB4.mirror = true;
        setRotation(hairTMB4, 0F, 0F, 0F);

        hairTMB5 = new RendererModel(this, 56, 0);
        hairTMB5.addBox(-3.5F, -3.5F, 3.5F, 2, 1, 1);
        hairTMB5.setRotationPoint(0F, 8F, 0F);
        hairTMB5.setTextureSize(256, 128);
        hairTMB5.mirror = true;
        setRotation(hairTMB5, 0F, 0F, 0F);

        hairTMB6 = new RendererModel(this, 57, 0);
        hairTMB6.addBox(-4.5F, -5.5F, 3.5F, 9, 1, 1);
        hairTMB6.setRotationPoint(0F, 8F, 0F);
        hairTMB6.setTextureSize(256, 128);
        hairTMB6.mirror = true;
        setRotation(hairTMB6, 0F, 0F, 0F);

        hairTBB = new RendererModel(this, 57, 0);
        hairTBB.addBox(-4.5F, -11.5F, 2.5F, 9, 6, 2);
        hairTBB.setRotationPoint(0F, 8F, 0F);
        hairTBB.setTextureSize(256, 128);
        hairTBB.mirror = true;
        setRotation(hairTBB, 0F, 0F, 0F);

        hairTB2 = new RendererModel(this, 57, 0);
        hairTB2.addBox(-4.5F, -8.5F, 0.5F, 9, 4, 2);
        hairTB2.setRotationPoint(0F, 4F, 0F);
        hairTB2.setTextureSize(256, 128);
        hairTB2.mirror = true;
        setRotation(hairTB2, 0F, 0F, 0F);

        hairTFFF3 = new RendererModel(this, 57, 3);
        hairTFFF3.addBox(-4F, -8.5F, -4.5F, 8, 1, 1);
        hairTFFF3.setRotationPoint(0F, 4F, 0F);
        hairTFFF3.setTextureSize(256, 128);
        hairTFFF3.mirror = true;
        setRotation(hairTFFF3, 0F, 0F, 0F);

        hairTFFF4 = new RendererModel(this, 57, 3);
        hairTFFF4.addBox(-4.2F, -7.5F, -4.5F, 1, 1, 1);
        hairTFFF4.setRotationPoint(0F, 4F, 0F);
        hairTFFF4.setTextureSize(256, 128);
        hairTFFF4.mirror = true;
        setRotation(hairTFFF4, 0F, 0F, 0F);

        this.bipedHeadwear.addChild(beardLM);
        this.bipedHeadwear.addChild(beardB);
        this.bipedHeadwear.addChild(beardMMB);
        this.bipedHeadwear.addChild(beardMBB);
        this.bipedHeadwear.addChild(beardBBB1);
        this.bipedHeadwear.addChild(beardBBB2);
        this.bipedHeadwear.addChild(beardMB);
        this.bipedHeadwear.addChild(moustacheLB);
        this.bipedHeadwear.addChild(beardRM);
        this.bipedHeadwear.addChild(beardTM);
        this.bipedHeadwear.addChild(beardTT);

        this.bipedHeadwear.addChild(moustacheTM);
        this.bipedHeadwear.addChild(moustacheLM);
        this.bipedHeadwear.addChild(moustacheRM);
        this.bipedHeadwear.addChild(moustacheRB);

        this.bipedHeadwear.addChild(hairTMB1);
        this.bipedHeadwear.addChild(hairTMB2);
        this.bipedHeadwear.addChild(hairTMB3);
        this.bipedHeadwear.addChild(hairTMB4);
        this.bipedHeadwear.addChild(hairTMB5);
        this.bipedHeadwear.addChild(hairTMB6);

        this.bipedHeadwear.addChild(hairM);
        this.bipedHeadwear.addChild(hairMM);
        this.bipedHeadwear.addChild(hairTFFF1);
        this.bipedHeadwear.addChild(hairTF1);
        this.bipedHeadwear.addChild(hairTFF);
        this.bipedHeadwear.addChild(hairTB1);

        this.bipedHeadwear.addChild(hairTM);
        this.bipedHeadwear.addChild(hairTFFF2);
        this.bipedHeadwear.addChild(hairTF2);
        this.bipedHeadwear.addChild(hairTBB);
        this.bipedHeadwear.addChild(hairTB2);
        this.bipedHeadwear.addChild(hairTFFF3);
        this.bipedHeadwear.addChild(hairTFFF4);
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

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
