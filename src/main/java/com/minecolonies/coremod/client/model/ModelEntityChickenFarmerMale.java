// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityChickenFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityChickenFarmerMale()
    {
        ModelRenderer feed;
        ModelRenderer strap;
        ModelRenderer baseBag;
        ModelRenderer headDetail;
        ModelRenderer beardBot;
        ModelRenderer beardTop;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        feed = new ModelRenderer(this);
        feed.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(feed);
        feed.setTextureOffset(14, 38).addBox(3.3F, 9.8F, -2.5F, 1.0F, 2.0F, 5.0F, 0.0F, true);

        strap = new ModelRenderer(this);
        strap.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(strap);
        setRotationAngle(strap, 0.0F, 0.0F, -0.6109F);
        strap.setTextureOffset(0, 33).addBox(-4.0F, -3.0F, -3.0F, 1.0F, 14.0F, 6.0F, 0.0F, true);

        baseBag = new ModelRenderer(this);
        baseBag.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(baseBag);
        baseBag.setTextureOffset(14, 45).addBox(2.4667F, 10.0F, -3.0F, 2.0F, 2.0F, 6.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        beardBot = new ModelRenderer(this);
        beardBot.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(beardBot);
        beardBot.setTextureOffset(31, 47).addBox(-1.0F, 1.0F, -4.0F, 2.0F, 1.0F, 0.0F, 0.0F, true);

        beardTop = new ModelRenderer(this);
        beardTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(beardTop);
        beardTop.setTextureOffset(31, 52).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.0F, 0.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
