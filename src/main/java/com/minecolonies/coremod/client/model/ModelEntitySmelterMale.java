package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * The smelter male model.
 */
public class ModelEntitySmelterMale extends CitizenModel
{
    /**
     * Constructor which generates the model.
     */
    public ModelEntitySmelterMale()
    {
        ModelRenderer toolHandle1;
        ModelRenderer toolHandle2;
        ModelRenderer pocket;

        textureWidth = 128;
        textureHeight = 64;

        toolHandle1 = new ModelRenderer(this, 10, 32);
        toolHandle1.addCuboid(0F, 0F, 0F, 1, 3, 1);
        toolHandle1.setRotationPoint(-1F, 5F, -3F);
        toolHandle1.setTextureSize(128, 64);
        toolHandle1.mirror = true;
        setRotation(toolHandle1, 0F, 0F, 0F);

        toolHandle2 = new ModelRenderer(this, 10, 32);
        toolHandle2.addCuboid(0F, 0F, 0F, 1, 2, 1);
        toolHandle2.setRotationPoint(1F, 6F, -3F);
        toolHandle2.setTextureSize(128, 64);
        toolHandle2.mirror = true;
        setRotation(toolHandle2, 0F, 0F, 0F);

        pocket = new ModelRenderer(this, 0, 32);
        pocket.addCuboid(0F, 0F, 0F, 4, 3, 1);
        pocket.setRotationPoint(-2F, 8F, -3F);
        pocket.setTextureSize(128, 64);
        pocket.mirror = true;
        setRotation(pocket, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addCuboid(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addCuboid(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

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
        bipedBody.addCuboid(-4F, 0F, -2F, 8, 12, 4);
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

        this.bipedBody.addChild(toolHandle1);
        this.bipedBody.addChild(toolHandle2);
        this.bipedBody.addChild(pocket);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
