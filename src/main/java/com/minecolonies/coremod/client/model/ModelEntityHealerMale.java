package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityHealerMale extends CitizenModel<AbstractEntityCitizen>
{
  public ModelEntityHealerMale()
  {
      ModelRenderer button;
      ModelRenderer rightlegshoe;
      ModelRenderer leftarmcoat;
      ModelRenderer rightarmcoat;
      ModelRenderer rightarmGlove;
      ModelRenderer chest;
      ModelRenderer leftlegshoe;
      ModelRenderer leftarmGlove;
      ModelRenderer belt;
      ModelRenderer MaskL2;
      ModelRenderer MaskTop;
      ModelRenderer MaskL3;
      ModelRenderer MaskL1;
      ModelRenderer MaskR3;
      ModelRenderer MaskR1;
      ModelRenderer MaskR2;
      ModelRenderer MaskBottom;
      ModelRenderer maskFace;
      ModelRenderer eyeL;
      ModelRenderer eyeR;
      ModelRenderer maskFace1;
      ModelRenderer maskFace2;
      ModelRenderer maskFace3;
      ModelRenderer maskFace4;
      ModelRenderer BrimB;
      ModelRenderer BrimL;
      ModelRenderer MidMid;
      ModelRenderer BrimR;
      ModelRenderer BrimF;
      ModelRenderer Brim;
      ModelRenderer MidT;
      ModelRenderer MidB;
      ModelRenderer BeakBottom;
      ModelRenderer BeakEnd2;
      ModelRenderer BeakTop;
      ModelRenderer BeakEnd1;
      ModelRenderer BeakEnd3;

      textureWidth = 128;
      textureHeight = 64;
    
      button = new ModelRenderer(this, 90, 48);
      button.addBox(0F, 0F, 0F, 1, 1, 1);
      button.setRotationPoint(2F, 10.3F, -4.2F);
      button.setTextureSize(128, 64);
      setRotation(button, 0F, 0F, 0F);

      bipedBody = new ModelRenderer(this, 16, 16);
      bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
      bipedBody.setRotationPoint(0F, 0F, 0F);
      bipedBody.setTextureSize(128, 64);
      setRotation(bipedBody, 0F, 0F, 0F);

      bipedLeftArm = new ModelRenderer(this, 40, 16);
      bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
      bipedLeftArm.setRotationPoint(5F, 2F, 0F);
      bipedLeftArm.setTextureSize(128, 64);
      bipedLeftArm.mirror = true;
      setRotation(bipedLeftArm, 0F, 0F, 0F);

      bipedRightLeg = new ModelRenderer(this, 0, 16);
      bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
      bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
      bipedRightLeg.setTextureSize(128, 64);
      bipedRightLeg.mirror = true;
      setRotation(bipedRightLeg, 0F, 0F, 0F);

      bipedLeftLeg = new ModelRenderer(this, 0, 16);
      bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
      bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
      bipedLeftLeg.setTextureSize(128, 64);
      bipedLeftLeg.mirror = true;
      setRotation(bipedLeftLeg, 0F, 0F, 0F);

      bipedHead = new ModelRenderer(this, 0, 0);
      bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
      bipedHead.setRotationPoint(0F, 0F, 0F);
      bipedHead.setTextureSize(128, 64);
      bipedHead.mirror = true;
      setRotation(bipedHead, 0F, 0F, 0F);

      bipedRightArm = new ModelRenderer(this, 40, 16);
      bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
      bipedRightArm.setRotationPoint(-5F, 2F, 0F);
      bipedRightArm.setTextureSize(128, 64);
      bipedRightArm.mirror = true;
      setRotation(bipedRightArm, 0F, 0F, 0F);

      leftarmcoat = new ModelRenderer(this, 56, 16);
      leftarmcoat.addBox(-1.5F, -2F, -2.5F, 8, 7, 5);
      leftarmcoat.setRotationPoint(2F, 1F, 0F);
      leftarmcoat.setTextureSize(128, 64);
      leftarmcoat.mirror = true;
      setRotation(leftarmcoat, 0F, 0F, 0F);

      rightarmcoat = new ModelRenderer(this, 56, 16);
      rightarmcoat.addBox(-3.5F, 3F, -2.5F, 8, 7, 5);
      rightarmcoat.setRotationPoint(-5F, -4F, 0F);
      rightarmcoat.setTextureSize(128, 64);
      rightarmcoat.mirror = true;
      setRotation(rightarmcoat, 0F, 0F, 0F);

      rightarmGlove = new ModelRenderer(this, 56, 16);
      rightarmGlove.addBox(1.5F, 1F, -2.5F, 5, 4, 5);
      rightarmGlove.setRotationPoint(-5F, 6F, 0F);
      rightarmGlove.setTextureSize(128, 64);
      rightarmGlove.mirror = true;
      setRotation(rightarmGlove, 0F, 0F, 0F);

      leftarmGlove = new ModelRenderer(this, 56, 16);
      leftarmGlove.addBox(-8.5F, 1F, -2.5F, 5, 4, 5);
      leftarmGlove.setRotationPoint(7F, 6F, 0F);
      leftarmGlove.setTextureSize(128, 64);
      leftarmGlove.mirror = true;
      setRotation(leftarmGlove, 0F, 0F, 0F);

      chest = new ModelRenderer(this, 18, 35);
      chest.addBox(-3.5F, 3F, -2.5F, 2, 3, 2);
      chest.setRotationPoint(5F, 7F, -1.5F);
      chest.setTextureSize(128, 64);
      chest.mirror = true;
      setRotation(chest, 0F, 0F, 0F);

      leftlegshoe = new ModelRenderer(this, 0, 32);
      leftlegshoe.addBox(-5.5F, -9F, -2.5F, 5, 4, 5);
      leftlegshoe.setRotationPoint(3F, 17F, 0F);
      leftlegshoe.setTextureSize(128, 64);
      leftlegshoe.mirror = true;
      setRotation(leftlegshoe, 0F, 0F, 0F);
      //bootR.addBox(-0.5F, -8F, -2.5F, 5, 2, 5);

      rightlegshoe = new ModelRenderer(this, 0, 32);
      rightlegshoe.addBox(-1.5F, -9F, -2.5F, 5, 4, 5);
      rightlegshoe.setRotationPoint(-1F, 17F, 0F);
      rightlegshoe.setTextureSize(128, 64);
      rightlegshoe.mirror = true;
      setRotation(rightlegshoe, 0F, 0F, 0F);
      //bootL.addBox(-4.5F, -8F, -2.5F, 5, 2, 5);

      belt = new ModelRenderer(this, 0, 32);
      belt.addBox(-3.5F, 3F, -2.5F, 9, 2, 6);
      belt.setRotationPoint(-1F, 8F, -0.5F);
      belt.setTextureSize(128, 64);
      belt.mirror = true;
      setRotation(belt, 0F, 0F, 0F);

      MaskL2 = new ModelRenderer(this, 55, 0);
      MaskL2.addBox(3.5F, -5.5F, -4.5F, 1, 3, 5);
      MaskL2.setRotationPoint(0F, 0F, 0F);
      MaskL2.setTextureSize(128, 64);
      MaskL2.mirror = true;
      setRotation(MaskL2, 0F, 0F, 0F);

      MaskTop = new ModelRenderer(this, 55, 0);
      MaskTop.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 3);
      MaskTop.setRotationPoint(0F, 0.2F, 0F);
      MaskTop.setTextureSize(128, 64);
      MaskTop.mirror = true;
      setRotation(MaskTop, 0F, 0F, 0F);

      MaskL3 = new ModelRenderer(this, 55, 0);
      MaskL3.addBox(3.5F, -7.5F, -4.5F, 1, 2, 4);
      MaskL3.setRotationPoint(0F, 0F, 0F);
      MaskL3.setTextureSize(128, 64);
      MaskL3.mirror = true;
      setRotation(MaskL3, 0F, 0F, 0F);

      MaskL1 = new ModelRenderer(this, 55, 0);
      MaskL1.addBox(3.5F, -2.5F, -4.5F, 1, 2, 4);
      MaskL1.setRotationPoint(0F, 0F, 0F);
      MaskL1.setTextureSize(128, 64);
      MaskL1.mirror = true;
      setRotation(MaskL1, 0F, 0F, 0F);

      MaskR3 = new ModelRenderer(this, 55, 0);
      MaskR3.addBox(-4.5F, -7.5F, -4.5F, 1, 2, 4);
      MaskR3.setRotationPoint(0F, 0F, 0F);
      MaskR3.setTextureSize(128, 64);
      MaskR3.mirror = true;
      setRotation(MaskR3, 0F, 0F, 0F);

      MaskR1 = new ModelRenderer(this, 55, 0);
      MaskR1.addBox(-4.5F, -2.5F, -4.5F, 1, 2, 4);
      MaskR1.setRotationPoint(0F, 0F, 0F);
      MaskR1.setTextureSize(128, 64);
      MaskR1.mirror = true;
      setRotation(MaskR1, 0F, 0F, 0F);

      MaskR2 = new ModelRenderer(this, 55, 0);
      MaskR2.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 5);
      MaskR2.setRotationPoint(0F, 0F, 0F);
      MaskR2.setTextureSize(128, 64);
      MaskR2.mirror = true;
      setRotation(MaskR2, 0F, 0F, 0F);

      MaskBottom = new ModelRenderer(this, 55, 0);
      MaskBottom.addBox(-4.5F, -0.5F, -4.5F, 9, 1, 3);
      MaskBottom.setRotationPoint(0F, 0F, 0F);
      MaskBottom.setTextureSize(128, 64);
      MaskBottom.mirror = true;
      setRotation(MaskBottom, 0F, 0F, 0F);

      maskFace = new ModelRenderer(this, 55, 0);
      maskFace.addBox(-4F, -7.5F, -5F, 8, 3, 1);
      maskFace.setRotationPoint(0F, 4F, 0F);
      maskFace.setTextureSize(128, 64);
      maskFace.mirror = true;
      setRotation(maskFace, 0F, 0F, 0F);

      eyeL = new ModelRenderer(this, 33, 1);
      eyeL.addBox(-3.2F, -6.5F, -5.5F, 2, 2, 1);
      eyeL.setRotationPoint(0.2F, 1F, 0F);
      eyeL.setTextureSize(128, 64);
      eyeL.mirror = true;
      setRotation(eyeL, 0F, 0F, 0F);

      eyeR = new ModelRenderer(this, 33, 1);
      eyeR.addBox(1.2F, -6.5F, -5.5F, 2, 2, 1);
      eyeR.setRotationPoint(-0.2F, 1F, 0F);
      eyeR.setTextureSize(128, 64);
      eyeR.mirror = true;
      setRotation(eyeR, 0F, 0F, 0F);

      maskFace1 = new ModelRenderer(this, 55, 0);
      maskFace1.addBox(-4F, -7.5F, -5F, 1, 2, 1);
      maskFace1.setRotationPoint(7F, 2F, 0F);
      maskFace1.setTextureSize(128, 64);
      maskFace1.mirror = true;
      setRotation(maskFace1, 0F, 0F, 0F);

      maskFace2 = new ModelRenderer(this, 55, 0);
      maskFace2.addBox(-4F, -7.5F, -5F, 8, 2, 1);
      maskFace2.setRotationPoint(0F, 0F, 0F);
      maskFace2.setTextureSize(128, 64);
      maskFace2.mirror = true;
      setRotation(maskFace2, 0F, 0F, 0F);

      maskFace3 = new ModelRenderer(this, 55, 0);
      maskFace3.addBox(-4F, -7.5F, -5F, 1, 2, 1);
      maskFace3.setRotationPoint(0F, 2F, 0F);
      maskFace3.setTextureSize(128, 64);
      maskFace3.mirror = true;
      setRotation(maskFace3, 0F, 0F, 0F);

      maskFace4 = new ModelRenderer(this, 55, 0);
      maskFace4.addBox(-4F, -7.5F, -5F, 2, 2, 1);
      maskFace4.setRotationPoint(3F, 2F, 0F);
      maskFace4.setTextureSize(128, 64);
      maskFace4.mirror = true;
      setRotation(maskFace4, 0F, 0F, 0F);

      BrimB = new ModelRenderer(this, 2, 46);
      BrimB.addBox(-4.5F, -9F, 4.5F, 9, 1, 1);
      BrimB.setRotationPoint(0F, 0F, 0F);
      BrimB.setTextureSize(128, 64);
      BrimB.mirror = true;
      setRotation(BrimB, -0.0698132F, 0F, 0F);

      BrimL = new ModelRenderer(this, 2, 46);
      BrimL.addBox(5F, -9F, -4F, 1, 1, 8);
      BrimL.setRotationPoint(0F, 0F, 0F);
      BrimL.setTextureSize(128, 64);
      BrimL.mirror = true;
      setRotation(BrimL, -0.0698132F, 0F, 0F);

      MidMid = new ModelRenderer(this, 2, 46);
      MidMid.addBox(-3.5F, -11F, -3.5F, 7, 1, 7);
      MidMid.setRotationPoint(0F, 0F, 0F);
      MidMid.setTextureSize(128, 64);
      MidMid.mirror = true;
      setRotation(MidMid, -0.0698132F, 0F, 0F);

      BrimR = new ModelRenderer(this, 2, 46);
      BrimR.addBox(-6F, -9F, -4F, 1, 1, 8);
      BrimR.setRotationPoint(0F, 0F, 0F);
      BrimR.setTextureSize(128, 64);
      BrimR.mirror = true;
      setRotation(BrimR, -0.0698132F, 0F, 0F);

      BrimF = new ModelRenderer(this, 2, 46);
      BrimF.addBox(-4.5F, -9F, -5.5F, 9, 1, 1);
      BrimF.setRotationPoint(0F, 0F, 0F);
      BrimF.setTextureSize(128, 64);
      BrimF.mirror = true;
      setRotation(BrimF, -0.0698132F, 0F, 0F);

      Brim = new ModelRenderer(this, 2, 46);
      Brim.addBox(-5F, -9F, -4.5F, 10, 1, 9);
      Brim.setRotationPoint(0F, 0F, 0F);
      Brim.setTextureSize(128, 64);
      Brim.mirror = true;
      setRotation(Brim, -0.0698132F, 0F, 0F);

      MidT = new ModelRenderer(this, 2, 46);
      MidT.addBox(-4F, -12F, -4F, 8, 1, 8);
      MidT.setRotationPoint(0F, 0F, 0F);
      MidT.setTextureSize(128, 64);
      MidT.mirror = true;
      setRotation(MidT, -0.0698132F, 0F, 0F);

      MidB = new ModelRenderer(this, 0, 32);
      MidB.addBox(-4F, -10F, -4F, 8, 1, 8);
      MidB.setRotationPoint(0F, 0F, 0F);
      MidB.setTextureSize(128, 64);
      MidB.mirror = true;
      setRotation(MidB, -0.0698132F, 0F, 0F);

      BeakBottom = new ModelRenderer(this, 55, 0);
      BeakBottom.addBox(-1F, -3.4F, -7.2F, 2, 1, 4);
      BeakBottom.setRotationPoint(0F, 0F, 0F);
      BeakBottom.setTextureSize(128, 64);
      BeakBottom.mirror = true;
      setRotation(BeakBottom, 0.2782199F, 0F, 0F);

      BeakEnd2 = new ModelRenderer(this, 55, 0);
      BeakEnd2.addBox(-0.5F, -6.2F, -8.5F, 1, 2, 3);
      BeakEnd2.setRotationPoint(0F, 0F, 0F);
      BeakEnd2.setTextureSize(128, 64);
      BeakEnd2.mirror = true;
      setRotation(BeakEnd2, 0.5749259F, 0F, 0F);

      BeakTop = new ModelRenderer(this, 55, 0);
      BeakTop.addBox(-1F, -5.6F, -7.5F, 2, 2, 5);
      BeakTop.setRotationPoint(0F, 0F, 0F);
      BeakTop.setTextureSize(128, 64);
      BeakTop.mirror = true;
      setRotation(BeakTop, 0.400393F, 0F, 0F);

      BeakEnd1 = new ModelRenderer(this, 55, 0);
      BeakEnd1.addBox(-1F, -5.7F, -8F, 2, 2, 4);
      BeakEnd1.setRotationPoint(0F, 0F, 0F);
      BeakEnd1.setTextureSize(128, 64);
      BeakEnd1.mirror = true;
      setRotation(BeakEnd1, 0.4527529F, 0F, 0F);

      BeakEnd3 = new ModelRenderer(this, 55, 0);
      BeakEnd3.addBox(-0.5F, -7.2F, -9F, 1, 1, 3);
      BeakEnd3.setRotationPoint(0F, 0F, 0F);
      BeakEnd3.setTextureSize(128, 64);
      BeakEnd3.mirror = true;
      setRotation(BeakEnd3, 0.7669121F, 0F, 0F);

      bipedBody.addChild(button);
      bipedBody.addChild(leftarmcoat);
      bipedBody.addChild(rightarmcoat);
      bipedBody.addChild(chest);
      bipedLeftLeg.addChild(leftlegshoe);
      bipedRightLeg.addChild(rightlegshoe);

      bipedRightArm.addChild(rightarmGlove);
      bipedLeftArm.addChild(leftarmGlove);

      bipedBody.addChild(belt);

      bipedHead.addChild(MaskL2);
      bipedHead.addChild(MaskTop);
      bipedHead.addChild(MaskL3);
      bipedHead.addChild(MaskL1);

      bipedHead.addChild(MaskR3);
      bipedHead.addChild(MaskR1);
      bipedHead.addChild(MaskR2);

      bipedHead.addChild(MaskBottom);
      bipedHead.addChild(maskFace);

      bipedHead.addChild(eyeL);
      bipedHead.addChild(eyeR);

      bipedHead.addChild(maskFace1);
      bipedHead.addChild(maskFace2);
      bipedHead.addChild(maskFace3);
      bipedHead.addChild(maskFace4);

      bipedHead.addChild(BrimB);
      bipedHead.addChild(BrimL);
      bipedHead.addChild(MidMid);
      bipedHead.addChild(BrimR);
      bipedHead.addChild(BrimF);
      bipedHead.addChild(Brim);
      bipedHead.addChild(MidT);
      bipedHead.addChild(MidB);

      bipedHead.addChild(BeakBottom);
      bipedHead.addChild(BeakEnd2);
      bipedHead.addChild(BeakTop);
      bipedHead.addChild(BeakEnd1);
      bipedHead.addChild(BeakEnd3);

      bipedHeadwear.showModel = false;
  }

  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
}
