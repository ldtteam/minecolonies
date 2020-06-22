package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityConcreteMixerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityConcreteMixerFemale()
    {
        final ModelRenderer mask;
        final ModelRenderer bone;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.25F, -1.0F, 1.02F, 0.25F, 3.25F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -1.0F, 1.02F, 0.25F, 3.25F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(-4.25F, -2.0F, 1.02F, 0.25F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -2.0F, 1.02F, 0.25F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -2.0F, 3.02F, 0.35F, 6.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -8.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(2.75F, -8.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -8.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -8.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(4.0F, -2.0F, 2.02F, 0.35F, 1.0F, 1.0F, 0.0F, false);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -2.0F, 2.02F, 0.35F, 1.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -8.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -8.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -8.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-2.75F, -8.25F, -4.48F, 5.5F, 2.25F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.0F, -8.25F, -3.48F, 8.0F, 2.25F, 7.5F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.25F, -8.25F, 4.02F, 8.5F, 6.75F, 0.25F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(3.75F, -1.5F, 4.02F, 0.5F, 5.5F, 0.25F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.25F, -1.5F, 4.02F, 0.5F, 5.5F, 0.25F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-3.75F, -1.5F, 3.02F, 7.5F, 7.75F, 1.25F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -8.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 49).addBox(-4.35F, -2.0F, 3.02F, 0.35F, 6.0F, 1.0F, 0.0F, true);

        mask = new ModelRenderer(this);
        mask.setRotationPoint(6.0F, -0.5F, -5.0F);
        bipedHead.addChild(mask);
        mask.setTextureOffset(74, 49).addBox(-10.28F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-10.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-7.5F, -3.0F, 0.76F, 3.0F, 4.0F, 0.75F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-5.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-9.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-5.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
        mask.setTextureOffset(74, 49).addBox(-2.0F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(0, 32).addBox(-0.5F, 5.0F, -2.25F, 5.0F, 0.75F, 4.5F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 32).addBox(-3.5F, 5.0F, -2.25F, 5.0F, 0.75F, 4.5F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        bone = new ModelRenderer(this);
        bone.setRotationPoint(3.0F, 2.5F, -2.0F);
        bipedBody.addChild(bone);
        setRotationAngle(bone, 0.7854F, 0.0F, 0.0F);
        bone.setTextureOffset(18, 17).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
