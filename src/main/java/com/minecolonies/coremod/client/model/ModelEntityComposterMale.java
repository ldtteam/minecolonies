// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityComposterMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityComposterMale()
    {
        ModelRenderer bootL;
        ModelRenderer bootR;
        ModelRenderer gloveL;
        ModelRenderer gloveR;
        ModelRenderer headDetail;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bootL = new ModelRenderer(this);
        bootL.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addChild(bootL);
        bootL.setTextureOffset(0, 38).addBox(-4.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bootR = new ModelRenderer(this);
        bootR.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addChild(bootR);
        bootR.setTextureOffset(20, 38).addBox(-0.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        gloveL = new ModelRenderer(this);
        gloveL.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addChild(gloveL);
        gloveL.setTextureOffset(0, 32).addBox(-6.5F, 3.0F, -2.5F, 5.0F, 1.0F, 5.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        gloveR = new ModelRenderer(this);
        gloveR.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(gloveR);
        gloveR.setTextureOffset(20, 32).addBox(1.5F, 3.0F, -2.5F, 5.0F, 1.0F, 5.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
