package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.RendererModel;
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
        RendererModel toolHandle1;
        RendererModel toolHandle2;
        RendererModel pocket;

        textureWidth = 128;
        textureHeight = 64;

        toolHandle1 = new RendererModel(this, 10, 32);
        toolHandle1.addBox(0F, 0F, 0F, 1, 3, 1);
        toolHandle1.setRotationPoint(-1F, 5F, -3F);
        toolHandle1.setTextureSize(128, 64);
        toolHandle1.mirror = true;
        setRotation(toolHandle1, 0F, 0F, 0F);

        toolHandle2 = new RendererModel(this, 10, 32);
        toolHandle2.addBox(0F, 0F, 0F, 1, 2, 1);
        toolHandle2.setRotationPoint(1F, 6F, -3F);
        toolHandle2.setTextureSize(128, 64);
        toolHandle2.mirror = true;
        setRotation(toolHandle2, 0F, 0F, 0F);

        pocket = new RendererModel(this, 0, 32);
        pocket.addBox(0F, 0F, 0F, 4, 3, 1);
        pocket.setRotationPoint(-2F, 8F, -3F);
        pocket.setTextureSize(128, 64);
        pocket.mirror = true;
        setRotation(pocket, 0F, 0F, 0F);

        bipedRightArm = new RendererModel(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 64);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new RendererModel(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 64);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightLeg = new RendererModel(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 64);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new RendererModel(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 64);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new RendererModel(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 64);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new RendererModel(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 64);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        this.bipedBody.addChild(toolHandle1);
        this.bipedBody.addChild(toolHandle2);
        this.bipedBody.addChild(pocket);

        bipedHeadwear.isHidden = true;
    }

    @Override
    public void render(
      @NotNull final AbstractEntityCitizen entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    private void setRotation(@NotNull final RendererModel model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
