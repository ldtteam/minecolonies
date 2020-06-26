package com.minecolonies.coremod.client.model;// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Class used for the beekeeper entity model.
 */
public class ModelEntityBeekeeperMale extends CitizenModel<AbstractEntityCitizen>
{
    private final ModelRenderer hatBottom;
    private final ModelRenderer hatTop;
    private final ModelRenderer hatRight;
    private final ModelRenderer hatNeck;
    private final ModelRenderer hatM;

    public ModelEntityBeekeeperMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedHeadwear.showModel = false;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(36, 42).addBox(-1.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(36, 42).addBox(-3.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        hatBottom = new ModelRenderer(this);
        hatBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.0349F, 0.0F, 0.0F);
        hatBottom.setTextureOffset(61, 48).addBox(-5.5F, -5.4856F, -5.457F, 11.25F, 1.0F, 10.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatTop);
        setRotationAngle(hatTop, -0.0349F, 0.0F, 0.0F);
        hatTop.setTextureOffset(70, 53).addBox(-3.5F, -9.0358F, -2.9483F, 7.0F, 1.0F, 5.0F, 0.0F, true);

        hatRight = new ModelRenderer(this);
        hatRight.setRotationPoint(0.0F, 0.0F, 2.0F);
        bipedHead.addChild(hatRight);
        setRotationAngle(hatRight, -0.0349F, 0.0F, 0.0F);
        hatRight.setTextureOffset(56, 33).addBox(-5.5F, -4.3454F, 1.5018F, 11.0F, 5.0F, 1.0F, 0.0F, true);
        hatRight.setTextureOffset(56, 24).addBox(4.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatRight.setTextureOffset(56, 24).addBox(-5.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatRight.setTextureOffset(57, 31).addBox(-5.5F, -4.4849F, -7.5006F, 11.0F, 5.0F, 1.0F, 0.0F, true);

        hatNeck = new ModelRenderer(this);
        hatNeck.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatNeck);
        setRotationAngle(hatNeck, -0.3491F, 0.0F, 0.0F);
        hatNeck.setTextureOffset(98, 14).addBox(-4.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatNeck.setTextureOffset(98, 14).addBox(3.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatNeck.setTextureOffset(98, 14).addBox(-4.5F, 1.05F, -2.7F, 8.75F, 0.75F, 1.0F, 0.0F, true);

        hatM = new ModelRenderer(this);
        hatM.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hatM);
        setRotationAngle(hatM, -0.0349F, 0.0F, 0.0F);
        hatM.setTextureOffset(58, 11).addBox(-4.75F, -8.3358F, -4.6983F, 9.5F, 3.0F, 8.5F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
