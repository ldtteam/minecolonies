package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityComposterFemale extends ModelBiped
{
    //fields
    ModelRenderer gloveR;
    ModelRenderer gloveL;
    ModelRenderer bootR;
    ModelRenderer bootL;

    ModelRenderer chest;

    ModelRenderer hairLeftTop;
    ModelRenderer hairTop;
    public ModelEntityComposterFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        gloveR = new ModelRenderer(this, 20, 32);
        gloveR.addBox(-3.5F, 5F, -2.5F, 5, 1, 5);
        gloveR.setRotationPoint(-5F, 2F, 0F);
        gloveR.setTextureSize(128, 64);
        gloveR.mirror = true;
        setRotation(gloveR, 0F, 0F, 0F);

        gloveL = new ModelRenderer(this, 0, 32);
        gloveL.addBox(-1.5F, 5F, -2.5F, 5, 1, 5);
        gloveL.setRotationPoint(5F, 2F, 0F);
        gloveL.setTextureSize(128, 64);
        gloveL.mirror = true;
        setRotation(gloveL, 0F, 0F, 0F);

        bootR = new ModelRenderer(this, 20, 38);
        bootR.addBox(-2.5F, 6F, -2.5F, 5, 2, 5);
        bootR.setRotationPoint(-2F, 12F, 0F);
        bootR.setTextureSize(128, 64);
        bootR.mirror = true;
        setRotation(bootR, 0F, 0F, 0F);

        bootL = new ModelRenderer(this, 0, 38);
        bootL.addBox(-2.5F, 6F, -2.5F, 5, 2, 5);
        bootL.setRotationPoint(2F, 12F, 0F);
        bootL.setTextureSize(128, 64);
        bootL.mirror = true;
        setRotation(bootL, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 40, 32);
        chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

        hairLeftTop = new ModelRenderer(this, 0, 45);
        hairLeftTop.addBox(-1.5F, -7.5F, -4.5F, 4, 1, 8);
        hairLeftTop.setRotationPoint(0F, 0F, 0F);
        hairLeftTop.setTextureSize(128, 64);
        hairLeftTop.mirror = true;
        setRotation(hairLeftTop, 0F, 0F, 0F);

        hairTop = new ModelRenderer(this, 0, 45);
        hairTop.addBox(-2.5F, -7.5F, 3.5F, 5, 7, 1);
        hairTop.setRotationPoint(0F, 0F, 0F);
        hairTop.setTextureSize(128, 64);
        hairTop.mirror = true;
        setRotation(hairTop, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

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

        bipedBody.addChild(chest);

        bipedHead.addChild(hairTop);
        bipedHead.addChild(hairLeftTop);
        

    }

    public void render(final Entity entity,
                       final float limbSwing,
                       final float limbSwingAmount,
                       final float ageInTicks,
                       final float netHeadYaw,
                       final float headPitch,
                       final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    }

    public void setRotationAngles(final float limbSwing,
                                  final float limbSwingAmount,
                                  final float ageInTicks,
                                  final float netHeadYaw,
                                  final float headPitch,
                                  final float scaleFactor,
                                  final Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
