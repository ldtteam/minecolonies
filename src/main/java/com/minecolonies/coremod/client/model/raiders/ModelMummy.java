package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * ModelMummy - Either Mojang or a mod author
 * Created using Tabula 7.0.0
 */
public class ModelMummy extends EgyptianModel<AbstractEntityEgyptian>
{
    public ModelMummy()
    {
        ModelRenderer bodyLayer;
        ModelRenderer stripRightA;
        ModelRenderer stripRightB;
        ModelRenderer armRightLayer;
        ModelRenderer stripRightA_1;
        ModelRenderer armLeftLayer;
        ModelRenderer legRightLayer;
        ModelRenderer legLeftLayer;
        
        textureWidth = 64;
        textureHeight = 64;
        stripRightB = new ModelRenderer(this, 36, -2);
        stripRightB.setRotationPoint(1.3F, 5.0F, 1.0F);
        stripRightB.addBox(0.0F, 0.0F, -1.0F, 0, 5, 2, 0.0F);
        setRotateAngle(stripRightB, 1.3962634015954636F, 0.10000736613927509F, 0.0F);
        bipedBody = new ModelRenderer(this, 0, 16);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        armRightLayer = new ModelRenderer(this, 24, 32);
        armRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armRightLayer.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        bipedRightLeg = new ModelRenderer(this, 24, 16);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        setRotateAngle(bipedRightLeg, -0.5918411493512771F, 0.0F, 0.0F);
        stripRightA_1 = new ModelRenderer(this, 40, -2);
        stripRightA_1.setRotationPoint(3.3F, 5.0F, 2.0F);
        stripRightA_1.addBox(0.0F, 0.0F, -1.0F, 0, 7, 2, 0.0F);
        setRotateAngle(stripRightA_1, 1.3962634015954636F, -0.10000736613927509F, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        setRotateAngle(bipedLeftArm, -1.3962634015954636F, 0.10000736613927509F, -0.10000736613927509F);
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 40, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        setRotateAngle(bipedLeftLeg, 0.5918411493512771F, 0.0F, 0.0F);
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
        setRotateAngle(bipedRightArm, -1.3962634015954636F, -0.10000736613927509F, 0.10000736613927509F);
        armLeftLayer = new ModelRenderer(this, 40, 32);
        armLeftLayer.mirror = true;
        armLeftLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        armLeftLayer.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F);
        stripRightA = new ModelRenderer(this, 32, -2);
        stripRightA.setRotationPoint(-3.3F, 7.0F, 1.0F);
        stripRightA.addBox(0.0F, 0.0F, -1.0F, 0, 10, 2, 0.0F);
        setRotateAngle(stripRightA, 1.3962634015954636F, 0.10000736613927509F, 0.0F);
        legRightLayer = new ModelRenderer(this, 40, 32);
        legRightLayer.setRotationPoint(0.0F, 0.0F, 0.0F);
        legRightLayer.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F);
        bipedRightArm.addChild(stripRightB);
        bipedRightArm.addChild(armRightLayer);
        bipedLeftArm.addChild(stripRightA_1);
        bipedBody.addChild(bodyLayer);
        bipedLeftLeg.addChild(legLeftLayer);
        bipedLeftArm.addChild(armLeftLayer);
        bipedRightArm.addChild(stripRightA);
        bipedRightLeg.addChild(legRightLayer);

        bipedHeadwear.showModel = false;
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
