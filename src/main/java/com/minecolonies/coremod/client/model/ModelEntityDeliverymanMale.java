package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityDeliverymanMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityDeliverymanMale()
    {
        final ModelRenderer backpack;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -7.4F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 2F, -4F);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0.3490659F, 0F, 0F);
        
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -4F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 1F, -2F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(0F, 0F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(4F, 2F, -4F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-4F, 0F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-4F, 2F, -4F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);

        backpack = new ModelRenderer(this, 32, 0);
        backpack.addCuboid(-4F, 0F, 0F, 8, 10, 6);
        backpack.setRotationPoint(0F, 1F, -2F);
        backpack.rotateAngleX = 0.34907F;

        bipedBody.addChild(backpack);
        bipedHeadwear.showModel=false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public float getActualRotation()
    {
        return 0.34907F;
    }
}
