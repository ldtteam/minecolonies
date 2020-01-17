package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBuilderFemale extends CitizenModel<AbstractEntityCitizen>
{
    ModelRenderer hatBase;
    ModelRenderer hatBottomMiddle;
    ModelRenderer hatBack;
    ModelRenderer hatFront;
    ModelRenderer hatTopMiddle;
    ModelRenderer hatBrimBase;
    ModelRenderer hatBrimFrontTip;
    ModelRenderer hatBrimFront;
    ModelRenderer chest;
    ModelRenderer ponytailTail;
    ModelRenderer ponytailBase;
    ModelRenderer hammerHandle;
    ModelRenderer hammerHead;
    ModelRenderer belt;
    ModelRenderer ruler;

    public ModelEntityBuilderFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        hatBase = new ModelRenderer(this, 57, 19);
        hatBase.addCuboid(-4F, -9.7F, -4F, 8, 2, 7);
        hatBase.setRotationPoint(0F, 0F, 0F);
        hatBase.setTextureSize(128, 64);
        hatBase.mirror = true;
        setRotation(hatBase, -0.1396263F, 0F, 0F);

        hatBottomMiddle = new ModelRenderer(this, 57, 8);
        hatBottomMiddle.addCuboid(-3F, -10F, -5F, 6, 2, 9);
        hatBottomMiddle.setTextureSize(128, 64);
        hatBottomMiddle.mirror = true;
        setRotation(hatBottomMiddle, 0F, 0F, 0F);

        hatTopMiddle = new ModelRenderer(this, 61, 0);
        hatTopMiddle.addCuboid(-2F, -11F, -4F, 4, 1, 7);
        hatTopMiddle.setTextureSize(128, 64);
        hatTopMiddle.mirror = true;
        setRotation(hatTopMiddle, 0F, 0F, 0F);

        hatBack = new ModelRenderer(this, 64, 31);
        hatBack.addCuboid(-3.5F, -8F, 4F, 7, 1, 1);
        hatBack.setTextureSize(128, 64);
        hatBack.mirror = true;
        setRotation(hatBack, 0F, 0F, 0F);

        hatFront = new ModelRenderer(this, 66, 28);
        hatFront.addCuboid(-2.5F, -9F, -6F, 5, 1, 1);
        hatFront.setTextureSize(128, 64);
        hatFront.mirror = true;
        setRotation(hatFront, 0F, 0F, 0F);

        hatBrimBase = new ModelRenderer(this, 53, 33);
        hatBrimBase.addCuboid(-4.5F, -8F, -6F, 9, 1, 10);
        hatBrimBase.setTextureSize(128, 64);
        hatBrimBase.mirror = true;
        setRotation(hatBrimBase, 0F, 0F, 0F);

        hatBrimFront = new ModelRenderer(this, 64, 44);
        hatBrimFront.addCuboid(-3.5F, -8F, -7F, 7, 1, 1);
        hatBrimFront.setTextureSize(128, 64);
        hatBrimFront.mirror = true;
        setRotation(hatBrimFront, 0F, 0F, 0F);

        hatBrimFrontTip = new ModelRenderer(this, 66, 46);
        hatBrimFrontTip.addCuboid(-2.5F, -8F, -8F, 5, 1, 1);
        hatBrimFrontTip.setTextureSize(128, 64);
        hatBrimFrontTip.mirror = true;
        setRotation(hatBrimFrontTip, 0F, 0F, 0F);

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

        chest = new ModelRenderer(this, 17, 32);
        chest.addCuboid(-3.5F, 1.7F, -1F, 7, 3, 4);
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

        ponytailBase = new ModelRenderer(this, 32, 0);
        ponytailBase.addCuboid(-1F, -2.2F, 3.5F, 2, 5, 1);
        ponytailBase.setTextureSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.2268928F, 0F, 0F);

        ponytailTail = new ModelRenderer(this, 33, 6);
        ponytailTail.addCuboid(-0.5F, 2.2F, 3.8F, 1, 5, 1);
        ponytailTail.setTextureSize(128, 64);
        ponytailTail.mirror = true;
        setRotation(ponytailTail, -0.122173F, 0F, 0F);

        hammerHandle = new ModelRenderer(this, 2, 49);
        hammerHandle.addCuboid(1F, 7.3F, -2.4F, 1, 4, 1);
        hammerHandle.setTextureSize(128, 64);
        hammerHandle.mirror = true;
        setRotation(hammerHandle, 0F, 0F, 0.3141593F);

        hammerHead = new ModelRenderer(this, 0, 47);
        hammerHead.addCuboid(0F, 7.5F, -2.5F, 3, 1, 1);
        hammerHead.setTextureSize(128, 64);
        hammerHead.mirror = true;
        setRotation(hammerHead, 0F, 0F, 0F);

        belt = new ModelRenderer(this, 0, 40);
        belt.addCuboid(-4.5F, 9F, -2.5F, 9, 1, 5);
        belt.setTextureSize(128, 64);
        belt.mirror = true;
        setRotation(belt, 0F, 0F, 0F);

        ruler = new ModelRenderer(this, 17, 47);
        ruler.addCuboid(2F, 7.3F, -2.2F, 1, 4, 1);
        ruler.setTextureSize(128, 64);
        ruler.mirror = true;
        setRotation(ruler, 0F, 0F, 0F);

        bipedBody.addChild(chest);
        bipedBody.addChild(belt);
        bipedBody.addChild(ruler);
        bipedBody.addChild(hammerHandle);
        hammerHandle.addChild(hammerHead);

        bipedHead.addChild(hatBase);
        hatBase.addChild(hatBottomMiddle);
        hatBottomMiddle.addChild(hatBack);
        hatBottomMiddle.addChild(hatFront);
        hatBottomMiddle.addChild(hatTopMiddle);

        hatBase.addChild(hatBrimBase);
        hatBrimBase.addChild(hatBrimFront);
        hatBrimFront.addChild(hatBrimFrontTip);

        bipedHead.addChild(ponytailBase);
        ponytailBase.addChild(ponytailTail);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
