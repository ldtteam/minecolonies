package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityMinerFemale extends CitizenModel<AbstractEntityCitizen>
{
    //fields
    ModelRenderer goggleLeft;
    ModelRenderer goggleBase;
    ModelRenderer goggleRight;
    ModelRenderer goggleRightLens;
    ModelRenderer goggleLeftLens;
    ModelRenderer chest;
    ModelRenderer helmetBase;
    ModelRenderer helmetLight;
    ModelRenderer helmetFront;
    ModelRenderer helmetTop;
    ModelRenderer helmetMiddle;
    ModelRenderer bag;
    ModelRenderer belt;
    ModelRenderer torch;
    ModelRenderer bagLock;
    ModelRenderer beltBuckle;
    ModelRenderer ponytailBase;
    ModelRenderer ponytailTail;

    public ModelEntityMinerFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

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
        bipedLeftLeg.mirror = false;

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -7F, -4F, 8, 7, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 0, 55);
        chest.addCuboid(-3.5F, 2.7F, -0.6F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

        goggleLeft = new ModelRenderer(this, 20, 39);
        goggleLeft.addCuboid(1F, -5F, -4.5F, 2, 2, 1);
        goggleLeft.setRotationPoint(0F, 0F, 0F);
        goggleLeft.setTextureSize(128, 64);
        goggleLeft.mirror = true;
        setRotation(goggleLeft, 0F, 0F, 0F);

        goggleBase = new ModelRenderer(this, 0, 33);
        goggleBase.addCuboid(-4.5F, -4.9F, -4.35F, 9, 1, 4);
        goggleBase.setRotationPoint(0F, 0F, 0F);
        goggleBase.setTextureSize(128, 64);
        goggleBase.mirror = true;
        setRotation(goggleBase, 0F, 0F, 0F);

        goggleRight = new ModelRenderer(this, 0, 39);
        goggleRight.addCuboid(-3F, -5F, -4.5F, 2, 2, 1);
        goggleRight.setRotationPoint(0F, 0F, 0F);
        goggleRight.setTextureSize(128, 64);
        goggleRight.mirror = true;
        setRotation(goggleRight, 0F, 0F, 0F);

        goggleRightLens = new ModelRenderer(this, 7, 39);
        goggleRightLens.addCuboid(-2.3F, -4.5F, -4.6F, 1, 1, 1);
        goggleRightLens.setRotationPoint(0F, 0F, 0F);
        goggleRightLens.setTextureSize(128, 64);
        goggleRightLens.mirror = true;
        setRotation(goggleRightLens, 0F, 0F, 0F);

        goggleLeftLens = new ModelRenderer(this, 15, 39);
        goggleLeftLens.addCuboid(1.3F, -4.5F, -4.6F, 1, 1, 1);
        goggleLeftLens.setRotationPoint(0F, 0F, 0F);
        goggleLeftLens.setTextureSize(128, 64);
        goggleLeftLens.mirror = true;
        setRotation(goggleLeftLens, 0F, 0F, 0F);

        helmetBase = new ModelRenderer(this, 32, 49);
        helmetBase.addCuboid(-4.5F, -7.5F, -5.5F, 9, 2, 9);
        helmetBase.setRotationPoint(0F, 0F, 0F);
        helmetBase.setTextureSize(128, 64);
        helmetBase.mirror = true;
        setRotation(helmetBase, -0.122173F, 0F, 0F);

        helmetFront = new ModelRenderer(this, 1, 43);
        helmetFront.addCuboid(-3F, -8.5F, -5.5F, 6, 1, 9);
        helmetFront.setRotationPoint(0F, 0F, 0F);
        helmetFront.setTextureSize(128, 64);
        helmetFront.mirror = true;
        setRotation(helmetFront, 0F, 0F, 0F);

        helmetLight = new ModelRenderer(this, 23, 54);
        helmetLight.addCuboid(-1F, -8F, -6F, 2, 2, 2);
        helmetLight.setRotationPoint(0F, 0F, 0F);
        helmetLight.setTextureSize(128, 64);
        helmetLight.mirror = true;
        setRotation(helmetLight, 0F, 0F, 0F);

        helmetMiddle = new ModelRenderer(this, 32, 41);
        helmetMiddle.addCuboid(-4.5F, -8.5F, -3.5F, 9, 1, 6);
        helmetMiddle.setRotationPoint(0F, 0F, 0F);
        helmetMiddle.setTextureSize(128, 64);
        helmetMiddle.mirror = true;
        setRotation(helmetMiddle, 0F, 0F, 0F);

        helmetTop = new ModelRenderer(this, 32, 33);
        helmetTop.addCuboid(-3F, -9.5F, -3.5F, 6, 1, 6);
        helmetTop.setRotationPoint(0F, 0F, 0F);
        helmetTop.setTextureSize(128, 64);
        helmetTop.mirror = true;
        setRotation(helmetTop, 0F, 0F, 0F);

        bag = new ModelRenderer(this, 57, 16);
        bag.addCuboid(-3.5F, 7.5F, 1.4F, 4, 4, 1);
        bag.setRotationPoint(0F, 0F, 0F);
        bag.setTextureSize(128, 64);
        bag.mirror = true;
        setRotation(bag, 0F, 0F, 0F);

        belt = new ModelRenderer(this, 57, 22);
        belt.addCuboid(-4.5F, 9.5F, -2.5F, 9, 1, 5);
        belt.setRotationPoint(0F, 0F, 0F);
        belt.setTextureSize(128, 64);
        belt.mirror = true;
        setRotation(belt, 0F, 0F, 0F);

        torch = new ModelRenderer(this, 57, 29);
        torch.addCuboid(-3.5F, 7.4F, -2.4F, 1, 4, 1);
        torch.setRotationPoint(0F, 0F, 0F);
        torch.setTextureSize(128, 64);
        torch.mirror = true;
        setRotation(torch, 0F, 0F, 0F);

        bagLock = new ModelRenderer(this, 68, 16);
        bagLock.addCuboid(-2.5F, 8F, 1.5F, 2, 1, 1);
        bagLock.setRotationPoint(0F, 0F, 0F);
        bagLock.setTextureSize(128, 64);
        bagLock.mirror = true;
        setRotation(bagLock, 0F, 0F, 0F);

        beltBuckle = new ModelRenderer(this, 68, 29);
        beltBuckle.addCuboid(-1F, 9F, -2.7F, 2, 2, 1);
        beltBuckle.setRotationPoint(0F, 0F, 0F);
        beltBuckle.setTextureSize(128, 64);
        beltBuckle.mirror = true;
        setRotation(beltBuckle, 0F, 0F, 0F);

        ponytailBase = new ModelRenderer(this, 80, 40);
        ponytailBase.addCuboid(-0.5F, 2.4F, 3.7F, 1, 5, 1);
        ponytailBase.setRotationPoint(0F, 0F, 0F);
        ponytailBase.setTextureSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.1047198F, 0F, 0F);

        ponytailTail = new ModelRenderer(this, 79, 33);
        ponytailTail.addCuboid(-1F, -2F, 3.4F, 2, 5, 1);
        ponytailTail.setRotationPoint(0F, 0F, 0F);
        ponytailTail.setTextureSize(128, 64);
        ponytailTail.mirror = true;
        setRotation(ponytailTail, 0.122173F, 0F, 0F);

        bipedBody.addChild(chest);
        bipedBody.addChild(belt);
        bipedBody.addChild(bag);
        bipedBody.addChild(torch);

        belt.addChild(beltBuckle);
        bag.addChild(bagLock);

        bipedHead.addChild(goggleBase);
        goggleBase.addChild(goggleLeft);
        goggleBase.addChild(goggleRight);
        goggleLeft.addChild(goggleLeftLens);
        goggleRight.addChild(goggleRightLens);

        bipedHead.addChild(ponytailBase);
        ponytailBase.addChild(ponytailTail);

        bipedHead.addChild(helmetBase);
        helmetBase.addChild(helmetFront);
        helmetFront.addChild(helmetLight);

        helmetBase.addChild(helmetMiddle);
        helmetMiddle.addChild(helmetTop);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
