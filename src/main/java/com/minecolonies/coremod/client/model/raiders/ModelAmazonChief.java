package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.AmazonModel;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Amazon Chief model.
 */
public class ModelAmazonChief extends AmazonModel<AbstractEntityAmazon>
{
    /**
     * Create an instance of it.
     */
    public ModelAmazonChief()
    {
        ModelRenderer hairBack1;
        ModelRenderer hat;
        ModelRenderer nehat;
        ModelRenderer mask;
        ModelRenderer bone2;
        ModelRenderer feather;
        ModelRenderer feather2;
        ModelRenderer feather3;
        ModelRenderer feather5;
        ModelRenderer feather6;
        ModelRenderer feather7;
        ModelRenderer feather4;
        ModelRenderer hairback;
        ModelRenderer bone;
        ModelRenderer chest;

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
        hairBack1.texOffs(74, 14).addBox(-5.0F, -7.2F, -4.8F, 10.0F, 2.0F, 6.0F, 0.0F, false);
        hairBack1.texOffs(74, 14).addBox(-2.0F, -7.2F, -5.3F, 4.0F, 2.0F, 6.0F, 0.0F, false);
        hairBack1.texOffs(28, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(48, 13).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(45, 24).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.texOffs(24, 26).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, false);
        hairBack1.texOffs(48, 0).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, false);

        hat = new ModelRenderer(this);
        hat.setPos(0.1F, -6.8F, -4.3F);
        hairBack1.addChild(hat);
        setRotationAngle(hat, -0.5585F, 0.0F, 0.0F);
        hat.texOffs(80, 14).addBox(-4.6F, -2.961F, -1.1223F, 9.0F, 3.0F, 1.0F, 0.0F, false);

        nehat = new ModelRenderer(this);
        nehat.setPos(0.4F, -1.9F, -1.0F);
        hat.addChild(nehat);
        setRotationAngle(nehat, -0.1745F, 0.0F, 0.0F);
        nehat.texOffs(104, 15).addBox(3.0F, -3.1545F, -0.4684F, 1.0F, 4.0F, 1.0F, 0.0F, false);
        nehat.texOffs(113, 13).addBox(1.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, 0.0F, false);
        nehat.texOffs(122, 12).addBox(-1.0F, -9.1545F, -0.4684F, 1.0F, 10.0F, 1.0F, 0.0F, false);
        nehat.texOffs(124, 23).addBox(-3.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, 0.0F, false);
        nehat.texOffs(106, 32).addBox(-5.0F, -3.192F, -0.3757F, 1.0F, 4.0F, 1.0F, 0.0F, false);

        mask = new ModelRenderer(this);
        mask.setPos(0.0F, -5.7F, -4.7F);
        hairBack1.addChild(mask);
        mask.texOffs(83, 22).addBox(-3.0F, 0.5F, -0.1F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        mask.texOffs(104, 34).addBox(-1.0F, 1.5F, -0.1F, 2.0F, 4.0F, 1.0F, 0.0F, false);
        mask.texOffs(100, 47).addBox(-2.0F, 2.5F, -0.1F, 4.0F, 2.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(-3.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(2.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.texOffs(114, 45).addBox(-3.0F, 3.5F, -0.7F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(-4.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(3.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(-2.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.texOffs(116, 33).addBox(1.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);

        bone2 = new ModelRenderer(this);
        bone2.setPos(-3.5F, 33.0F, 3.8F);
        mask.addChild(bone2);


        feather = new ModelRenderer(this);
        feather.setPos(-4.8F, -7.0F, -1.0F);
        hairBack1.addChild(feather);
        setRotationAngle(feather, 0.7854F, 0.0F, 0.0F);
        feather.texOffs(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather.texOffs(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather.texOffs(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather2 = new ModelRenderer(this);
        feather2.setPos(4.6F, -7.0F, -1.0F);
        hairBack1.addChild(feather2);
        setRotationAngle(feather2, 0.7854F, 0.0F, 0.0F);
        feather2.texOffs(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather2.texOffs(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather2.texOffs(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather2.texOffs(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather2.texOffs(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather3 = new ModelRenderer(this);
        feather3.setPos(4.0F, -9.7F, -4.0F);
        hairBack1.addChild(feather3);
        setRotationAngle(feather3, 0.0F, 0.6981F, -1.5708F);
        feather3.texOffs(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather3.texOffs(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather3.texOffs(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather3.texOffs(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather3.texOffs(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather5 = new ModelRenderer(this);
        feather5.setPos(2.0F, -11.7F, -2.0F);
        hairBack1.addChild(feather5);
        setRotationAngle(feather5, 0.0F, 0.6981F, -1.5708F);
        feather5.texOffs(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather5.texOffs(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather5.texOffs(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather5.texOffs(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather5.texOffs(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather6 = new ModelRenderer(this);
        feather6.setPos(-2.0F, -11.7F, -2.0F);
        hairBack1.addChild(feather6);
        setRotationAngle(feather6, 0.0F, 0.6981F, -1.5708F);
        feather6.texOffs(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather6.texOffs(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather6.texOffs(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather6.texOffs(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather6.texOffs(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather7 = new ModelRenderer(this);
        feather7.setPos(0.0F, -13.7F, 0.0F);
        hairBack1.addChild(feather7);
        setRotationAngle(feather7, 0.0F, 0.6981F, -1.5708F);
        feather7.texOffs(0, 104).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather7.texOffs(0, 104).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather7.texOffs(0, 104).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather7.texOffs(0, 104).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather7.texOffs(0, 104).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather4 = new ModelRenderer(this);
        feather4.setPos(-4.0F, -9.7F, -3.6F);
        hairBack1.addChild(feather4);
        setRotationAngle(feather4, 0.0F, 0.6981F, -1.5708F);
        feather4.texOffs(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather4.texOffs(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather4.texOffs(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather4.texOffs(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather4.texOffs(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

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

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 0.0F, 0.0F);
        rightArm.texOffs(43, 33).addBox(-2.5F, 6.0F, -2.5F, 4.0F, 1.0F, 5.0F, 0.0F, false);
        rightArm.texOffs(0, 43).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 0.0F, 0.0F);
        leftArm.texOffs(40, 40).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 9.0F, 0.0F);
        leftLeg.texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        leftLeg.texOffs(32, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 9.0F, 0.0F);
        rightLeg.texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        rightLeg.texOffs(24, 38).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
