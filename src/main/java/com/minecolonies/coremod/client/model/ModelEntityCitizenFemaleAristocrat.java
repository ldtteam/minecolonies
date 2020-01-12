package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleAristocrat extends CitizenModel
{
    ModelRenderer breast;
    ModelRenderer leftArm2;
    ModelRenderer leftArm1;
    ModelRenderer hair1;
    ModelRenderer hair2;
    ModelRenderer hair3;
    ModelRenderer umbrellaHand;
    ModelRenderer umbrella;
    ModelRenderer dressPart1;
    ModelRenderer dressPart2;
    ModelRenderer dressPart3;
    ModelRenderer dressPart5;
    ModelRenderer dressPart6;
    ModelRenderer dressPart7;
    ModelRenderer dressPart8;
    ModelRenderer dressPart9;
    ModelRenderer dressPart10;
    ModelRenderer dressPart11;
    ModelRenderer dressPart12;
    ModelRenderer dressPart13;

    public ModelEntityCitizenFemaleAristocrat()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 12, 17);
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 1F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 34, 17);
        bipedRightArm.addCuboid(-2F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-4F, 0F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        leftArm2 = new ModelRenderer(this, 46, 22);
        leftArm2.addCuboid(-1F, -1F, -1F, 3, 7, 3);
        leftArm2.setRotationPoint(5F, 6F, 1F);
        leftArm2.setTextureSize(64, 64);
        setRotation(leftArm2, -1.570796F, 0F, 0F);

        leftArm1 = new ModelRenderer(this, 34, 17);
        leftArm1.addCuboid(0F, 0F, -1F, 3, 6, 3);
        leftArm1.setRotationPoint(4F, 0F, 0F);
        leftArm1.setTextureSize(64, 64);
        setRotation(leftArm1, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 17);
        bipedRightLeg.addCuboid(-1F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 17);
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        breast = new ModelRenderer(this, 0, 33);
        breast.addCuboid(-3F, 1F, -2.5F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(64, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair1 = new ModelRenderer(this, 32, 0);
        hair1.addCuboid(-3F, -3F, -4F, 6, 3, 6);
        hair1.setRotationPoint(0F, -8F, 1F);
        hair1.setTextureSize(64, 64);
        setRotation(hair1, 0F, 0F, 0F);

        hair2 = new ModelRenderer(this, 56, 0);
        hair2.addCuboid(-1F, -1F, -2F, 2, 1, 2);
        hair2.setRotationPoint(0F, -11F, 1F);
        hair2.setTextureSize(64, 64);
        setRotation(hair2, 0F, 0F, 0F);

        hair3 = new ModelRenderer(this, 32, 10);
        hair3.addCuboid(-2F, -1F, -3F, 4, 2, 4);
        hair3.setRotationPoint(0F, -13F, 1F);
        hair3.setTextureSize(64, 64);
        setRotation(hair3, 0F, 0F, 0F);

        umbrellaHand = new ModelRenderer(this, 60, 10);
        umbrellaHand.addCuboid(0F, -2F, 0F, 1, 21, 1);
        umbrellaHand.setRotationPoint(5F, 6F, -5F);
        umbrellaHand.setTextureSize(64, 64);
        setRotation(umbrellaHand, 2.565634F, 0F, 0F);

        umbrella = new ModelRenderer(this, 0, 54);
        umbrella.addCuboid(-4F, 0F, -4F, 9, 1, 9);
        umbrella.setRotationPoint(5F, -10F, 5F);
        umbrella.setTextureSize(64, 64);
        setRotation(umbrella, -0.4712389F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 18, 33);
        dressPart1.addCuboid(-6F, 0F, -8F, 12, 6, 11, 0.01F);
        dressPart1.setRotationPoint(0F, 16F, 1F);
        dressPart1.setTextureSize(64, 64);
        setRotation(dressPart1, 0F, 0.418879F, 0F);

        dressPart2 = new ModelRenderer(this, 18, 33);
        dressPart2.addCuboid(-6F, 0F, -7F, 12, 6, 11);
        dressPart2.setRotationPoint(0F, 16F, 0F);
        dressPart2.setTextureSize(64, 64);
        setRotation(dressPart2, 0F, -0.3839724F, 0F);

        dressPart3 = new ModelRenderer(this, 30, 50);
        dressPart3.addCuboid(-5F, 0F, -5F, 10, 4, 7, 0.01F);
        dressPart3.setRotationPoint(0F, 9F, 0F);
        dressPart3.setTextureSize(64, 64);
        setRotation(dressPart3, 0F, -0.3316126F, 0F);

        dressPart5 = new ModelRenderer(this, 30, 50);
        dressPart5.addCuboid(-6F, 0F, -5F, 10, 4, 7);
        dressPart5.setRotationPoint(0F, 9F, 0F);
        dressPart5.setTextureSize(64, 64);
        setRotation(dressPart5, 0F, 0.4363323F, 0F);

        dressPart6 = new ModelRenderer(this, 0, 40);
        dressPart6.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart6.setRotationPoint(2F, 18F, -4F);
        dressPart6.setTextureSize(64, 64);
        setRotation(dressPart6, 2.503836F, 0.3210144F, -0.3592861F);
        dressPart6.offsetZ -= 0.1F;

        dressPart7 = new ModelRenderer(this, 0, 40);
        dressPart7.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart7.setRotationPoint(-2F, 18F, -4F);
        dressPart7.setTextureSize(64, 64);
        setRotation(dressPart7, 2.070064F, 0.797036F, -0.6991393F);
        dressPart7.offsetZ -= 0.1F;

        dressPart8 = new ModelRenderer(this, 0, 40);
        dressPart8.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart8.setRotationPoint(-5F, 18F, -1F);
        dressPart8.setTextureSize(64, 64);
        setRotation(dressPart8, 2.740167F, 0.6363323F, 0.4537856F);
        dressPart8.offsetZ -= 0.1F;

        dressPart9 = new ModelRenderer(this, 0, 40);
        dressPart9.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart9.setRotationPoint(-5F, 18F, 2F);
        dressPart9.setTextureSize(64, 64);
        setRotation(dressPart9, 3.010485F, 1.313108F, 0.2890419F);
        dressPart9.offsetZ -= 0.1F;

        dressPart10 = new ModelRenderer(this, 0, 40);
        dressPart10.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart10.setRotationPoint(-2F, 18F, 5F);
        dressPart10.setTextureSize(64, 64);
        setRotation(dressPart10, -2.563121F, 0.3303337F, 0.4129171F);
        dressPart10.offsetZ -= 0.1F;

        dressPart11 = new ModelRenderer(this, 0, 40);
        dressPart11.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart11.setRotationPoint(2F, 18F, 5F);
        dressPart11.setTextureSize(64, 64);
        setRotation(dressPart11, -2.458432F, -0.2216398F, -0.3303337F);
        dressPart11.offsetZ -= 0.1F;

        dressPart12 = new ModelRenderer(this, 0, 40);
        dressPart12.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart12.setRotationPoint(4F, 18F, 2F);
        dressPart12.setTextureSize(64, 64);
        setRotation(dressPart12, -2.852551F, 0.5367922F, -0.2064585F);
        dressPart12.offsetZ -= 0.1F;

        dressPart13 = new ModelRenderer(this, 0, 40);
        dressPart13.addCuboid(-2F, 0F, -2F, 4, 7, 4);
        dressPart13.setRotationPoint(4F, 18F, -1F);
        dressPart13.setTextureSize(64, 64);
        setRotation(dressPart13, 2.778193F, -0.5512723F, -0.2477502F);
        dressPart13.offsetZ -= 0.1F;

        bipedHead.addChild(hair1);
        bipedHead.addChild(hair2);
        bipedHead.addChild(hair3);

        bipedBody.addChild(breast);

        bipedBody.addChild(dressPart1);
        bipedBody.addChild(dressPart2);
        bipedBody.addChild(dressPart3);
        bipedBody.addChild(dressPart5);
        bipedBody.addChild(dressPart6);
        bipedBody.addChild(dressPart7);
        bipedBody.addChild(dressPart8);
        bipedBody.addChild(dressPart9);
        bipedBody.addChild(dressPart10);
        bipedBody.addChild(dressPart11);
        bipedBody.addChild(dressPart12);
        bipedBody.addChild(dressPart13);

        bipedBody.addChild(umbrella);
        bipedBody.addChild(umbrellaHand);

        bipedBody.addChild(leftArm1);
        bipedBody.addChild(leftArm2);
        bipedLeftArm.showModel = false;
        this.bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}