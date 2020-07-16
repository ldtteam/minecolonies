// Made with Blockbench 3.5.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityConcreteMixerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityConcreteMixerMale()
    {
        ModelRenderer mask;
        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(9, 56).addBox(-4.0F, -0.75F, 1.02F, 0.0F, 1.0F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(33, 50).addBox(4.25F, -0.75F, 1.02F, 0.0F, 1.0F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(32, 61).addBox(-4.0F, -2.0F, 1.02F, 0.0F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(28, 61).addBox(4.25F, -2.0F, 1.02F, 0.0F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(34, 45).addBox(4.35F, -2.0F, 3.02F, 0.0F, 2.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(38, 45).addBox(4.35F, -8.0F, 0.02F, 0.0F, 6.0F, 4.0F, 0.0F, false);
        bipedHead.setTextureOffset(41, 56).addBox(3.35F, -7.5F, -4.48F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(24, 45).addBox(4.35F, -7.75F, -3.48F, 0.0F, 3.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(29, 45).addBox(4.35F, -8.0F, -1.73F, 0.0F, 4.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(34, 48).addBox(4.35F, -2.0F, 2.02F, 0.0F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(1, 62).addBox(-4.0F, -2.0F, 2.02F, 0.0F, 1.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(1, 55).addBox(-4.0F, -8.0F, -1.73F, 0.0F, 4.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(2, 46).addBox(-4.0F, -7.75F, -3.48F, 0.0F, 3.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(22, 56).addBox(-3.75F, -7.5F, -4.48F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(28, 56).addBox(-2.25F, -8.0F, -4.48F, 5.0F, 2.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 46).addBox(-4.0F, -8.0F, -3.48F, 8.0F, 2.0F, 7.0F, 0.0F, true);
        bipedHead.setTextureOffset(47, 49).addBox(-3.75F, -7.5F, 4.02F, 8.0F, 6.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(22, 61).addBox(4.25F, -1.0F, 4.02F, 0.0F, 1.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(25, 61).addBox(-3.75F, -1.0F, 4.02F, 0.0F, 1.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(5, 62).addBox(-3.25F, -0.75F, 4.02F, 7.0F, 1.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(47, 53).addBox(-4.0F, -8.0F, 0.02F, 0.0F, 6.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(6, 56).addBox(-4.0F, -2.0F, 3.02F, 0.0F, 2.0F, 1.0F, 0.0F, true);

        mask = new ModelRenderer(this);
        mask.setRotationPoint(6.0F, -0.5F, -5.0F);
        bipedHead.addChild(mask);
        mask.setTextureOffset(74, 0).addBox(-10.0F, -1.5F, 0.77F, 0.0F, 1.0F, 6.0F, 0.0F, false);
        mask.setTextureOffset(82, 9).addBox(-10.0F, -2.25F, 0.75F, 3.0F, 3.0F, 0.0F, 0.0F, false);
        mask.setTextureOffset(90, 1).addBox(-7.5F, -3.0F, 0.76F, 3.0F, 4.0F, 0.0F, 0.0F, false);
        mask.setTextureOffset(90, 7).addBox(-5.0F, -2.25F, 0.75F, 3.0F, 3.0F, 0.0F, 0.0F, false);
        mask.setTextureOffset(90, 12).addBox(-9.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.0F, 0.0F, false);
        mask.setTextureOffset(88, 16).addBox(-5.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.0F, 0.0F, false);
        mask.setTextureOffset(100, 1).addBox(-1.72F, -1.5F, 0.77F, 0.0F, 1.0F, 6.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(0, 32).addBox(-0.5F, 5.75F, -2.25F, 5.0F, 0.0F, 4.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(20, 32).addBox(-3.5F, 5.75F, -2.25F, 5.0F, 0.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
