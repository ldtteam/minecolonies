package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelEntityDeliverymanMale extends CitizenModel
{
    public ModelRenderer backpack;

    public ModelEntityDeliverymanMale()
    {
        final float scale = 0F;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addCuboid(-4F, -8F, -4F, 8, 8, 8, scale);
        bipedHead.setRotationPoint(0F, 2F, -4F);
        bipedHead.rotateAngleX = 0.34907F;

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addCuboid(-4F, 0F, -4F, 8, 12, 4, scale);
        bipedBody.setRotationPoint(0F, 1F, -2F);
        bipedBody.rotateAngleX = 0.34907F;
        bipedBody.rotateAngleZ = 0F;

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(0F, 0F, -2F, 4, 12, 4, scale);
        bipedLeftArm.setRotationPoint(4F, 2F, -4F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-4F, 0F, -2F, 4, 12, 4, scale);
        bipedRightArm.setRotationPoint(-4F, 2F, -4F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4, scale);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addCuboid(-2F, 0F, -2F, 4, 12, 4, scale);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);

        backpack = new ModelRenderer(this, 32, 0);
        backpack.addCuboid(-4F, 0F, 0F, 8, 10, 6, scale);
        backpack.setRotationPoint(0F, 1F, -2F);
        backpack.rotateAngleX = 0.34907F;

        bipedBody.addChild(backpack);
    }
}
