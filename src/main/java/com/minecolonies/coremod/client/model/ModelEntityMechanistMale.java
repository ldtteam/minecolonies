// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMechanistMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMechanistMale()
    {
        ModelRenderer belt;
        ModelRenderer torch1;
        ModelRenderer torch2;
        ModelRenderer torch3;
        ModelRenderer gloveright;
        ModelRenderer gloveleft;
        ModelRenderer headDetail;
        ModelRenderer mask;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        belt = new ModelRenderer(this);
        belt.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(belt);
        belt.setTextureOffset(0, 42).addBox(-0.5F, -14.0F, -4.0F, 4.0F, 3.0F, 2.0F, 0.0F, true);
        belt.setTextureOffset(13, 45).addBox(1.0F, -13.45F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        belt.setTextureOffset(0, 33).addBox(-4.5F, -13.0F, -3.0F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        torch1 = new ModelRenderer(this);
        torch1.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(torch1);
        torch1.setTextureOffset(0, 56).addBox(-0.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch1.setTextureOffset(0, 62).addBox(-0.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch1.setTextureOffset(1, 54).addBox(0.0F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        torch1.setTextureOffset(1, 57).addBox(0.0F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);

        torch2 = new ModelRenderer(this);
        torch2.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(torch2);
        torch2.setTextureOffset(5, 62).addBox(1.0F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch2.setTextureOffset(5, 56).addBox(1.0F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch2.setTextureOffset(6, 57).addBox(1.25F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        torch2.setTextureOffset(6, 54).addBox(1.25F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);

        torch3 = new ModelRenderer(this);
        torch3.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(torch3);
        torch3.setTextureOffset(11, 57).addBox(2.5F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        torch3.setTextureOffset(10, 56).addBox(2.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch3.setTextureOffset(10, 62).addBox(2.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch3.setTextureOffset(11, 54).addBox(2.5F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        gloveright = new ModelRenderer(this);
        gloveright.setRotationPoint(5.0F, 22.0F, 0.0F);
        bipedRightArm.addChild(gloveright);
        gloveright.setTextureOffset(56, 16).addBox(-8.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);
        gloveright.setTextureOffset(96, 10).addBox(-9.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        gloveleft = new ModelRenderer(this);
        gloveleft.setRotationPoint(-5.0F, 22.0F, 0.0F);
        bipedLeftArm.addChild(gloveleft);
        gloveleft.setTextureOffset(96, 17).addBox(3.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);
        gloveleft.setTextureOffset(76, 16).addBox(3.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        mask = new ModelRenderer(this);
        mask.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(mask);
        mask.setTextureOffset(64, 8).addBox(3.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);
        mask.setTextureOffset(56, 0).addBox(-4.5F, -32.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);
        mask.setTextureOffset(76, 10).addBox(-4.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.setTextureOffset(77, 1).addBox(-4.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.setTextureOffset(82, 2).addBox(-4.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);
        mask.setTextureOffset(56, 4).addBox(-4.5F, -25.0F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);
        mask.setTextureOffset(24, 0).addBox(1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(82, 10).addBox(-4.0F, -28.0F, -5.0F, 8.0F, 3.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(72, 9).addBox(-1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(77, 0).addBox(3.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(30, 0).addBox(-3.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(94, 7).addBox(-4.0F, -32.0F, -5.0F, 8.0F, 2.0F, 1.0F, 0.0F, true);
        mask.setTextureOffset(89, 1).addBox(3.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.setTextureOffset(99, 1).addBox(3.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.setTextureOffset(83, 0).addBox(-4.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
