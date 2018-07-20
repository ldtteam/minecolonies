package com.minecolonies.coremod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityComposterMale extends ModelBiped
{
    //fields
    ModelRenderer gloveR;
    ModelRenderer gloveL;
    ModelRenderer bootR;
    ModelRenderer LeftLeg;
    ModelRenderer Body;
    ModelRenderer Head;
    ModelRenderer bootL;
    ModelRenderer RightLeg;
    ModelRenderer LeftArm;
    ModelRenderer RightArm;

    public ModelEntityComposterMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        gloveR = new ModelRenderer(this, 20, 32);
        gloveR.addBox(-3.5F, 5F, -2.5F, 5, 1, 5);
        gloveR.setRotationPoint(-5F, 2F, 0F);
        gloveR.setTextureSize(128, 64);
        gloveR.mirror = true;
        setRotation(gloveR, 0F, 0F, 0F);
        gloveL.mirror = true;
        gloveL = new ModelRenderer(this, 0, 32);
        gloveL.addBox(-1.5F, 5F, -2.5F, 5, 1, 5);
        gloveL.setRotationPoint(5F, 2F, 0F);
        gloveL.setTextureSize(128, 64);
        gloveL.mirror = true;
        setRotation(gloveL, 0F, 0F, 0F);
        gloveL.mirror = false;
        bootR = new ModelRenderer(this, 20, 38);
        bootR.addBox(-2.5F, 6F, -2.5F, 5, 2, 5);
        bootR.setRotationPoint(-2F, 12F, 0F);
        bootR.setTextureSize(128, 64);
        bootR.mirror = true;
        setRotation(bootR, 0F, 0F, 0F);
        LeftLeg.mirror = true;
        LeftLeg = new ModelRenderer(this, 0, 16);
        LeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        LeftLeg.setRotationPoint(2F, 12F, 0F);
        LeftLeg.setTextureSize(128, 64);
        LeftLeg.mirror = true;
        setRotation(LeftLeg, 0F, 0F, 0F);
        LeftLeg.mirror = false;
        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 0F, -2F, 8, 12, 4);
        Body.setRotationPoint(0F, 0F, 0F);
        Body.setTextureSize(128, 64);
        Body.mirror = true;
        setRotation(Body, 0F, 0F, 0F);
        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -8F, -4F, 8, 8, 8);
        Head.setRotationPoint(0F, 0F, 0F);
        Head.setTextureSize(128, 64);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);
        bootL = new ModelRenderer(this, 0, 38);
        bootL.addBox(-2.5F, 6F, -2.5F, 5, 2, 5);
        bootL.setRotationPoint(2F, 12F, 0F);
        bootL.setTextureSize(128, 64);
        bootL.mirror = true;
        setRotation(bootL, 0F, 0F, 0F);
        RightLeg = new ModelRenderer(this, 0, 16);
        RightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        RightLeg.setRotationPoint(-2F, 12F, 0F);
        RightLeg.setTextureSize(128, 64);
        RightLeg.mirror = true;
        setRotation(RightLeg, 0F, 0F, 0F);
        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        LeftArm.setRotationPoint(5F, 2F, 0F);
        LeftArm.setTextureSize(128, 64);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, 0F);
        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        RightArm.setRotationPoint(-5F, 2F, 0F);
        RightArm.setTextureSize(128, 64);
        RightArm.mirror = true;
        setRotation(RightArm, 0F, 0F, 0F);
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
        gloveR.render(scaleFactor);
        gloveL.render(scaleFactor);
        bootR.render(scaleFactor);
        LeftLeg.render(scaleFactor);
        Body.render(scaleFactor);
        Head.render(scaleFactor);
        bootL.render(scaleFactor);
        RightLeg.render(scaleFactor);
        LeftArm.render(scaleFactor);
        RightArm.render(scaleFactor);
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
