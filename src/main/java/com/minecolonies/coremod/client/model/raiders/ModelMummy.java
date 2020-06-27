package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/**
 * Create a mummy model.
 * Created using Tabula 7.0.0
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
        
        textureWidth = 64;
        textureHeight = 64;
        stripRightB = new ModelRenderer(this, 36, -2);
        stripRightB.setRotationPoint(1.3F, 5.0F, 1.0F);
        stripRightB.addBox(0.0F, 0.0F, -1.0F, 0, 5, 2, 0.0F);
        bipedBody = new ModelRenderer(this, 0, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        armRightLayer = new ModelRenderer(this, 24, 32);
        armRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armRightLayer.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        bipedRightLeg = new ModelRenderer(this, 24, 16);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        stripLeftA = new ModelRenderer(this, 40, -2);
        stripLeftA.setRotationPoint(3.3F, 5.0F, 2.0F);
        stripLeftA.addBox(0.0F, 0.0F, -1.0F, 0, 7, 2, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 40, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        bodyLayer = new ModelRenderer(this, 0, 32);
        bodyLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        bodyLayer.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F);
        legLeftLayer = new ModelRenderer(this, 24, 32);
        legLeftLayer.mirror = true;
        legLeftLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        legLeftLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);
        bipedRightArm = new ModelRenderer(this, 24, 16);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        armLeftLayer = new ModelRenderer(this, 40, 32);
        armLeftLayer.mirror = true;
        armLeftLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armLeftLayer.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        stripRightA = new ModelRenderer(this, 32, -2);
        stripRightA.setRotationPoint(-3.3F, 7.0F, 1.0F);
        stripRightA.addBox(0.0F, 0.0F, -1.0F, 0, 10, 2, 0.0F);
        legRightLayer = new ModelRenderer(this, 40, 32);
        legRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        legRightLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);
        bipedRightArm.addChild(stripRightB);
        bipedRightArm.addChild(armRightLayer);
        bipedLeftArm.addChild(stripLeftA);
        bipedBody.addChild(bodyLayer);
        bipedLeftLeg.addChild(legLeftLayer);
        bipedLeftArm.addChild(armLeftLayer);
        bipedRightArm.addChild(stripRightA);
        bipedRightLeg.addChild(legRightLayer);

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
    public void setRotationAngles(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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
