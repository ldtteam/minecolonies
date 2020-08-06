// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFletcherFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFletcherFemale()
    {
        ModelRenderer knifeblade;
        ModelRenderer chest;
        ModelRenderer hair;
        ModelRenderer headDetail;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(83, 20).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F, 0.0F, true);
        bipedBody.setTextureOffset(5, 40).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(11, 40).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(26, 40).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        knifeblade = new ModelRenderer(this);
        knifeblade.setRotationPoint(2.5F, 26.0F, 0.5F);
        bipedBody.addChild(knifeblade);
        knifeblade.setTextureOffset(73, 28).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(80, 28).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(87, 28).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(70, 28).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(77, 28).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(84, 28).addBox(-0.5F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(70, 28).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(77, 28).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(84, 28).addBox(-0.75F, -15.5F, -3.25F, 0.25F, 2.25F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(84, 28).addBox(-1.0F, -15.5F, -3.25F, 0.25F, 1.75F, 0.5F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(3.0F, -3.0F, 5.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, 0.8727F, 0.0F, 0.0F);
        chest.setTextureOffset(38, 32).addBox(-6.0F, -2.5179F, -10.5745F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(69, 12).addBox(-1.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(69, 18).addBox(-3.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(32, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.75F, 5.5F, 0.0F, false);
        hair.setTextureOffset(11, 49).addBox(-4.25F, -32.0F, -0.25F, 0.25F, 5.5F, 4.25F, 0.0F, false);
        hair.setTextureOffset(111, 48).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 6.0F, 1.5F, 0.0F, false);
        hair.setTextureOffset(116, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 6.75F, 1.25F, 0.0F, false);
        hair.setTextureOffset(24, 51).addBox(4.0F, -32.0F, -3.0F, 0.25F, 5.25F, 1.5F, 0.0F, false);
        hair.setTextureOffset(52, 52).addBox(-2.5F, -19.5F, 3.0F, 5.0F, 3.0F, 1.25F, 0.0F, false);
        hair.setTextureOffset(49, 47).addBox(-3.25F, -22.5F, 3.0F, 6.75F, 3.0F, 1.25F, 0.0F, false);
        hair.setTextureOffset(66, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F, 0.0F, false);
        hair.setTextureOffset(86, 49).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F, 0.0F, false);
        hair.setTextureOffset(6, 49).addBox(1.0F, -30.0F, -4.5F, 1.25F, 1.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(39, 48).addBox(0.0F, -30.0F, -4.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(26, 48).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 2.5F, 0.5F, 0.0F, false);
        hair.setTextureOffset(0, 55).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 3.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(0, 51).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(0, 46).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(11, 49).addBox(2.25F, -30.0F, -4.5F, 0.5F, 1.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(7, 55).addBox(2.75F, -30.0F, -4.5F, 1.25F, 2.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(0, 48).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(45, 54).addBox(4.0F, -26.25F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.setTextureOffset(32, 47).addBox(-4.25F, -26.5F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.setTextureOffset(43, 47).addBox(4.0F, -26.25F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.setTextureOffset(18, 46).addBox(-4.25F, -26.5F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.setTextureOffset(28, 52).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.75F, 1.25F, 0.0F, false);
        hair.setTextureOffset(90, 49).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 5.5F, 1.25F, 0.0F, false);

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
