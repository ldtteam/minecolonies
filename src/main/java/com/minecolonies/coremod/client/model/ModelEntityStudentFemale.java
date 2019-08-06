package com.minecolonies.coremod.client.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the male students (monks).
 */
public class ModelEntityStudentFemale extends BipedModel
{
    public ModelEntityStudentFemale()
    {
        RendererModel front;
        RendererModel back;
        RendererModel left;
        RendererModel right;
        RendererModel armCHorizontal;
        RendererModel rightArmC;
        RendererModel leftArmC;
        RendererModel helmet;
        RendererModel chest;
        RendererModel ponytailBase;
        RendererModel ponyTailTip;
        RendererModel book;

        textureWidth = 128;
        textureHeight = 64;

        front = new RendererModel(this, 16, 48);
        front.addBox(0F, 0F, 1F, 8, 8, 0);
        front.setRotationPoint(-4F, 12F, -3F);
        front.setTextureSize(128, 64);
        front.mirror = true;
        setRotation(front, 0F, 0F, 0F);

        back = new RendererModel(this, 16, 40);
        back.addBox(0F, 0F, 1F, 8, 8, 0);
        back.setRotationPoint(-4F, 12F, 3F);
        back.setTextureSize(128, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);

        left = new RendererModel(this, 16, 34);
        left.addBox(0F, 0F, 1F, 0, 8, 6);
        left.setRotationPoint(4F, 12F, -3F);
        left.setTextureSize(128, 64);
        left.mirror = true;
        setRotation(left, 0F, 0F, 0F);

        right = new RendererModel(this, 16, 34);
        right.addBox(0F, 0F, 1F, 0, 8, 6);
        right.setRotationPoint(-4F, 12F, -3F);
        right.setTextureSize(128, 64);
        right.mirror = true;
        setRotation(right, 0F, 0F, 0F);




        armCHorizontal = new RendererModel(this, 0, 56);
        armCHorizontal.addBox(0F, 0F, 0F, 16, 4, 4);
        armCHorizontal.setRotationPoint(-8F, 3.8F, -3.5F);
        armCHorizontal.setTextureSize(128, 64);
        armCHorizontal.mirror = true;
        setRotation(armCHorizontal, -0.4886922F, 0F, 0F);

        rightArmC = new RendererModel(this, 0, 44);
        rightArmC.addBox(0F, 0F, 0F, 4, 8, 4);
        rightArmC.setRotationPoint(-8F, -0.5F, -1F);
        rightArmC.setTextureSize(128, 64);
        rightArmC.mirror = true;
        setRotation(rightArmC, -0.5061455F, 0F, 0F);

        leftArmC = new RendererModel(this, 0, 44);
        leftArmC.addBox(0F, 0F, 0F, 4, 8, 4);
        leftArmC.setRotationPoint(4F, -0.5F, -1F);
        leftArmC.setTextureSize(128, 64);
        leftArmC.mirror = true;
        setRotation(leftArmC, -0.5061455F, 0F, 0F);

        bipedRightArm = new RendererModel(this, 44, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 44, 16);
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
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 6);
        bipedBody.setRotationPoint(0F, 0F, -1F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        helmet = new RendererModel(this, 40, 46);
        helmet.addBox(0F, 0F, 0F, 9, 9, 9);
        helmet.setRotationPoint(-4.5F, -8.5F, -4.5F);
        helmet.setTextureSize(128, 64);
        helmet.mirror = true;
        setRotation(helmet, 0F, 0F, 0F);

        chest = new RendererModel(this, 44, 32);
        chest.addBox(0F, 0F, 0F, 7, 3, 4);
        chest.setRotationPoint(-3.5F, 1.7F, -2.7F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.4537856F, 0F, 0F);

        ponytailBase = new RendererModel(this, 32, 49);
        ponytailBase.addBox(0F, 0F, 0F, 2, 5, 2);
        ponytailBase.setRotationPoint(-1F, -4F, 2F);
        ponytailBase.setTextureSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.5576792F, 0F, 0F);

        ponyTailTip = new RendererModel(this, 34, 49);
        ponyTailTip.addBox(0F, 0F, 0F, 1, 5, 1);
        ponyTailTip.setRotationPoint(-0.5F, -1F, 4.8F);
        ponyTailTip.setTextureSize(128, 64);
        ponyTailTip.mirror = true;
        setRotation(ponyTailTip, 0.2230717F, 0F, 0F);

        book = new RendererModel(this, 32, 0);
        book.addBox(4F, -2.5F, 0F, 2, 4, 6);
        book.setRotationPoint(-6.5F, 10F, -3F);
        book.setTextureSize(128, 64);
        book.mirror = true;
        setRotation(book, 0F, 0F, 0F);

        this.bipedBody.addChild(front);
        this.bipedBody.addChild(back);
        this.bipedBody.addChild(right);
        this.bipedBody.addChild(left);
        this.bipedBody.addChild(armCHorizontal);
        this.bipedBody.addChild(chest);

        this.bipedHead.addChild(helmet);
        this.bipedHead.addChild(ponytailBase);
        this.bipedHead.addChild(ponyTailTip);


        this.bipedRightArm.addChild(book);

        this.bipedBody.addChild(leftArmC);
        this.bipedBody.addChild(rightArmC);

    }

    @Override
    public void render(
      @NotNull final LivingEntity entity,
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
