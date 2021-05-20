package com.minecolonies.coremod.client.model.basemodels;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.modularcitizen.ModelRenderer;

public class ModelEntityBaseKobold extends CitizenModel
{
    public ModelEntityBaseKobold()
    {
        textureWidth = 64;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 6.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 6.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 14.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 14.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
