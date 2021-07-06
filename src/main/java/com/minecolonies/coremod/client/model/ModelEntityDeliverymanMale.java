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

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -7.4F, -4F, 8, 8, 8);
        head.setPos(0F, 2F, -4F);
        head.mirror = true;
        setRotation(head, 0.3490659F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -4F, 8, 12, 4);
        body.setPos(0F, 1F, -2F);

        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.addBox(-1F, -0.5F, -4.9F, 4, 12, 4, 0F);
        leftArm.setPos(4F, 2F, -4F);
        leftArm.setTexSize(256, 128);
        leftArm.mirror = true;

        rightArm = new ModelRenderer(this, 40, 16);
        rightArm.addBox(-3F, -0.5F, -4.9F, 4, 12, 4, 0F);
        rightArm.setPos(-4F, 2F, -4F);
        rightArm.setTexSize(256, 128);
        rightArm.mirror = true;

        rightLeg = new ModelRenderer(this, 0, 16);
        rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightLeg.setPos(-2F, 12F, 0F);

        leftLeg = new ModelRenderer(this, 0, 16);
        leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftLeg.setPos(2F, 12F, 0F);

        backpack = new ModelRenderer(this, 32, 0);
        backpack.addBox(-4F, 0F, 0F, 8, 10, 6);
        backpack.setPos(0F, 1F, -2F);
        backpack.xRot = 0.34907F;

        body.addChild(backpack);
        hat.visible = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    @Override
    public float getActualRotation()
    {
        return 0.34907F;
    }
}
