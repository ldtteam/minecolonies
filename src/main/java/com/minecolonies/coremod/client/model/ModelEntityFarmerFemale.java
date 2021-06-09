// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFarmerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFarmerFemale()
    {
        ModelRenderer chest;
        ModelRenderer boxBottom;
        ModelRenderer boxFront;
        ModelRenderer boxBack;
        ModelRenderer boxLeft;
        ModelRenderer boxRight;
        ModelRenderer seeds;
        ModelRenderer strapLeft;
        ModelRenderer strapRight;
        ModelRenderer headDetail;
        ModelRenderer hatFrill;
        ModelRenderer hatBottom;
        ModelRenderer hatTop;
        ModelRenderer hatStrap;
        ModelRenderer ponytailBase;
        ModelRenderer ponytailTail;

        texWidth = 128;
        texHeight = 64;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
        chest.texOffs(17, 32).addBox(-3.5F, 2.7F, -0.6F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        boxBottom = new ModelRenderer(this);
        boxBottom.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxBottom);
        boxBottom.texOffs(19, 50).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        boxFront = new ModelRenderer(this);
        boxFront.setPos(0.0F, 9.0F, -4.0F);
        body.addChild(boxFront);
        boxFront.texOffs(21, 55).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

        boxBack = new ModelRenderer(this);
        boxBack.setPos(0.0F, 9.0F, 0.0F);
        body.addChild(boxBack);
        boxBack.texOffs(21, 40).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 1.0F, 0.0F, true);

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
        strapLeft.texOffs(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        strapRight = new ModelRenderer(this);
        strapRight.setPos(-4.0F, 4.0F, -4.0F);
        body.addChild(strapRight);
        setRotationAngle(strapRight, 1.0472F, 0.0F, 0.0F);
        strapRight.texOffs(0, 55).addBox(0.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatFrill = new ModelRenderer(this);
        hatFrill.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatFrill);
        setRotationAngle(hatFrill, -0.6981F, 0.0F, 0.0F);
        hatFrill.texOffs(57, 21).addBox(-5.5F, -5.7F, -8.0F, 11.0F, 1.0F, 10.0F, 0.0F, true);

        hatBottom = new ModelRenderer(this);
        hatBottom.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.7854F, 0.0F, 0.0F);
        hatBottom.texOffs(61, 9).addBox(-5.0F, -7.8F, -7.0F, 10.0F, 3.0F, 8.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatTop);
        setRotationAngle(hatTop, -0.7854F, 0.0F, 0.0F);
        hatTop.texOffs(64, 1).addBox(-4.5F, -8.5F, -6.0F, 9.0F, 1.0F, 6.0F, 0.0F, true);

        hatStrap = new ModelRenderer(this);
        hatStrap.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatStrap);
        setRotationAngle(hatStrap, -0.3491F, 0.0F, 0.0F);
        hatStrap.texOffs(68, 33).addBox(-4.5F, -6.7F, -2.7F, 9.0F, 8.0F, 1.0F, 0.0F, true);

        ponytailBase = new ModelRenderer(this);
        ponytailBase.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailBase);
        setRotationAngle(ponytailBase, 0.1047F, 0.0F, 0.0F);
        ponytailBase.texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        ponytailTail = new ModelRenderer(this);
        ponytailTail.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailTail);
        setRotationAngle(ponytailTail, 0.2269F, 0.0F, 0.0F);
        ponytailTail.texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
