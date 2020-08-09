// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntitySheepFarmerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntitySheepFarmerMale()
    {
        ModelRenderer bagR;
        ModelRenderer bagL;
        ModelRenderer bagBack;
        ModelRenderer bagFront;
        ModelRenderer bagWheat;
        ModelRenderer bagBot;
        ModelRenderer headDetail;

        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        bagR = new ModelRenderer(this);
        bagR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagR);
        bagR.setTextureOffset(0, 34).addBox(3.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagL = new ModelRenderer(this);
        bagL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagL);
        bagL.setTextureOffset(1, 38).addBox(-4.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

        bagBack = new ModelRenderer(this);
        bagBack.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagBack);
        bagBack.setTextureOffset(2, 34).addBox(-3.0F, 0.0F, 2.0F, 6.0F, 9.0F, 1.0F, 0.0F, true);

        bagFront = new ModelRenderer(this);
        bagFront.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagFront);
        bagFront.setTextureOffset(2, 39).addBox(-3.0F, 1.0F, 6.0F, 6.0F, 8.0F, 1.0F, 0.0F, true);

        bagWheat = new ModelRenderer(this);
        bagWheat.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagWheat);
        bagWheat.setTextureOffset(19, 37).addBox(-3.0F, 1.5F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bagBot = new ModelRenderer(this);
        bagBot.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(bagBot);
        bagBot.setTextureOffset(0, 46).addBox(-3.0F, 9.0F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
