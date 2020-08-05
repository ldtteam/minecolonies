// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityBakerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBakerMale()
    {
        ModelRenderer headdetail;
        ModelRenderer hat;
        ModelRenderer base;
        ModelRenderer middle;
        ModelRenderer top;

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
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headdetail);
        headdetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hat = new ModelRenderer(this);
        hat.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hat);


        base = new ModelRenderer(this);
        base.setRotationPoint(0.0F, 0.0F, 0.0F);
        hat.addChild(base);
        setRotationAngle(base, -0.1859F, 0.0F, 0.0F);
        base.setTextureOffset(0, 33).addBox(-4.5F, -9.0F, -5.8F, 9.0F, 2.0F, 9.0F, 0.0F, true);

        middle = new ModelRenderer(this);
        middle.setRotationPoint(0.0F, 0.0F, 0.0F);
        hat.addChild(middle);
        setRotationAngle(middle, -0.1859F, 0.0F, 0.0F);
        middle.setTextureOffset(0, 44).addBox(-3.5F, -10.0F, -5.0F, 7.0F, 1.0F, 8.0F, 0.0F, true);

        top = new ModelRenderer(this);
        top.setRotationPoint(0.0F, 0.0F, 0.0F);
        hat.addChild(top);
        setRotationAngle(top, -0.1859F, 0.0F, 0.0F);
        top.setTextureOffset(0, 53).addBox(-2.5F, -11.0F, -4.6F, 5.0F, 1.0F, 7.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
