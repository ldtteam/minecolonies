// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityPigFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityPigFarmerMale()
    {
        ModelRenderer carrot1;
        ModelRenderer carrot2;
        ModelRenderer carrot3;
        ModelRenderer carrot4;
        ModelRenderer carrotBase;
        ModelRenderer strapL;
        ModelRenderer strapR;
        ModelRenderer headDetail;

        texWidth = 128;
        texHeight = 64;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        carrot1 = new ModelRenderer(this);
        carrot1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(carrot1);
        setRotationAngle(carrot1, -0.1115F, 0.0F, -0.0175F);
        carrot1.texOffs(0, 33).addBox(-2.5F, 6.0F, -1.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot2 = new ModelRenderer(this);
        carrot2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(carrot2);
        setRotationAngle(carrot2, 0.0F, 0.3346F, 0.1115F);
        carrot2.texOffs(2, 33).addBox(0.5F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot3 = new ModelRenderer(this);
        carrot3.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(carrot3);
        setRotationAngle(carrot3, 0.0F, -0.1115F, 0.1487F);
        carrot3.texOffs(4, 33).addBox(1.0F, 6.0F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrot4 = new ModelRenderer(this);
        carrot4.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(carrot4);
        setRotationAngle(carrot4, 0.0F, -0.1487F, -0.1859F);
        carrot4.texOffs(6, 33).addBox(0.0F, 6.5F, -2.5F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        carrotBase = new ModelRenderer(this);
        carrotBase.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(carrotBase);
        carrotBase.texOffs(0, 49).addBox(-3.5F, 8.0F, -3.5F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        strapL = new ModelRenderer(this);
        strapL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(strapL);
        setRotationAngle(strapL, -0.0698F, 0.0F, 0.0F);
        strapL.texOffs(10, 36).addBox(2.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F, 0.0F, true);

        strapR = new ModelRenderer(this);
        strapR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(strapR);
        setRotationAngle(strapR, -0.0698F, 0.0F, 0.0F);
        strapR.texOffs(0, 36).addBox(-3.8F, 0.01F, -2.5F, 1.0F, 9.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
