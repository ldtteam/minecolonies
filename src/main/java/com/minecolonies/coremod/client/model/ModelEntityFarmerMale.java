// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFarmerMale()
    {
        ModelRenderer boxBottom;
        ModelRenderer boxBack;
        ModelRenderer boxFront;
        ModelRenderer boxLeft;
        ModelRenderer boxRight;
        ModelRenderer seeds;
        ModelRenderer strapLeft;
        ModelRenderer strapRight;
        ModelRenderer headDetail;
        ModelRenderer hatStrap;
        ModelRenderer hatBottom;
        ModelRenderer hatTop;
        ModelRenderer hatFrillBottom;
        ModelRenderer hatFrillBack;
        ModelRenderer hatFrillFront;
        ModelRenderer hatFrillLeft;
        ModelRenderer hatFrillRight;

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
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        boxBottom = new ModelRenderer(this);
        boxBottom.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxBottom);
        boxBottom.texOffs(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        boxBack = new ModelRenderer(this);
        boxBack.setPos(0.0F, 9.0F, 0.0F);
        body.addChild(boxBack);
        boxBack.texOffs(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxFront = new ModelRenderer(this);
        boxFront.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxFront);
        boxFront.texOffs(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxLeft = new ModelRenderer(this);
        boxLeft.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxLeft);
        boxLeft.texOffs(42, 43).addBox(3.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, true);

        boxRight = new ModelRenderer(this);
        boxRight.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxRight);
        boxRight.texOffs(0, 43).addBox(-4.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, true);

        seeds = new ModelRenderer(this);
        seeds.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(seeds);
        seeds.texOffs(19, 45).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        strapLeft = new ModelRenderer(this);
        strapLeft.setPos(3.0F, 4.0F, -4.0F);
        body.addChild(strapLeft);
        setRotationAngle(strapLeft, 1.0472F, 0.0F, 0.0F);
        strapLeft.texOffs(92, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        strapRight = new ModelRenderer(this);
        strapRight.setPos(-4.0F, 4.0F, -4.0F);
        body.addChild(strapRight);
        setRotationAngle(strapRight, 1.0472F, 0.0F, 0.0F);
        strapRight.texOffs(110, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatStrap = new ModelRenderer(this);
        hatStrap.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatStrap);
        setRotationAngle(hatStrap, -0.3491F, 0.0F, 0.0F);
        hatStrap.texOffs(98, 14).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F, 0.0F, true);

        hatBottom = new ModelRenderer(this);
        hatBottom.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.2094F, 0.0F, 0.0F);
        hatBottom.texOffs(57, 11).addBox(-5.0F, -9.8F, -6.0F, 10.0F, 3.0F, 9.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setPos(0.0F, 0.0F, 0.0F);
        hatBottom.addChild(hatTop);
        hatTop.texOffs(64, 2).addBox(-4.5F, -10.5F, -5.0F, 9.0F, 1.0F, 7.0F, 0.0F, true);

        hatFrillBottom = new ModelRenderer(this);
        hatFrillBottom.setPos(0.0F, 0.0F, 0.0F);
        hatBottom.addChild(hatFrillBottom);
        hatFrillBottom.texOffs(57, 44).addBox(-7.5F, -6.7F, -8.5F, 15.0F, 1.0F, 14.0F, 0.0F, true);

        hatFrillBack = new ModelRenderer(this);
        hatFrillBack.setPos(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillBack);
        hatFrillBack.texOffs(87, 40).addBox(-6.5F, -7.7F, 4.5F, 13.0F, 1.0F, 1.0F, 0.0F, true);

        hatFrillFront = new ModelRenderer(this);
        hatFrillFront.setPos(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillFront);
        hatFrillFront.texOffs(57, 40).addBox(-6.5F, -7.7F, -8.5F, 13.0F, 1.0F, 1.0F, 0.0F, true);

        hatFrillLeft = new ModelRenderer(this);
        hatFrillLeft.setPos(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillLeft);
        hatFrillLeft.texOffs(57, 24).addBox(6.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F, 0.0F, true);

        hatFrillRight = new ModelRenderer(this);
        hatFrillRight.setPos(0.0F, 0.0F, 0.0F);
        hatFrillBottom.addChild(hatFrillRight);
        hatFrillRight.texOffs(88, 24).addBox(-7.5F, -7.7F, -8.5F, 1.0F, 1.0F, 14.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
