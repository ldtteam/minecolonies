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

        textureWidth = 128;
        textureHeight = 128;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, -3.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack1);
        hairBack1.setTextureOffset(24, 4).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairBack1.setTextureOffset(24, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        hairBack1.setTextureOffset(74, 14).addBox(-5.0F, -7.2F, -4.8F, 10.0F, 2.0F, 6.0F, 0.0F, false);
        hairBack1.setTextureOffset(74, 14).addBox(-2.0F, -7.2F, -5.3F, 4.0F, 2.0F, 6.0F, 0.0F, false);
        hairBack1.setTextureOffset(28, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(48, 13).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(45, 24).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        hairBack1.setTextureOffset(24, 26).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, false);
        hairBack1.setTextureOffset(48, 0).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, false);

        hat = new ModelRenderer(this);
        hat.setRotationPoint(0.1F, -6.8F, -4.3F);
        hairBack1.addChild(hat);
        setRotationAngle(hat, -0.5585F, 0.0F, 0.0F);
        hat.setTextureOffset(80, 14).addBox(-4.6F, -2.961F, -1.1223F, 9.0F, 3.0F, 1.0F, 0.0F, false);

        nehat = new ModelRenderer(this);
        nehat.setRotationPoint(0.4F, -1.9F, -1.0F);
        hat.addChild(nehat);
        setRotationAngle(nehat, -0.1745F, 0.0F, 0.0F);
        nehat.setTextureOffset(104, 15).addBox(3.0F, -3.1545F, -0.4684F, 1.0F, 4.0F, 1.0F, 0.0F, false);
        nehat.setTextureOffset(113, 13).addBox(1.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, 0.0F, false);
        nehat.setTextureOffset(122, 12).addBox(-1.0F, -9.1545F, -0.4684F, 1.0F, 10.0F, 1.0F, 0.0F, false);
        nehat.setTextureOffset(124, 23).addBox(-3.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, 0.0F, false);
        nehat.setTextureOffset(106, 32).addBox(-5.0F, -3.192F, -0.3757F, 1.0F, 4.0F, 1.0F, 0.0F, false);

        mask = new ModelRenderer(this);
        mask.setRotationPoint(0.0F, -5.7F, -4.7F);
        hairBack1.addChild(mask);
        mask.setTextureOffset(83, 22).addBox(-3.0F, 0.5F, -0.1F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(104, 34).addBox(-1.0F, 1.5F, -0.1F, 2.0F, 4.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(100, 47).addBox(-2.0F, 2.5F, -0.1F, 4.0F, 2.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(-3.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(2.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(114, 45).addBox(-3.0F, 3.5F, -0.7F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(-4.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(3.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(-2.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        mask.setTextureOffset(116, 33).addBox(1.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);

        bone2 = new ModelRenderer(this);
        bone2.setRotationPoint(-3.5F, 33.0F, 3.8F);
        mask.addChild(bone2);


        feather = new ModelRenderer(this);
        feather.setRotationPoint(-4.8F, -7.0F, -1.0F);
        hairBack1.addChild(feather);
        setRotationAngle(feather, 0.7854F, 0.0F, 0.0F);
        feather.setTextureOffset(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather.setTextureOffset(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather.setTextureOffset(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather2 = new ModelRenderer(this);
        feather2.setRotationPoint(4.6F, -7.0F, -1.0F);
        hairBack1.addChild(feather2);
        setRotationAngle(feather2, 0.7854F, 0.0F, 0.0F);
        feather2.setTextureOffset(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather2.setTextureOffset(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather2.setTextureOffset(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, 0.0F, false);
        feather2.setTextureOffset(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather2.setTextureOffset(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather3 = new ModelRenderer(this);
        feather3.setRotationPoint(4.0F, -9.7F, -4.0F);
        hairBack1.addChild(feather3);
        setRotationAngle(feather3, 0.0F, 0.6981F, -1.5708F);
        feather3.setTextureOffset(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather3.setTextureOffset(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather3.setTextureOffset(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather3.setTextureOffset(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather3.setTextureOffset(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather5 = new ModelRenderer(this);
        feather5.setRotationPoint(2.0F, -11.7F, -2.0F);
        hairBack1.addChild(feather5);
        setRotationAngle(feather5, 0.0F, 0.6981F, -1.5708F);
        feather5.setTextureOffset(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather5.setTextureOffset(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather5.setTextureOffset(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather5.setTextureOffset(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather5.setTextureOffset(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather6 = new ModelRenderer(this);
        feather6.setRotationPoint(-2.0F, -11.7F, -2.0F);
        hairBack1.addChild(feather6);
        setRotationAngle(feather6, 0.0F, 0.6981F, -1.5708F);
        feather6.setTextureOffset(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather6.setTextureOffset(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather6.setTextureOffset(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather6.setTextureOffset(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather6.setTextureOffset(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather7 = new ModelRenderer(this);
        feather7.setRotationPoint(0.0F, -13.7F, 0.0F);
        hairBack1.addChild(feather7);
        setRotationAngle(feather7, 0.0F, 0.6981F, -1.5708F);
        feather7.setTextureOffset(0, 104).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather7.setTextureOffset(0, 104).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather7.setTextureOffset(0, 104).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather7.setTextureOffset(0, 104).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather7.setTextureOffset(0, 104).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        feather4 = new ModelRenderer(this);
        feather4.setRotationPoint(-4.0F, -9.7F, -3.6F);
        hairBack1.addChild(feather4);
        setRotationAngle(feather4, 0.0F, 0.6981F, -1.5708F);
        feather4.setTextureOffset(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);
        feather4.setTextureOffset(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, 0.0F, false);
        feather4.setTextureOffset(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather4.setTextureOffset(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, 0.0F, false);
        feather4.setTextureOffset(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, 0.0F, false);

        hairback = new ModelRenderer(this);
        hairback.setRotationPoint(0.0F, -7.0F, 3.0F);
        hairBack1.addChild(hairback);
        setRotationAngle(hairback, -0.5236F, 0.0F, 0.0F);
        hairback.setTextureOffset(3, 5).addBox(-0.5F, -3.0314F, -0.5411F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(0, 26).addBox(-0.5F, -5.8046F, 0.3981F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(0, 0).addBox(-0.5F, -1.2233F, 0.6553F, 1.0F, 5.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(20, 26).addBox(-0.5F, -3.869F, 0.9776F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(23, 27).addBox(-0.5F, -3.5984F, -0.2911F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        hairback.setTextureOffset(14, 43).addBox(-0.5F, -5.3135F, 0.4115F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, -4.5F, 1.5981F);
        hairback.addChild(bone);
        setRotationAngle(bone, 0.5236F, 0.0F, 0.0F);
        bone.setTextureOffset(0, 16).addBox(-0.5F, -1.3154F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(3, 20).addBox(-0.5F, 1.9346F, -0.2545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(4, 16).addBox(-0.5F, 3.9346F, -0.9545F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone.setTextureOffset(4, 0).addBox(-0.5F, 5.9346F, -0.6545F, 1.0F, 4.0F, 1.0F, 0.0F, false);

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

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 0.0F, 0.0F);
        bipedRightArm.setTextureOffset(43, 33).addBox(-2.5F, 6.0F, -2.5F, 4.0F, 1.0F, 5.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 43).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 0.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 40).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 9.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        bipedLeftLeg.setTextureOffset(32, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 9.0F, 0.0F);
        bipedRightLeg.setTextureOffset(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(24, 38).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
