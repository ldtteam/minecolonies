package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleCitizen extends CitizenModel
{
    private final ModelRenderer breast;
    private final ModelRenderer hair;
    private final ModelRenderer dressPart1;
    private final ModelRenderer dressPart2;
    private final ModelRenderer dressPart3;

    public ModelEntityCitizenFemaleCitizen()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 1F);
        bipedHead.setTextureSize(64, 64);
        setRotation(bipedHead, 0F, 0F, 0F);

        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addCuboid(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        bipedHeadwear.setRotationPoint(0F, 0F, 1F);
        bipedHeadwear.setTextureSize(64, 64);
        setRotation(bipedHeadwear, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 12, 17);
        bipedBody.addCuboid(-4F, 0F, -4F, 8, 12, 3);
        bipedBody.setRotationPoint(0F, 0F, 3F);
        bipedBody.setTextureSize(64, 64);
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 34, 17);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addCuboid(-1F, 0F, -1F, 3, 12, 3);
        bipedLeftArm.setRotationPoint(4F, 0F, 0F);
        bipedLeftArm.setTextureSize(64, 64);
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 34, 17);
        bipedRightArm.addCuboid(-2F, 0F, -1F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-5F, 0F, 0F);
        bipedRightArm.setTextureSize(64, 64);
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 17);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-1F, 12F, 1F);
        bipedRightLeg.setTextureSize(64, 64);
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 17);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 3, 12, 3);
        bipedLeftLeg.setRotationPoint(2F, 12F, 1F);
        bipedLeftLeg.setTextureSize(64, 64);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        breast = new ModelRenderer(this, 0, 33);
        breast.addCuboid(-3F, 2F, -4.5F, 8, 4, 3);
        breast.setRotationPoint(-1F, 3F, 1F);
        breast.setTextureSize(64, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair = new ModelRenderer(this, 46, 17);
        hair.addCuboid(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        hair.setRotationPoint(0F, 0F, 1F);
        hair.setTextureSize(64, 64);
        setRotation(hair, 0F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 26, 46);
        dressPart1.addCuboid(-5F, 2F, -7F, 10, 9, 9);
        dressPart1.setRotationPoint(0F, 11F, 0F);
        dressPart1.setTextureSize(64, 64);
        setRotation(dressPart1, 0F, 0F, 0F);

        dressPart2 = new ModelRenderer(this, 28, 38);
        dressPart2.addCuboid(-5F, 1F, -6F, 10, 1, 7);
        dressPart2.setRotationPoint(0F, 11F, 0F);
        dressPart2.setTextureSize(64, 64);
        setRotation(dressPart2, 0F, 0F, 0F);

        dressPart3 = new ModelRenderer(this, 32, 32);
        dressPart3.addCuboid(-4F, 0F, -5F, 8, 1, 5);
        dressPart3.setRotationPoint(0F, 11F, 0F);
        dressPart3.setTextureSize(64, 64);
        setRotation(dressPart3, 0F, 0F, 0F);

        bipedHead.addChild(hair);
        bipedBody.addChild(breast);
        bipedBody.addChild(dressPart1);
        bipedBody.addChild(dressPart2);
        bipedBody.addChild(dressPart3);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}