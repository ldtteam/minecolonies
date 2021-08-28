package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Archer mummy model. Created using Tabula 7.0.0
 */
public class ModelArcherMummy extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelPart stripRightA;
    private ModelPart stripRightB;
    private ModelPart stripLeftA;

    /**
     * Create an instance of the model.
     */
    public ModelArcherMummy()
    {
        ModelPart bodyLayer;
        ModelPart quiver;
        ModelPart arrowA;
        ModelPart arrowB;
        ModelPart arrowC;
        ModelPart armRightLayer;
        ModelPart armLeftLayer;
        ModelPart legRightLayer;
        ModelPart legLeftLayer;

        texWidth = 64;
        texHeight = 64;

        rightLeg = new ModelPart(this, 24, 16);
        rightLeg.setPos(-1.9F, 12.0F, 0.1F);
        rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        legRightLayer = new ModelPart(this, 40, 32);
        legRightLayer.setPos(0.0F, 0.0F, 0.0F);
        legRightLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);

        armLeftLayer = new ModelPart(this, 40, 32);
        armLeftLayer.mirror = true;
        armLeftLayer.setPos(0.0F, 0.0F, 0.0F);
        armLeftLayer.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);

        rightArm = new ModelPart(this, 24, 16);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        stripLeftA = new ModelPart(this, 40, -2);
        stripLeftA.setPos(3.3F, 5.0F, 2.0F);
        stripLeftA.addBox(0.0F, 0.0F, -1.0F, 0, 7, 2, 0.0F);

        head = new ModelPart(this, 0, 0);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

        armRightLayer = new ModelPart(this, 24, 32);
        armRightLayer.setPos(0.0F, 0.0F, 0.0F);
        armRightLayer.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);

        body = new ModelPart(this, 0, 16);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);

        quiver = new ModelPart(this, 0, 48);
        quiver.setPos(-4.0F, 0.0F, 2.0F);
        quiver.addBox(-1.5F, 0.0F, 0.0F, 3, 11, 3, 0.0F);
        setRotateAngle(quiver, 0.0F, 0.0F, -0.6373942428283291F);

        arrowA = new ModelPart(this, 12, 48);
        arrowA.setPos(-4.1F, 0.0F, 3.0F);
        arrowA.addBox(-2.75F, -4.5F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowA, 0.0F, 0.0F, -0.6314601233715484F);

        stripRightA = new ModelPart(this, 32, -2);
        stripRightA.setPos(-3.3F, 7.0F, 1.0F);
        stripRightA.addBox(0.0F, 0.0F, -1.0F, 0, 10, 2, 0.0F);

        leftArm = new ModelPart(this, 40, 16);
        leftArm.mirror = true;
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        arrowC = new ModelPart(this, 12, 48);
        arrowC.setPos(-3.1F, 0.0F, 3.5F);
        arrowC.addBox(-2.95F, -5.0F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowC, 0.0F, 0.0F, -0.6373942428283291F);

        bodyLayer = new ModelPart(this, 0, 32);
        bodyLayer.setPos(0.0F, 0.0F, 0.0F);
        bodyLayer.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F);

        arrowB = new ModelPart(this, 12, 48);
        arrowB.setPos(-4.9F, 0.0F, 4.0F);
        arrowB.addBox(-2.75F, -4.0F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowB, 0.0F, 0.0F, -0.6373942428283291F);

        stripRightB = new ModelPart(this, 36, -2);
        stripRightB.setPos(1.3F, 5.0F, 1.0F);
        stripRightB.addBox(0.0F, 0.0F, -1.0F, 0, 5, 2, 0.0F);

        leftLeg = new ModelPart(this, 40, 16);
        leftLeg.mirror = true;
        leftLeg.setPos(1.9F, 12.0F, 0.1F);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        legLeftLayer = new ModelPart(this, 24, 32);
        legLeftLayer.mirror = true;
        legLeftLayer.setPos(0.0F, 0.0F, 0.0F);
        legLeftLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);

        rightLeg.addChild(legRightLayer);
        leftArm.addChild(armLeftLayer);
        leftArm.addChild(stripLeftA);
        rightArm.addChild(armRightLayer);
        body.addChild(quiver);
        body.addChild(arrowA);
        rightArm.addChild(stripRightA);
        body.addChild(arrowC);
        body.addChild(bodyLayer);
        body.addChild(arrowB);
        rightArm.addChild(stripRightB);
        leftLeg.addChild(legLeftLayer);

        hat.visible = false;
    }

    /**
     * this is a helper function from tabula to set the rotation of model parts
     *
     * @param modelRenderer the model renderer.
     * @param z             the z coord.
     * @param y             the y coord.
     * @param x             the x coord.
     */
    public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = 0.05F * Mth.sin((float) Math.PI * ageInTicks / 30.0F) % 2.0F;
        setRotateAngle(this.stripLeftA,
          -1.1F * this.leftArm.xRot + f,
          -this.leftArm.yRot,
          -this.leftArm.zRot + f);
        setRotateAngle(this.stripRightA,
          -1.1F * this.rightArm.xRot + f,
          -this.rightArm.yRot,
          -this.rightArm.zRot + f);
        setRotateAngle(this.stripRightB,
          -1.1F * this.rightArm.xRot + f,
          -this.rightArm.yRot,
          -this.rightArm.zRot + f);
    }
}
