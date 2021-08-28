// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityChickenFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityChickenFarmerMale()
    {
        ModelPart feed;
        ModelPart strap;
        ModelPart baseBag;
        ModelPart headDetail;
        ModelPart beardBot;
        ModelPart beardTop;

        texWidth = 128;
        texHeight = 64;

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        feed = new ModelPart(this);
        feed.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(feed);
        feed.texOffs(14, 38).addBox(3.3F, 9.8F, -2.5F, 1.0F, 2.0F, 5.0F, 0.0F, true);

        strap = new ModelPart(this);
        strap.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(strap);
        setRotationAngle(strap, 0.0F, 0.0F, -0.6109F);
        strap.texOffs(0, 33).addBox(-4.0F, -3.0F, -3.0F, 1.0F, 14.0F, 6.0F, 0.0F, true);

        baseBag = new ModelPart(this);
        baseBag.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(baseBag);
        baseBag.texOffs(14, 45).addBox(2.4667F, 10.0F, -3.0F, 2.0F, 2.0F, 6.0F, 0.0F, true);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        beardBot = new ModelPart(this);
        beardBot.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(beardBot);
        beardBot.texOffs(31, 47).addBox(-1.0F, 1.0F, -4.0F, 2.0F, 1.0F, 0.0F, 0.0F, true);

        beardTop = new ModelPart(this);
        beardTop.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(beardTop);
        beardTop.texOffs(31, 52).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.0F, 0.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
