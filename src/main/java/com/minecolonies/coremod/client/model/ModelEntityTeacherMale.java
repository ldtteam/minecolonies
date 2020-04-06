package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityTeacherMale extends CitizenModel<AbstractEntityCitizen>
{
  public ModelEntityTeacherMale()
  {
      textureWidth = 128;
      textureHeight = 64;

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

      bipedRightLeg = new ModelRenderer(this, 0, 16);
      bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
      bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
      bipedRightLeg.setTextureSize(128, 64);
      bipedRightLeg.mirror = true;
      setRotation(bipedRightLeg, 0F, 0F, 0F);

      bipedLeftArm = new ModelRenderer(this, 40, 16);
      bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
      bipedLeftArm.setRotationPoint(5F, 2F, 0F);
      bipedLeftArm.setTextureSize(128, 64);
      bipedLeftArm.mirror = true;
      setRotation(bipedLeftArm, 0F, 0F, 0F);

      bipedRightArm = new ModelRenderer(this, 40, 16);
      bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
      bipedRightArm.setRotationPoint(-5F, 2F, 0F);
      bipedRightArm.setTextureSize(128, 64);
      bipedRightArm.mirror = true;
      setRotation(bipedRightArm, 0F, 0F, 0F);

      bipedBody = new ModelRenderer(this, 16, 16);
      bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
      bipedBody.setRotationPoint(0F, 0F, 0F);
      bipedBody.setTextureSize(128, 64);
      setRotation(bipedBody, 0F, 0F, 0F);

      ModelRenderer backhair = new ModelRenderer(this, 0, 45);
      backhair.addBox(-2.5F, -7.5F, 3.5F, 5, 8, 1);
      backhair.setRotationPoint(0F, 0F, 0F);
      backhair.setTextureSize(128, 64);
      setRotation(backhair, 0F, 0F, 0F);

      ModelRenderer hairbackTop_2 = new ModelRenderer(this, 1, 45);
      hairbackTop_2.addBox(-4.5F, -5.5F, -0.5F, 2, 2, 6);
      hairbackTop_2.setRotationPoint(0F, 0F, -1F);
      hairbackTop_2.setTextureSize(128, 64);
      setRotation(hairbackTop_2, 0F, 0F, 0F);

      ModelRenderer hairBackTop_4 = new ModelRenderer(this, 1, 45);
      hairBackTop_4.addBox(-4.5F, -3.5F, 1.5F, 2, 4, 5);
      hairBackTop_4.setRotationPoint(0F, 0F, -2F);
      hairBackTop_4.setTextureSize(128, 64);
      setRotation(hairBackTop_4, 0F, 0F, 0F);

      ModelRenderer hairfrontTop_1 = new ModelRenderer(this, 0, 45);
      hairfrontTop_1.addBox(2.5F, -6.5F, -4.5F, 1, 1, 1);
      hairfrontTop_1.setRotationPoint(0F, 0F, 0F);
      hairfrontTop_1.setTextureSize(128, 64);
      setRotation(hairfrontTop_1, 0F, 0F, 0F);

      ModelRenderer hairfrontTop_2 = new ModelRenderer(this, 1, 45);
      hairfrontTop_2.addBox(-4.5F, -5.5F, -4.5F, 1, 3, 3);
      hairfrontTop_2.setRotationPoint(0F, 0F, 0F);
      hairfrontTop_2.setTextureSize(128, 64);
      setRotation(hairfrontTop_2, 0F, 0F, 0F);

      ModelRenderer hairfrontTop_3 = new ModelRenderer(this, 0, 45);
      hairfrontTop_3.addBox(3.5F, -6.5F, -4.5F, 1, 3, 3);
      hairfrontTop_3.setRotationPoint(0F, 0F, 0F);
      hairfrontTop_3.setTextureSize(128, 64);
      setRotation(hairfrontTop_3, 0F, 0F, 0F);

      ModelRenderer hairTop_2 = new ModelRenderer(this, 0, 45);
      hairTop_2.addBox(2.5F, -7.5F, -4.5F, 2, 1, 9);
      hairTop_2.setRotationPoint(0F, 0F, 0F);
      hairTop_2.setTextureSize(128, 64);
      setRotation(hairTop_2, 0F, 0F, 0F);

      ModelRenderer hairLeftTop_1 = new ModelRenderer(this, 0, 45);
      hairLeftTop_1.addBox(2.5F, -6.5F, -3.5F, 2, 2, 6);
      hairLeftTop_1.setRotationPoint(0F, 0F, 2F);
      hairLeftTop_1.setTextureSize(128, 64);
      setRotation(hairLeftTop_1, 0F, 0F, 0F);

      ModelRenderer hairLeftTop_4 = new ModelRenderer(this, 0, 45);
      hairLeftTop_4.addBox(2.5F, -3.5F, 1.5F, 2, 5, 5);
      hairLeftTop_4.setRotationPoint(0F, -1F, -2F);
      hairLeftTop_4.setTextureSize(128, 64);
      setRotation(hairLeftTop_4, 0F, 0F, 0F);

      ModelRenderer hairLeftTop_5 = new ModelRenderer(this, 0, 45);
      hairLeftTop_5.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8);
      hairLeftTop_5.setRotationPoint(0F, 0F, 0F);
      hairLeftTop_5.setTextureSize(128, 64);
      setRotation(hairLeftTop_5, 0F, 0F, 0F);

      ModelRenderer hairTop_1 = new ModelRenderer(this, 1, 45);
      hairTop_1.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
      hairTop_1.setRotationPoint(0F, 0F, 0F);
      hairTop_1.setTextureSize(128, 64);
      setRotation(hairTop_1, 0F, 0F, 0F);

      ModelRenderer left_top_1 = new ModelRenderer(this, 1, 45);
      left_top_1.addBox(-4.5F, -7.5F, -4.5F, 3, 2, 9);
      left_top_1.setRotationPoint(0F, 0F, 0F);
      left_top_1.setTextureSize(128, 64);
      setRotation(left_top_1, 0F, 0F, 0F);

      bipedHead.addChild(hairbackTop_2);
      bipedHead.addChild(hairBackTop_4);
      bipedHead.addChild(hairfrontTop_1);
      bipedHead.addChild(hairfrontTop_2);
      bipedHead.addChild(hairfrontTop_3);
      bipedHead.addChild(hairLeftTop_1);
      bipedHead.addChild(hairLeftTop_4);
      bipedHead.addChild(hairLeftTop_5);
      bipedHead.addChild(hairTop_1);
      bipedHead.addChild(hairTop_2);
      bipedHead.addChild(backhair);
      bipedHead.addChild(left_top_1);

      bipedHeadwear.showModel = false;
  }

  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
}
