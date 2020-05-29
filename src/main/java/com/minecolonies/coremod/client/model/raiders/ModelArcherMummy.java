package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/**
 * Archer mummy model.
 * Created using Tabula 7.0.0
 */
public class ModelArcherMummy extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelRenderer stripRightA;
    private ModelRenderer stripRightB;
    private ModelRenderer stripLeftA;

    /**
     * Create an instance of the model.
     */
    public ModelArcherMummy()
    {
        ModelRenderer bodyLayer;
        ModelRenderer quiver;
        ModelRenderer arrowA;
        ModelRenderer arrowB;
        ModelRenderer arrowC;
        ModelRenderer armRightLayer;
        ModelRenderer armLeftLayer;
        ModelRenderer legRightLayer;
        ModelRenderer legLeftLayer;

        textureWidth = 64;
        textureHeight = 64;

        bipedRightLeg = new ModelRenderer(this, 24, 16);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        legRightLayer = new ModelRenderer(this, 40, 32);
        legRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        legRightLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);

        armLeftLayer = new ModelRenderer(this, 40, 32);
        armLeftLayer.mirror = true;
        armLeftLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armLeftLayer.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);

        bipedRightArm = new ModelRenderer(this, 24, 16);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        stripLeftA = new ModelRenderer(this, 40, -2);
        stripLeftA.setRotationPoint(3.3F, 5.0F, 2.0F);
        stripLeftA.addBox(0.0F, 0.0F, -1.0F, 0, 7, 2, 0.0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

        armRightLayer = new ModelRenderer(this, 24, 32);
        armRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armRightLayer.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);

        bipedBody = new ModelRenderer(this, 0, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);

        quiver = new ModelRenderer(this, 0, 48);
        quiver.setRotationPoint(-4.0F, 0.0F, 2.0F);
        quiver.addBox(-1.5F, 0.0F, 0.0F, 3, 11, 3, 0.0F);
        setRotateAngle(quiver, 0.0F, 0.0F, -0.6373942428283291F);

        arrowA = new ModelRenderer(this, 12, 48);
        arrowA.setRotationPoint(-4.1F, 0.0F, 3.0F);
        arrowA.addBox(-2.75F, -4.5F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowA, 0.0F, 0.0F, -0.6314601233715484F);

        stripRightA = new ModelRenderer(this, 32, -2);
        stripRightA.setRotationPoint(-3.3F, 7.0F, 1.0F);
        stripRightA.addBox(0.0F, 0.0F, -1.0F, 0, 10, 2, 0.0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);

        arrowC = new ModelRenderer(this, 12, 48);
        arrowC.setRotationPoint(-3.1F, 0.0F, 3.5F);
        arrowC.addBox(-2.95F, -5.0F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowC, 0.0F, 0.0F, -0.6373942428283291F);

        bodyLayer = new ModelRenderer(this, 0, 32);
        bodyLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        bodyLayer.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F);

        arrowB = new ModelRenderer(this, 12, 48);
        arrowB.setRotationPoint(-4.9F, 0.0F, 4.0F);
        arrowB.addBox(-2.75F, -4.0F, -1.5F, 6, 6, 3, -1.5F);
        setRotateAngle(arrowB, 0.0F, 0.0F, -0.6373942428283291F);

        stripRightB = new ModelRenderer(this, 36, -2);
        stripRightB.setRotationPoint(1.3F, 5.0F, 1.0F);
        stripRightB.addBox(0.0F, 0.0F, -1.0F, 0, 5, 2, 0.0F);

        bipedLeftLeg = new ModelRenderer(this, 40, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

        legLeftLayer = new ModelRenderer(this, 24, 32);
        legLeftLayer.mirror = true;
        legLeftLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        legLeftLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);

        bipedRightLeg.addChild(legRightLayer);
        bipedLeftArm.addChild(armLeftLayer);
        bipedLeftArm.addChild(stripLeftA);
        bipedRightArm.addChild(armRightLayer);
        bipedBody.addChild(quiver);
        bipedBody.addChild(arrowA);
        bipedRightArm.addChild(stripRightA);
        bipedBody.addChild(arrowC);
        bipedBody.addChild(bodyLayer);
        bipedBody.addChild(arrowB);
        bipedRightArm.addChild(stripRightB);
        bipedLeftLeg.addChild(legLeftLayer);

        bipedHeadwear.showModel = false;
    }

    /**
     * this is a helper function from tabula to set the rotation of model parts
     * @param modelRenderer the model renderer.
     * @param z the z coord.
     * @param y the y coord.
     * @param x the x coord.
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void render(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = 0.05F * MathHelper.sin((float) Math.PI * ageInTicks/ 30.0F) % 2.0F;
        setRotateAngle(this.stripLeftA,
                - 1.1F * this.bipedLeftArm.rotateAngleX + f,
                - this.bipedLeftArm.rotateAngleY,
                - this.bipedLeftArm.rotateAngleZ + f);
        setRotateAngle(this.stripRightA,
                - 1.1F * this.bipedRightArm.rotateAngleX + f,
                - this.bipedRightArm.rotateAngleY,
                - this.bipedRightArm.rotateAngleZ + f);
        setRotateAngle(this.stripRightB,
                - 1.1F * this.bipedRightArm.rotateAngleX + f,
                - this.bipedRightArm.rotateAngleY,
                - this.bipedRightArm.rotateAngleZ + f);
    }
}
