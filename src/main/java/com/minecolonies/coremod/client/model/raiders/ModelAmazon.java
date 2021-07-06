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

        texWidth = 128;
        texHeight = 128;

        head = new ModelRenderer(this);
        head.setPos(0.0F, -3.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack1);
        hairBack1.texOffs(24, 4).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairBack1.texOffs(24, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        hairBack1.texOffs(0, 16).addBox(-5.0F, -7.2F, -4.8F, 10.0F, 2.0F, 8.0F, 0.0F, false);
        hairBack1.texOffs(28, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(48, 13).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(45, 24).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(24, 26).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, false);
        hairBack1.texOffs(48, 0).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, false);

        feather = new ModelRenderer(this);
        feather.setPos(-9.3F, -8.0F, -0.1F);
        hairBack1.addChild(feather);
        setRotationAngle(feather, 0.7854F, 0.0F, 0.0F);
        feather.texOffs(104, 0).addBox(4.4F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(4.6F, -0.9343F, -0.9343F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(4.6F, -0.1464F, -0.5808F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(4.6F, -0.7121F, -1.7222F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather.texOffs(104, 0).addBox(4.7F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        hairback = new ModelRenderer(this);
        hairback.setPos(0.0F, -7.0F, 3.0F);
        hairBack1.addChild(hairback);
        setRotationAngle(hairback, -0.5236F, 0.0F, 0.0F);
        hairback.texOffs(3, 5).addBox(-0.5F, -3.0314F, -0.5411F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairback.texOffs(0, 26).addBox(-0.5F, -5.8046F, 0.3981F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.texOffs(0, 0).addBox(-0.5F, -1.2233F, 0.6553F, 1.0F, 5.0F, 1.0F, 0.0F, false);
        hairback.texOffs(20, 26).addBox(-0.5F, -3.869F, 0.9776F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.texOffs(23, 27).addBox(-0.5F, -3.5984F, -0.2911F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.texOffs(14, 43).addBox(-0.5F, -5.3135F, 0.4115F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setPos(0.0F, -4.5F, 1.5981F);
        hairback.addChild(bone);
        setRotationAngle(bone, 0.5236F, 0.0F, 0.0F);
        bone.texOffs(0, 16).addBox(-0.5F, -1.3154F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);
        bone.texOffs(3, 20).addBox(-0.5F, 1.9346F, -0.2545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.texOffs(4, 16).addBox(-0.5F, 3.9346F, -0.9545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.texOffs(4, 0).addBox(-0.5F, 5.9346F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(74, 0).addBox(-3.5F, 6.4F, -2.6F, 7.0F, 1.0F, 5.0F, 0.0F, false);
        body.texOffs(0, 63).addBox(-4.5F, 7.4F, -2.6F, 9.0F, 7.0F, 5.0F, 0.0F, false);
        body.texOffs(0, 26).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 13.0F, 4.0F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.632F, 0.0F, 0.0F);
        chest.texOffs(48, 7).addBox(-3.5F, 0.2727F, -2.3632F, 7.0F, 3.0F, 3.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 9.0F, 0.0F);
        leftLeg.texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        leftLeg.texOffs(32, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 9.0F, 0.0F);
        rightLeg.texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        rightLeg.texOffs(24, 38).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 0.0F, 0.0F);
        leftArm.texOffs(40, 40).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 0.0F, 0.0F);
        rightArm.texOffs(0, 43).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        handstuff = new ModelRenderer(this);
        handstuff.setPos(5.0F, 24.0F, 0.0F);
        rightArm.addChild(handstuff);
        handstuff.texOffs(43, 33).addBox(-7.5F, -18.0F, -2.5F, 4.0F, 1.0F, 5.0F, 0.0F, false);

        hat.visible = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
