package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * ModelPharaohMummy.
 * Created using Tabula 7.0.0
 */
public class ModelPharaoh extends EgyptianModel<AbstractEntityEgyptian>
{
    public ModelPharaoh()
    {
        ModelRenderer bodyJewel;
        ModelRenderer bodyGoldenStrip;
        ModelRenderer snakeBody;
        ModelRenderer snakeHead;
        ModelRenderer headRightSideTop;
        ModelRenderer headRightSideMiddle;
        ModelRenderer headRightSideBottom;
        ModelRenderer headLeftSideTop;
        ModelRenderer headLeftSideMiddle;
        ModelRenderer headLeftSideBottom;
        ModelRenderer headTail;
        ModelRenderer headTop;
        ModelRenderer headCap;
        ModelRenderer jaw;
        
        textureWidth = 128;
        textureHeight = 64;

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(24, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(24, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.3F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
        bipedLeftLeg.setTextureOffset(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftLeg.setTextureOffset(56, 20).addBox(-2.4F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F, 0.0F, true);
        bipedLeftLeg.setTextureOffset(40, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.2F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        bipedBody.setTextureOffset(44, 0).addBox(-5.5F, -0.2F, -2.5F, 11.0F, 5.0F, 5.0F, 0.15F, true);
        bipedBody.setTextureOffset(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.3F, true);
        bipedBody.setTextureOffset(52, 11).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 4.0F, 5.0F, 0.0F, true);

        bodyJewel = new ModelRenderer(this);
        bodyJewel.setRotationPoint(0.0F, 10.0F, -2.0F);
        bipedBody.addChild(bodyJewel);
        setRotationAngle(bodyJewel, 0.0F, 0.0F, 0.7854F);
        bodyJewel.setTextureOffset(0, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, -0.3F, true);

        bodyGoldenStrip = new ModelRenderer(this);
        bodyGoldenStrip.setRotationPoint(0.0F, 10.0F, -2.6F);
        bipedBody.addChild(bodyGoldenStrip);
        bodyGoldenStrip.setTextureOffset(38, 12).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 8.0F, 0.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.3F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
        bipedRightLeg.setTextureOffset(24, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(56, 20).addBox(-2.6F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(24, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.2F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, -1.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(80, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, 0.1F, true);
        bipedHead.setTextureOffset(38, 48).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 2.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(14, 59).addBox(-4.5F, -2.85F, 1.0F, 9.0F, 3.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 14).addBox(-2.5F, -2.0F, -3.5F, 5.0F, 1.0F, 0.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 10).addBox(-2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(10, 10).addBox(2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F, 0.0F, true);

        snakeBody = new ModelRenderer(this);
        snakeBody.setRotationPoint(0.0F, -7.5F, -4.6F);
        bipedHead.addChild(snakeBody);
        setRotationAngle(snakeBody, -0.3491F, 0.0F, 0.0F);
        snakeBody.setTextureOffset(76, 2).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        snakeHead = new ModelRenderer(this);
        snakeHead.setRotationPoint(0.0F, -10.65F, -4.5F);
        bipedHead.addChild(snakeHead);
        setRotationAngle(snakeHead, 0.2793F, 0.0F, 0.0F);
        snakeHead.setTextureOffset(77, 3).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 3.0F, 3.0F, -0.99F, true);

        headRightSideTop = new ModelRenderer(this);
        headRightSideTop.setRotationPoint(-4.6F, -11.4F, 0.86F);
        bipedHead.addChild(headRightSideTop);
        setRotationAngle(headRightSideTop, 0.0F, 0.0F, -0.8901F);
        headRightSideTop.setTextureOffset(0, 51).addBox(-5.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, 0.13F, false);

        headRightSideMiddle = new ModelRenderer(this);
        headRightSideMiddle.setRotationPoint(-7.82F, -7.11F, 0.86F);
        bipedHead.addChild(headRightSideMiddle);
        setRotationAngle(headRightSideMiddle, 0.0F, 0.0F, 0.2688F);
        headRightSideMiddle.setTextureOffset(14, 51).addBox(0.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, 0.14F, false);

        headRightSideBottom = new ModelRenderer(this);
        headRightSideBottom.setRotationPoint(-9.58F, -1.23F, 0.91F);
        bipedHead.addChild(headRightSideBottom);
        setRotationAngle(headRightSideBottom, 0.0F, -0.9147F, 0.2688F);
        headRightSideBottom.setTextureOffset(0, 57).addBox(0.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F, 0.0F, false);

        headLeftSideTop = new ModelRenderer(this);
        headLeftSideTop.setRotationPoint(4.6F, -11.4F, 0.86F);
        bipedHead.addChild(headLeftSideTop);
        setRotationAngle(headLeftSideTop, 0.0F, 0.0F, 0.8901F);
        headLeftSideTop.setTextureOffset(0, 51).addBox(0.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, 0.13F, true);

        headLeftSideMiddle = new ModelRenderer(this);
        headLeftSideMiddle.setRotationPoint(7.82F, -7.11F, 0.86F);
        bipedHead.addChild(headLeftSideMiddle);
        setRotationAngle(headLeftSideMiddle, 0.0F, 0.0F, -0.2688F);
        headLeftSideMiddle.setTextureOffset(14, 51).addBox(-5.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, 0.14F, true);

        headLeftSideBottom = new ModelRenderer(this);
        headLeftSideBottom.setRotationPoint(9.58F, -1.23F, 0.91F);
        bipedHead.addChild(headLeftSideBottom);
        setRotationAngle(headLeftSideBottom, 0.0F, 0.9147F, -0.2688F);
        headLeftSideBottom.setTextureOffset(0, 57).addBox(-3.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F, 0.0F, true);

        headTail = new ModelRenderer(this);
        headTail.setRotationPoint(1.5F, 0.4F, 2.5F);
        bipedHead.addChild(headTail);
        headTail.setTextureOffset(28, 51).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 2.0F, -0.2F, true);

        headTop = new ModelRenderer(this);
        headTop.setRotationPoint(0.0F, -8.55F, -4.45F);
        bipedHead.addChild(headTop);
        setRotationAngle(headTop, 0.4714F, 0.0F, 0.0F);
        headTop.setTextureOffset(76, 18).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 4.0F, 8.0F, 0.09F, true);

        headCap = new ModelRenderer(this);
        headCap.setRotationPoint(0.0F, -11.4F, 0.85F);
        bipedHead.addChild(headCap);
        headCap.setTextureOffset(18, 49).addBox(-4.5F, 0.15F, 0.19F, 9.0F, 0.0F, 2.0F, 0.13F, true);

        jaw = new ModelRenderer(this);
        jaw.setRotationPoint(0.0F, -2.0F, 0.0F);
        bipedHead.addChild(jaw);
        setRotationAngle(jaw, 0.5463F, 0.0F, 0.0F);
        jaw.setTextureOffset(33, 54).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 1.0F, 5.0F, 0.0F, true);
        jaw.setTextureOffset(10, 11).addBox(2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F, true);
        jaw.setTextureOffset(0, 15).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 1.0F, 0.0F, 0.0F, true);
        jaw.setTextureOffset(10, 11).addBox(-2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
