package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * The smelter female model.
 */
public class ModelEntitySmelterFemale extends CitizenModel<AbstractEntityCitizen>
{
    /**
     * Constructor which generates the model.
     */
    public ModelEntitySmelterFemale()
    {
        ModelRenderer toolHandle1;
        ModelRenderer toolHandle2;
        ModelRenderer pocket;

        ModelRenderer bipedChest;

        ModelRenderer ponytailB;
        ModelRenderer ponytailT;

        textureWidth = 128;
        textureHeight = 64;

        toolHandle1 = new ModelRenderer(this, 0, 32);
        toolHandle1.addBox(0F, 0F, 0F, 4, 3, 1);
        toolHandle1.setRotationPoint(-2F, 8F, -3F);
        toolHandle1.setTextureSize(128, 64);
        toolHandle1.mirror = true;
        setRotation(toolHandle1, 0F, 0F, 0F);

        toolHandle2 = new ModelRenderer(this, 10, 32);
        toolHandle2.addBox(0F, 0F, 0F, 1, 2, 1);
        toolHandle2.setRotationPoint(-1F, 6F, -3F);
        toolHandle2.setTextureSize(128, 64);
        toolHandle2.mirror = true;
        setRotation(toolHandle2, 0F, 0F, 0F);

        pocket = new ModelRenderer(this, 10, 32);
        pocket.addBox(0F, 0F, 0F, 1, 2, 1);
        pocket.setRotationPoint(1F, 6F, -3F);
        pocket.setTextureSize(128, 64);
        pocket.mirror = true;
        setRotation(pocket, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);
        bipedLeftArm.mirror = false;

        bipedChest = new ModelRenderer(this, 0, 55);
        bipedChest.addBox(-3.5F, 2.7F, -0.6F, 7, 3, 4);
        bipedChest.setRotationPoint(0F, 0F, 0F);
        bipedChest.setTextureSize(128, 64);
        bipedChest.mirror = true;
        setRotation(bipedChest, -0.5934119F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -7F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, -1F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        ponytailB = new ModelRenderer(this, 80, 40);
        ponytailB.addBox(-0.5F, 2.4F, 3.7F, 1, 5, 1);
        ponytailB.setRotationPoint(0F, 0F, 0F);
        ponytailB.setTextureSize(128, 64);
        ponytailB.mirror = true;
        setRotation(ponytailB, 0.1047198F, 0F, 0F);

        ponytailT = new ModelRenderer(this, 79, 33);
        ponytailT.addBox(-1F, -2F, 3.4F, 2, 5, 1);
        ponytailT.setRotationPoint(0F, 0F, 0F);
        ponytailT.setTextureSize(128, 64);
        ponytailT.mirror = true;
        setRotation(ponytailT, 0.2268928F, 0F, 0F);

        this.bipedBody.addChild(toolHandle1);
        this.bipedBody.addChild(toolHandle2);
        this.bipedBody.addChild(pocket);
        this.bipedBody.addChild(bipedChest);

        this.bipedHead.addChild(ponytailB);
        this.bipedHead.addChild(ponytailT);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
