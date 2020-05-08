package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.AmazonModel;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * General amazon model.
 */
public class ModelAmazon extends AmazonModel<AbstractEntityAmazon>
{
    /**
     * Create an instance of it.
     */
    public ModelAmazon()
    {
        ModelRenderer hairBack1;
        ModelRenderer feather;
        ModelRenderer hairback;
        ModelRenderer bone;
        ModelRenderer chest;
        ModelRenderer handstuff;

        textureWidth = 128;
        textureHeight = 128;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, -3.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack1);
        hairBack1.setTextureOffset(4, 0).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairBack1.setTextureOffset(33, 30).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        hairBack1.setTextureOffset(0, 16).addBox(-5.0F, -7.2F, -4.8F, 10.0F, 2.0F, 8.0F, 0.0F, false);
        hairBack1.setTextureOffset(23, 26).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(56, 44).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(52, 39).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(24, 26).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, false);
        hairBack1.setTextureOffset(24, 29).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, false);

        feather = new ModelRenderer(this);
        feather.setRotationPoint(-9.3F, -8.0F, -0.1F);
        hairBack1.addChild(feather);
        setRotationAngle(feather, 0.7854F, 0.0F, 0.0F);
        feather.setTextureOffset(104, 0).addBox(4.4F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(4.6F, -0.9343F, -0.9343F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(4.6F, -0.1464F, -0.5808F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(4.6F, -0.7121F, -1.7222F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather.setTextureOffset(104, 0).addBox(4.7F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        hairback = new ModelRenderer(this);
        hairback.setRotationPoint(0.0F, -7.0F, 3.0F);
        hairBack1.addChild(hairback);
        setRotationAngle(hairback, -0.5236F, 0.0F, 0.0F);
        hairback.setTextureOffset(35, 31).addBox(-0.5F, -3.0314F, -0.5411F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(16, 20).addBox(-0.5F, -5.8046F, 0.3981F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(35, 29).addBox(-0.5F, -1.2233F, 0.6553F, 1.0F, 5.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(0, 22).addBox(-0.5F, -3.869F, 0.9776F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(3, 21).addBox(-0.5F, -3.5984F, -0.2911F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(14, 43).addBox(-0.5F, -5.3135F, 0.4115F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, -4.5F, 1.5981F);
        hairback.addChild(bone);
        setRotationAngle(bone, 0.5236F, 0.0F, 0.0F);
        bone.setTextureOffset(29, 28).addBox(-0.5F, -1.3154F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(29, 29).addBox(-0.5F, 1.9346F, -0.2545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(35, 30).addBox(-0.5F, 3.9346F, -0.9545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(4, 16).addBox(-0.5F, 5.9346F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(74, 0).addBox(-3.5F, 6.4F, -2.6F, 7.0F, 1.0F, 5.0F, 0.0F, false);
        bipedBody.setTextureOffset(0, 63).addBox(-4.5F, 7.4F, -2.6F, 9.0F, 7.0F, 5.0F, 0.0F, false);
        bipedBody.setTextureOffset(0, 26).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 13.0F, 4.0F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.632F, 0.0F, 0.0F);
        chest.setTextureOffset(48, 7).addBox(-3.5F, 0.2727F, -2.3632F, 7.0F, 3.0F, 3.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 9.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(10, 19).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        bipedLeftLeg.setTextureOffset(32, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 9.0F, 0.0F);
        bipedRightLeg.setTextureOffset(10, 18).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(24, 38).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 0.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 40).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 0.0F, 0.0F);
        bipedRightArm.setTextureOffset(0, 43).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        handstuff = new ModelRenderer(this);
        handstuff.setRotationPoint(5.0F, 24.0F, 0.0F);
        bipedRightArm.addChild(handstuff);
        handstuff.setTextureOffset(43, 33).addBox(-7.5F, -18.0F, -2.5F, 4.0F, 1.0F, 5.0F, 0.0F, false);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
