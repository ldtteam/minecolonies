package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityFarmerFemale extends CitizenModel
{
    ModelRenderer chest;
    ModelRenderer ponytailBase;
    ModelRenderer ponytailTail;
    ModelRenderer boxBottom;
    ModelRenderer boxFront;
    ModelRenderer boxBack;
    ModelRenderer boxLeft;
    ModelRenderer boxRight;
    ModelRenderer seeds;
    ModelRenderer strapLeft;
    ModelRenderer strapRight;
    ModelRenderer hatStrap;
    ModelRenderer hatFrill;
    ModelRenderer hatTop;
    ModelRenderer hatBottom;

    public ModelEntityFarmerFemale()
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

        chest = new ModelRenderer(this, 17, 32);
        chest.addCuboid(-3.5F, 2.7F, -0.6F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 64);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

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
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        ponytailBase = new ModelRenderer(this, 33, 6);
        ponytailBase.addCuboid(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        ponytailBase.setRotationPoint(0F, 0F, 0F);
        ponytailBase.setTextureSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.1047198F, 0F, 0F);

        ponytailTail = new ModelRenderer(this, 32, 0);
        ponytailTail.addCuboid(-1F, -2.2F, 3.5F, 2, 5, 1);
        ponytailTail.setRotationPoint(0F, 0F, 0F);
        ponytailTail.setTextureSize(128, 64);
        ponytailTail.mirror = true;
        setRotation(ponytailTail, 0.2268928F, 0F, 0F);

        boxBottom = new ModelRenderer(this, 19, 50);
        boxBottom.addCuboid(-3F, 0F, -2F, 6, 1, 3);
        boxBottom.setRotationPoint(0F, 9F, -4F);
        boxBottom.setTextureSize(128, 64);
        boxBottom.mirror = true;
        setRotation(boxBottom, 0F, 0F, 0F);

        boxFront = new ModelRenderer(this, 21, 55);
        boxFront.addCuboid(-3F, -2F, -3F, 6, 3, 1);
        boxFront.setRotationPoint(0F, 9F, -4F);
        boxFront.setTextureSize(128, 64);
        boxFront.mirror = true;
        setRotation(boxFront, 0F, 0F, 0F);

        seeds = new ModelRenderer(this, 19, 45);
        seeds.addCuboid(-3F, -2F, -2F, 6, 1, 3);
        seeds.setRotationPoint(0F, 9F, -4F);
        seeds.setTextureSize(128, 64);
        seeds.mirror = true;
        setRotation(seeds, 0F, 0F, 0F);

        boxLeft = new ModelRenderer(this, 42, 43);
        boxLeft.addCuboid(3F, -2F, -3F, 1, 3, 6);
        boxLeft.setRotationPoint(0F, 9F, -4F);
        boxLeft.setTextureSize(128, 64);
        boxLeft.mirror = true;
        setRotation(boxLeft, 0F, 0F, 0F);

        strapLeft = new ModelRenderer(this, 0, 55);
        strapLeft.addCuboid(0F, 0F, -4F, 1, 1, 8);
        strapLeft.setRotationPoint(3F, 4F, -4F);
        strapLeft.setTextureSize(128, 64);
        strapLeft.mirror = true;
        setRotation(strapLeft, 1.047198F, 0F, 0F);

        boxRight = new ModelRenderer(this, 0, 43);
        boxRight.addCuboid(-4F, -2F, -3F, 1, 3, 6);
        boxRight.setRotationPoint(0F, 9F, -4F);
        boxRight.setTextureSize(128, 64);
        boxRight.mirror = true;
        setRotation(boxRight, 0F, 0F, 0F);

        strapRight = new ModelRenderer(this, 0, 55);
        strapRight.addCuboid(0F, 0F, -4F, 1, 1, 8);
        strapRight.setRotationPoint(-4F, 4F, -4F);
        strapRight.setTextureSize(128, 64);
        strapRight.mirror = true;
        setRotation(strapRight, 1.047198F, 0F, 0F);

        boxBack = new ModelRenderer(this, 21, 40);
        boxBack.addCuboid(-3F, -2F, -3F, 6, 3, 1);
        boxBack.setRotationPoint(0F, 9F, 0F);
        boxBack.setTextureSize(128, 64);
        boxBack.mirror = true;
        setRotation(boxBack, 0F, 0F, 0F);

        hatBottom = new ModelRenderer(this, 61, 9);
        hatBottom.addCuboid(-5F, -7.8F, -7F, 10, 3, 8);
        hatBottom.setRotationPoint(0F, 0F, 0F);
        hatBottom.setTextureSize(128, 64);
        hatBottom.mirror = true;
        setRotation(hatBottom, -0.7853982F, 0F, 0F);

        hatStrap = new ModelRenderer(this, 68, 33);
        hatStrap.addCuboid(-4.5F, -6.7F, -2.7F, 9, 8, 1);
        hatStrap.setRotationPoint(0F, 0F, 0F);
        hatStrap.setTextureSize(128, 64);
        hatStrap.mirror = true;
        setRotation(hatStrap, -0.3490659F, 0F, 0F);

        hatFrill = new ModelRenderer(this, 57, 21);
        hatFrill.addCuboid(-5.5F, -5.7F, -8F, 11, 1, 10);
        hatFrill.setRotationPoint(0F, 0F, 0F);
        hatFrill.setTextureSize(128, 64);
        hatFrill.mirror = true;
        setRotation(hatFrill, -0.6981317F, 0F, 0F);

        hatTop = new ModelRenderer(this, 64, 1);
        hatTop.addCuboid(-4.5F, -8.5F, -6F, 9, 1, 6);
        hatTop.setRotationPoint(0F, 0F, 0F);
        hatTop.setTextureSize(128, 64);
        hatTop.mirror = true;
        setRotation(hatTop, -0.7853982F, 0F, 0F);

        bipedBody.addChild(chest);

        bipedHead.addChild(hatFrill);
        bipedHead.addChild(hatBottom);
        bipedHead.addChild(hatTop);

        bipedHead.addChild(hatStrap);

        bipedHead.addChild(ponytailBase);
        bipedHead.addChild(ponytailTail);

        bipedBody.addChild(boxBottom);
        bipedBody.addChild(boxFront);
        bipedBody.addChild(boxBack);
        bipedBody.addChild(boxLeft);
        bipedBody.addChild(boxRight);
        bipedBody.addChild(seeds);

        bipedBody.addChild(strapLeft);
        bipedBody.addChild(strapRight);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
