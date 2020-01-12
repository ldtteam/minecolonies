package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the male students (monks).
 */
public class ModelEntityStudentMale extends CitizenModel
{
    public ModelEntityStudentMale()
    {
        ModelRenderer front;
        ModelRenderer back;
        ModelRenderer left;
        ModelRenderer right;
        ModelRenderer armCHorizontal;
        ModelRenderer rightArmC;
        ModelRenderer leftArmC;
        ModelRenderer belly;
        ModelRenderer bellyU;
        ModelRenderer helmet;

        textureWidth = 128;
        textureHeight = 64;

        front = new ModelRenderer(this, 16, 48);
        front.addCuboid(0F, 0F, 1F, 8, 8, 0);
        front.setRotationPoint(-4F, 12F, -3F);
        front.setTextureSize(128, 64);
        front.mirror = true;
        setRotation(front, 0F, 0F, 0F);

        back = new ModelRenderer(this, 16, 40);
        back.addCuboid(0F, 0F, 1F, 8, 8, 0);
        back.setRotationPoint(-4F, 12F, 3F);
        back.setTextureSize(128, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);

        left = new ModelRenderer(this, 16, 34);
        left.addCuboid(0F, 0F, 1F, 0, 8, 6);
        left.setRotationPoint(4F, 12F, -3F);
        left.setTextureSize(128, 64);
        left.mirror = true;
        setRotation(left, 0F, 0F, 0F);

        right = new ModelRenderer(this, 16, 34);
        right.addCuboid(0F, 0F, 1F, 0, 8, 6);
        right.setRotationPoint(-4F, 12F, -3F);
        right.setTextureSize(128, 64);
        right.mirror = true;
        setRotation(right, 0F, 0F, 0F);

        armCHorizontal = new ModelRenderer(this, 0, 56);
        armCHorizontal.addCuboid(0F, -1F, 1F, 16, 4, 4);
        armCHorizontal.setRotationPoint(-8F, 3.8F, -3.5F);
        armCHorizontal.setTextureSize(128, 64);
        armCHorizontal.mirror = true;
        setRotation(armCHorizontal, -0.4886922F, 0F, 0F);

        rightArmC = new ModelRenderer(this, 0, 44);
        rightArmC.addCuboid(0F, 0F, 1F, 4, 8, 4);
        rightArmC.setRotationPoint(-8F, -0.5F, -1F);
        rightArmC.setTextureSize(128, 64);
        rightArmC.mirror = true;
        setRotation(rightArmC, -0.5061455F, 0F, 0F);

        leftArmC = new ModelRenderer(this, 0, 44);
        leftArmC.addCuboid(0F, 0F, 1F, 4, 8, 4);
        leftArmC.setRotationPoint(4F, -0.5F, -1F);
        leftArmC.setTextureSize(128, 64);
        leftArmC.mirror = true;
        setRotation(leftArmC, -0.5061455F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 44, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 44, 16);
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        belly = new ModelRenderer(this, 29, 34);
        belly.addCuboid(0F, 0F, 1F, 6, 4, 1);
        belly.setRotationPoint(-3F, 6F, -4F);
        belly.setTextureSize(128, 64);
        belly.mirror = true;
        setRotation(belly, 0F, 0F, 0F);

        bellyU = new ModelRenderer(this, 19, 29);
        bellyU.addCuboid(0F, 0F, 1F, 4, 1, 1);
        bellyU.setRotationPoint(-2F, 5F, -4F);
        bellyU.setTextureSize(128, 64);
        bellyU.mirror = true;
        setRotation(bellyU, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 6);
        bipedBody.setRotationPoint(0F, 0F, -1F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        helmet = new ModelRenderer(this, 40, 46);
        helmet.addCuboid(0F, 0F, 0F, 9, 9, 9);
        helmet.setRotationPoint(-4.5F, -8.5F, -4.5F);
        helmet.setTextureSize(128, 64);
        helmet.mirror = true;
        setRotation(helmet, 0F, 0F, 0F);

        this.bipedBody.addChild(belly);
        this.bipedBody.addChild(bellyU);
        this.bipedBody.addChild(front);
        this.bipedBody.addChild(back);
        this.bipedBody.addChild(right);
        this.bipedBody.addChild(left);
        this.bipedBody.addChild(armCHorizontal);

        this.bipedHead.addChild(helmet);


        this.bipedBody.addChild(leftArmC);
        this.bipedBody.addChild(rightArmC);

    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
