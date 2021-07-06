package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/**
 * Create a mummy model. Created using Tabula 7.0.0
 */
public class ModelMummy extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelRenderer stripRightA;
    private ModelRenderer stripRightB;
    private ModelRenderer stripLeftA;

    public ModelMummy()
    {
        ModelRenderer bodyLayer;
        ModelRenderer armRightLayer;
        ModelRenderer armLeftLayer;
        ModelRenderer legRightLayer;
        ModelRenderer legLeftLayer;

        texWidth = 64;
        texHeight = 64;
        stripRightB = new ModelRenderer(this, 36, -2);
        stripRightB.setPos(1.3F, 5.0F, 1.0F);
        stripRightB.addBox(0.0F, 0.0F, -1.0F, 0, 5, 2, 0.0F);
        body = new ModelRenderer(this, 0, 16);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        armRightLayer = new ModelRenderer(this, 24, 32);
        armRightLayer.setPos(0.0F, 0.0F, 0.0F);
        armRightLayer.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        rightLeg = new ModelRenderer(this, 24, 16);
        rightLeg.setPos(-1.9F, 12.0F, 0.1F);
        rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        stripLeftA = new ModelRenderer(this, 40, -2);
        stripLeftA.setPos(3.3F, 5.0F, 2.0F);
        stripLeftA.addBox(0.0F, 0.0F, -1.0F, 0, 7, 2, 0.0F);
        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.mirror = true;
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        head = new ModelRenderer(this, 0, 0);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        leftLeg = new ModelRenderer(this, 40, 16);
        leftLeg.mirror = true;
        leftLeg.setPos(1.9F, 12.0F, 0.1F);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        bodyLayer = new ModelRenderer(this, 0, 32);
        bodyLayer.setPos(0.0F, 0.0F, 0.0F);
        bodyLayer.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F);
        legLeftLayer = new ModelRenderer(this, 24, 32);
        legLeftLayer.mirror = true;
        legLeftLayer.setPos(0.0F, 0.0F, 0.0F);
        legLeftLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);
        rightArm = new ModelRenderer(this, 24, 16);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        armLeftLayer = new ModelRenderer(this, 40, 32);
        armLeftLayer.mirror = true;
        armLeftLayer.setPos(0.0F, 0.0F, 0.0F);
        armLeftLayer.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        stripRightA = new ModelRenderer(this, 32, -2);
        stripRightA.setPos(-3.3F, 7.0F, 1.0F);
        stripRightA.addBox(0.0F, 0.0F, -1.0F, 0, 10, 2, 0.0F);
        legRightLayer = new ModelRenderer(this, 40, 32);
        legRightLayer.setPos(0.0F, 0.0F, 0.0F);
        legRightLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);
        rightArm.addChild(stripRightB);
        rightArm.addChild(armRightLayer);
        leftArm.addChild(stripLeftA);
        body.addChild(bodyLayer);
        leftLeg.addChild(legLeftLayer);
        leftArm.addChild(armLeftLayer);
        rightArm.addChild(stripRightA);
        rightLeg.addChild(legRightLayer);

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
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = 0.05F * MathHelper.sin((float) Math.PI * ageInTicks / 30.0F) % 2.0F;
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
