// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFletcherMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFletcherMale()
    {
        ModelRenderer knifeblade;
        ModelRenderer hair;
        ModelRenderer headDetail;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(87, 18).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F, 0.0F, true);
        bipedBody.setTextureOffset(5, 38).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(8, 39).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(12, 39).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        knifeblade = new ModelRenderer(this);
        knifeblade.setRotationPoint(2.5F, 26.0F, 0.5F);
        bipedBody.addChild(knifeblade);
        knifeblade.setTextureOffset(72, 26).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(79, 26).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(86, 26).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(69, 26).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(76, 26).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(83, 26).addBox(-0.5F, -15.5F, -3.35F, 0.25F, 2.5F, 0.6F, 0.0F, false);
        knifeblade.setTextureOffset(69, 26).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(76, 26).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(83, 26).addBox(-0.75F, -15.5F, -3.35F, 0.25F, 2.25F, 0.6F, 0.0F, false);
        knifeblade.setTextureOffset(83, 26).addBox(-1.0F, -15.5F, -3.35F, 0.25F, 1.75F, 0.6F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(69, 19).addBox(-1.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(69, 13).addBox(-3.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(101, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F, 0.0F, false);
        hair.setTextureOffset(0, 45).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F, 0.0F, false);
        hair.setTextureOffset(52, 52).addBox(1.0F, -30.0F, -4.5F, 1.25F, 0.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(45, 56).addBox(0.0F, -30.0F, -4.5F, 1.0F, 0.5F, 0.5F, 0.0F, false);
        hair.setTextureOffset(33, 47).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 0.75F, 0.5F, 0.0F, false);
        hair.setTextureOffset(41, 47).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 1.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(58, 52).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(58, 55).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F, 0.0F, false);
        hair.setTextureOffset(36, 54).addBox(2.25F, -29.8F, -4.5F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        hair.setTextureOffset(50, 47).addBox(2.75F, -29.8F, -4.5F, 1.25F, 1.5F, 0.5F, 0.0F, false);
        hair.setTextureOffset(54, 47).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.25F, 0.5F, 0.0F, false);
        hair.setTextureOffset(77, 46).addBox(4.0F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.setTextureOffset(88, 48).addBox(-4.25F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.setTextureOffset(83, 45).addBox(4.0F, -27.0F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.setTextureOffset(95, 45).addBox(-4.25F, -27.0F, 1.75F, 0.25F, 4.0F, 2.25F, 0.0F, false);
        hair.setTextureOffset(73, 46).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.0F, 1.25F, 0.0F, false);
        hair.setTextureOffset(61, 46).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 3.0F, 1.25F, 0.0F, false);
        hair.setTextureOffset(69, 45).addBox(4.0F, -32.0F, -3.0F, 0.25F, 4.5F, 1.5F, 0.0F, false);
        hair.setTextureOffset(65, 45).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 4.25F, 1.5F, 0.0F, false);
        hair.setTextureOffset(76, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);
        hair.setTextureOffset(88, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);

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
