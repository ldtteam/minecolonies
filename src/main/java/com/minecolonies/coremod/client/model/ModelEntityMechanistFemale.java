package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMechanistFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMechanistFemale()
    {
        final ModelRenderer chest;
        final ModelRenderer mask;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        bipedBody.setTextureOffset(70, 22).addBox(1.0F, 10.55F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        bipedBody.setTextureOffset(18, 46).addBox(-0.5F, 10.0F, -4.0F, 4.0F, 3.0F, 2.0F, 0.0F, true);
        bipedBody.setTextureOffset(1, 44).addBox(-4.5F, 11.0F, -3.0F, 9.0F, 2.0F, 6.0F, 0.0F, true);
        bipedBody.setTextureOffset(69, 53).addBox(0.0F, 8.75F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(69, 53).addBox(1.25F, 8.75F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(69, 53).addBox(2.5F, 8.75F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(68, 53).addBox(2.25F, 8.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 53).addBox(1.0F, 8.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 53).addBox(-0.25F, 8.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 45).addBox(0.0F, 7.75F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(68, 45).addBox(1.25F, 7.75F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(68, 45).addBox(2.5F, 7.75F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(78, 52).addBox(2.25F, 8.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 52).addBox(1.0F, 8.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 52).addBox(-0.25F, 8.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 2.0F, -11.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, 0.7854F, 0.0F, 0.0F);
        chest.setTextureOffset(23, 19).addBox(-3.0F, 5.5566F, 4.181F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(72, 0).addBox(-2.0F, 6.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(65, 2).addBox(-1.5F, 7.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(72, 0).addBox(-3.5F, 7.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(72, 0).addBox(-4.0F, 6.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 44).addBox(-4.5F, -8.0F, -4.25F, 1.0F, 4.25F, 8.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(3.5F, -8.0F, -4.25F, 1.0F, 4.25F, 8.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.5F, -4.0F, 1.75F, 1.0F, 3.0F, 2.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(3.5F, -4.0F, 1.75F, 1.0F, 3.0F, 2.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.5F, -4.0F, -2.0F, 1.0F, 2.25F, 3.75F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(3.5F, -4.0F, -2.75F, 1.0F, 2.25F, 4.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.0F, 3.75F, 8.5F, 8.0F, 0.75F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.25F, -4.25F, 8.5F, 0.5F, 8.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.0F, -4.75F, 2.5F, 1.75F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(1.75F, -8.0F, -4.75F, 2.5F, 1.75F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-1.75F, -8.0F, -4.75F, 3.5F, 1.0F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-1.75F, -7.5F, -4.75F, 1.0F, 1.0F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -6.75F, -4.75F, 2.5F, 1.25F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(2.5F, -6.75F, -4.75F, 1.75F, 1.25F, 0.5F, 0.0F, false);

        mask = new ModelRenderer(this);
        mask.setRotationPoint(0.0F, 24.25F, -0.5F);
        bipedHead.addChild(mask);
        mask.setTextureOffset(55, 0).addBox(3.64F, -30.68F, -4.5F, 1.04F, 3.12F, 5.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.68F, -32.75F, -4.5F, 9.36F, 1.04F, 3.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.68F, -27.56F, -4.5F, 1.04F, 2.08F, 4.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.68F, -32.75F, -4.5F, 1.04F, 2.08F, 4.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.68F, -30.68F, -4.5F, 1.04F, 3.12F, 5.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.68F, -25.48F, -4.5F, 9.36F, 1.04F, 3.0F, 0.0F, true);
        mask.setTextureOffset(33, 1).addBox(1.04F, -30.68F, -5.0F, 2.08F, 2.08F, 1.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.16F, -28.6F, -5.0F, 8.32F, 3.12F, 1.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-1.04F, -30.68F, -5.0F, 2.08F, 2.08F, 1.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(3.12F, -30.68F, -5.0F, 1.04F, 2.08F, 1.0F, 0.0F, true);
        mask.setTextureOffset(33, 1).addBox(-3.12F, -30.68F, -5.0F, 2.08F, 2.08F, 1.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.16F, -32.75F, -5.0F, 8.32F, 2.08F, 1.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(3.64F, -27.56F, -4.5F, 1.04F, 2.08F, 4.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(3.64F, -32.75F, -4.5F, 1.04F, 2.08F, 4.0F, 0.0F, true);
        mask.setTextureOffset(55, 0).addBox(-4.16F, -30.68F, -5.0F, 1.04F, 2.08F, 1.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
