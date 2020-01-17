package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityChildMale extends CitizenModel<AbstractEntityCitizen>
{
    //fields

    public ModelEntityChildMale()
    {
        ModelRenderer pouch;
        ModelRenderer overLeftLeg;
        ModelRenderer overRightLeg;

        textureWidth = 128;
        textureHeight = 64;

        overLeftLeg = new ModelRenderer(this, 0, 33);
        overLeftLeg.addCuboid(-2F, -12F, 0F, 5, 12, 5);
        overLeftLeg.setRotationPoint(-0.5F, 12F, -2.5F);
        overLeftLeg.setTextureSize(128, 64);
        overLeftLeg.mirror = true;
        setRotation(overLeftLeg, 0F, 0F, 0F);

        overRightLeg = new ModelRenderer(this, 0, 33);
        overRightLeg.addCuboid(2F, -12F, 0F, 5, 12, 5);
        overRightLeg.setRotationPoint(-4.5F, 12F, -2.5F);
        overRightLeg.setTextureSize(128, 64);
        overRightLeg.mirror = true;
        setRotation(overRightLeg, 0F, 0F, 0F);

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
        bipedBody.addCuboid(-4.5F, 0F, -2.5F, 9, 12, 5);
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

        bipedLeftArm = new ModelRenderer(this, 44, 16);
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 44, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        pouch = new ModelRenderer(this, 20, 33);
        pouch.addCuboid(0F, 0F, 0F, 4, 3, 1);
        pouch.setRotationPoint(-4F, 9.5F, -3.5F);
        pouch.setTextureSize(128, 64);
        pouch.mirror = true;
        setRotation(pouch, 0F, 0F, 0F);

        bipedBody.addChild(pouch);
        bipedLeftLeg.addChild(overLeftLeg);
        bipedRightLeg.addChild(overRightLeg);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
