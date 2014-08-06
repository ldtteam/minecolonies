package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEntityDeliveryman extends ModelBiped
{
    public ModelRenderer backpack;

    public ModelEntityDeliveryman()
    {
        float scale = 0F;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8, scale);
        bipedHead.setRotationPoint(0F, 2F, -4F);
        bipedHead.rotateAngleX = 0.34907F;

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -4F, 8, 12, 4, scale);
        bipedBody.setRotationPoint(0F, 1F, -2F);
        bipedBody.rotateAngleX = 0.34907F;
        bipedBody.rotateAngleZ = 0F;

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(0F, 0F, -2F, 4, 12, 4, scale);
        bipedLeftArm.setRotationPoint(4F, 2F, -4F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-4F, 0F, -2F, 4, 12, 4, scale);
        bipedRightArm.setRotationPoint(-4F, 2F, -4F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4, scale);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4, scale);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);

        backpack = new ModelRenderer(this, 32, 0);
        backpack.addBox(-4F, 0F, 0F, 8, 10, 6, scale);
        backpack.setRotationPoint(0F, 1F, -2F);
        backpack.rotateAngleX = 0.34907F;

    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        bipedHead.render(f5);
        bipedBody.render(f5);
        bipedLeftArm.render(f5);
        bipedRightArm.render(f5);
        bipedRightLeg.render(f5);
        bipedLeftLeg.render(f5);
        backpack.render(f5);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        bipedHead.rotateAngleY = f3 / 57.29578F;
        bipedHead.rotateAngleX = f4 / 57.29578F + 0.45F;

        bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.0F * f1 * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.0F * f1 * 0.5F;
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;
        if(isRiding)
        {
            bipedRightArm.rotateAngleX += -0.6283185F;
            bipedLeftArm.rotateAngleX += -0.6283185F;
            bipedRightLeg.rotateAngleX = -1.256637F;
            bipedLeftLeg.rotateAngleX = -1.256637F;
            bipedRightLeg.rotateAngleY = 0.3141593F;
            bipedLeftLeg.rotateAngleY = -0.3141593F;
        }
        bipedRightArm.rotateAngleY = 0.0F;
        bipedLeftArm.rotateAngleY = 0.0F;
        if(onGround > -9990F)
        {
            float f6 = onGround;
            bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * 3.141593F * 2.0F) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
            f6 = 1.0F - onGround;
            f6 *= f6;
            f6 *= f6;
            f6 = 1.0F - f6;
            float f7 = MathHelper.sin(f6 * 3.141593F);
            float f8 = MathHelper.sin(onGround * 3.141593F) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
            bipedRightArm.rotateAngleX -= f7 * 1.2D + f8;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
            bipedRightArm.rotateAngleZ = MathHelper.sin(onGround * 3.141593F) * -0.4F;
        }
        if(isSneak)
        {
            bipedBody.rotateAngleX = 0.5F;
            bipedRightLeg.rotateAngleX -= 0.0F;
            bipedLeftLeg.rotateAngleX -= 0.0F;
            bipedRightArm.rotateAngleX += 0.4F;
            bipedLeftArm.rotateAngleX += 0.4F;
            bipedRightLeg.rotationPointZ = 4F;
            bipedLeftLeg.rotationPointZ = 4F;
            bipedRightLeg.rotationPointX = 9F;
            bipedLeftLeg.rotationPointY = 9F;
            bipedHead.rotationPointY = 1.0F;
        }
        else
        {
            bipedHead.rotationPointY = 1.5F;
            bipedHead.rotationPointZ = -5.0F;
            bipedBody.rotationPointZ = -3.0F;
            bipedBody.rotationPointY = 0.5F;
            backpack.rotationPointY = 1F;
            backpack.rotationPointZ = -3.5F;
            bipedRightArm.rotationPointZ = -4.0F;
            bipedLeftArm.rotationPointZ = -4.0F;
            bipedRightArm.rotationPointY = 1.0F;
            bipedLeftArm.rotationPointY = 1.0F;
            bipedRightArm.rotationPointX = -4F;
            bipedLeftArm.rotationPointX = 4F;
            bipedRightArm.rotateAngleX += 0.0F;
            bipedLeftArm.rotateAngleX += 0.0F;

            bipedRightLeg.rotationPointZ = 0.0F;
            bipedLeftLeg.rotationPointZ = 0.0F;
            bipedRightLeg.rotationPointY = 12F;
            bipedLeftLeg.rotationPointY = 12F;

        }
        bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.01F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.01F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.01F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.01F;
    }
}
