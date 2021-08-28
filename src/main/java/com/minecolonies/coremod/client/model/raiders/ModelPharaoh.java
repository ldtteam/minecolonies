package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * ModelPharaohMummy. Created using Tabula 7.0.0
 */
public class ModelPharaoh extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelPart bodyGoldenStrip;
    private ModelPart jaw;

    public ModelPharaoh()
    {
        ModelPart bodyJewel;
        ModelPart snakeBody;
        ModelPart snakeHead;
        ModelPart headRightSideTop;
        ModelPart headRightSideMiddle;
        ModelPart headRightSideBottom;
        ModelPart headLeftSideTop;
        ModelPart headLeftSideMiddle;
        ModelPart headLeftSideBottom;
        ModelPart headTail;
        ModelPart headTop;
        ModelPart headCap;

        texWidth = 128;
        texHeight = 64;

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(24, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        leftArm.texOffs(24, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.3F, true);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(1.9F, 12.0F, 0.1F);
        leftLeg.texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        leftLeg.texOffs(56, 20).addBox(-2.4F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F, 0.0F, true);
        leftLeg.texOffs(40, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.2F, true);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        body.texOffs(44, 0).addBox(-5.5F, -0.2F, -2.5F, 11.0F, 5.0F, 5.0F, 0.15F, true);
        body.texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.3F, true);
        body.texOffs(52, 11).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 4.0F, 5.0F, 0.0F, true);

        bodyJewel = new ModelPart(this);
        bodyJewel.setPos(0.0F, 10.0F, -2.0F);
        body.addChild(bodyJewel);
        setRotationAngle(bodyJewel, 0.0F, 0.0F, 0.7854F);
        bodyJewel.texOffs(0, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, -0.3F, true);

        bodyGoldenStrip = new ModelPart(this);
        bodyGoldenStrip.setPos(0.0F, 10.0F, -2.6F);
        body.addChild(bodyGoldenStrip);
        bodyGoldenStrip.texOffs(38, 12).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 8.0F, 0.0F, 0.0F, true);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.3F, false);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.1F);
        rightLeg.texOffs(24, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(56, 20).addBox(-2.6F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F, 0.0F, false);
        rightLeg.texOffs(24, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.2F, false);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, -1.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, true);
        head.texOffs(80, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, 0.1F, true);
        head.texOffs(38, 48).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 2.0F, 4.0F, 0.0F, true);
        head.texOffs(14, 59).addBox(-4.5F, -2.85F, 1.0F, 9.0F, 3.0F, 0.0F, 0.0F, true);
        head.texOffs(0, 14).addBox(-2.5F, -2.0F, -3.5F, 5.0F, 1.0F, 0.0F, 0.0F, true);
        head.texOffs(10, 10).addBox(-2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F, 0.0F, true);
        head.texOffs(10, 10).addBox(2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F, 0.0F, true);

        snakeBody = new ModelPart(this);
        snakeBody.setPos(0.0F, -7.5F, -4.6F);
        head.addChild(snakeBody);
        setRotationAngle(snakeBody, -0.3491F, 0.0F, 0.0F);
        snakeBody.texOffs(76, 2).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        snakeHead = new ModelPart(this);
        snakeHead.setPos(0.0F, -10.65F, -4.5F);
        head.addChild(snakeHead);
        setRotationAngle(snakeHead, 0.2793F, 0.0F, 0.0F);
        snakeHead.texOffs(77, 3).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 3.0F, 3.0F, -0.99F, true);

        headRightSideTop = new ModelPart(this);
        headRightSideTop.setPos(-4.6F, -11.4F, 0.86F);
        head.addChild(headRightSideTop);
        setRotationAngle(headRightSideTop, 0.0F, 0.0F, -0.8901F);
        headRightSideTop.texOffs(0, 51).addBox(-5.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, 0.13F, false);

        headRightSideMiddle = new ModelPart(this);
        headRightSideMiddle.setPos(-7.82F, -7.11F, 0.86F);
        head.addChild(headRightSideMiddle);
        setRotationAngle(headRightSideMiddle, 0.0F, 0.0F, 0.2688F);
        headRightSideMiddle.texOffs(14, 51).addBox(0.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, 0.14F, false);

        headRightSideBottom = new ModelPart(this);
        headRightSideBottom.setPos(-9.58F, -1.23F, 0.91F);
        head.addChild(headRightSideBottom);
        setRotationAngle(headRightSideBottom, 0.0F, -0.9147F, 0.2688F);
        headRightSideBottom.texOffs(0, 57).addBox(0.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F, 0.0F, false);

        headLeftSideTop = new ModelPart(this);
        headLeftSideTop.setPos(4.6F, -11.4F, 0.86F);
        head.addChild(headLeftSideTop);
        setRotationAngle(headLeftSideTop, 0.0F, 0.0F, 0.8901F);
        headLeftSideTop.texOffs(0, 51).addBox(0.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, 0.13F, true);

        headLeftSideMiddle = new ModelPart(this);
        headLeftSideMiddle.setPos(7.82F, -7.11F, 0.86F);
        head.addChild(headLeftSideMiddle);
        setRotationAngle(headLeftSideMiddle, 0.0F, 0.0F, -0.2688F);
        headLeftSideMiddle.texOffs(14, 51).addBox(-5.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, 0.14F, true);

        headLeftSideBottom = new ModelPart(this);
        headLeftSideBottom.setPos(9.58F, -1.23F, 0.91F);
        head.addChild(headLeftSideBottom);
        setRotationAngle(headLeftSideBottom, 0.0F, 0.9147F, -0.2688F);
        headLeftSideBottom.texOffs(0, 57).addBox(-3.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F, 0.0F, true);

        headTail = new ModelPart(this);
        headTail.setPos(1.5F, 0.4F, 2.5F);
        head.addChild(headTail);
        headTail.texOffs(28, 51).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 2.0F, -0.2F, true);

        headTop = new ModelPart(this);
        headTop.setPos(0.0F, -8.55F, -4.45F);
        head.addChild(headTop);
        setRotationAngle(headTop, 0.4714F, 0.0F, 0.0F);
        headTop.texOffs(76, 18).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 4.0F, 8.0F, 0.09F, true);

        headCap = new ModelPart(this);
        headCap.setPos(0.0F, -11.4F, 0.85F);
        head.addChild(headCap);
        headCap.texOffs(18, 49).addBox(-4.5F, 0.15F, 0.19F, 9.0F, 0.0F, 2.0F, 0.13F, true);

        jaw = new ModelPart(this);
        jaw.setPos(0.0F, -2.0F, 0.0F);
        head.addChild(jaw);
        jaw.texOffs(33, 54).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 1.0F, 5.0F, 0.0F, true);
        jaw.texOffs(10, 11).addBox(2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F, true);
        jaw.texOffs(0, 15).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 1.0F, 0.0F, 0.0F, true);
        jaw.texOffs(10, 11).addBox(-2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F, true);

        hat.visible = false;
    }

    private void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    private static float sinPi(float f)
    {
        return Mth.sin(f * (float) Math.PI);
    }

    @Override
    public void setupAnim(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bodyGoldenStrip.xRot = -Math.max(this.rightLeg.xRot, this.leftLeg.xRot);
        this.jaw.xRot = 0.3F - 0.1F * sinPi(ageInTicks / 20.0F) % 2.0F;
        this.jaw.yRot = 0.05F * sinPi((ageInTicks + 10.0F) / 20.0F) % 2.0F;
    }
}
