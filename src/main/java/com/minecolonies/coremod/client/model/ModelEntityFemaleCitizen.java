// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFemaleCitizen extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFemaleCitizen()
    {
        ModelRenderer hair;
        ModelRenderer hat;
        ModelRenderer breast;
        ModelRenderer dressPart1;
        ModelRenderer dressPart2;
        ModelRenderer dressPart3;

        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(46, 17).addBox(-4.0F, 0.4F, 2.1F, 8.0F, 7.0F, 1.0F, 0.5F, false);

        hat = new ModelRenderer(this);
        hat.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hat);
        hat.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 3.0F);
        bipedBody.setTextureOffset(12, 17).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 3.0F, 0.0F, false);

        breast = new ModelRenderer(this);
        breast.setRotationPoint(-1.0F, 3.0F, 1.0F);
        bipedBody.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.setTextureOffset(0, 33).addBox(-3.0F, 2.0F, -4.5F, 8.0F, 4.0F, 3.0F, 0.0F, false);

        dressPart1 = new ModelRenderer(this);
        dressPart1.setRotationPoint(0.0F, 11.0F, 0.0F);
        bipedBody.addChild(dressPart1);
        dressPart1.setTextureOffset(26, 46).addBox(-5.0F, 2.0F, -7.0F, 10.0F, 9.0F, 9.0F, 0.0F, false);

        dressPart2 = new ModelRenderer(this);
        dressPart2.setRotationPoint(0.0F, 11.0F, 0.0F);
        bipedBody.addChild(dressPart2);
        dressPart2.setTextureOffset(28, 38).addBox(-5.0F, 1.0F, -6.0F, 10.0F, 1.0F, 7.0F, 0.0F, false);

        dressPart3 = new ModelRenderer(this);
        dressPart3.setRotationPoint(0.0F, 11.0F, 0.0F);
        bipedBody.addChild(dressPart3);
        dressPart3.setTextureOffset(32, 32).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 1.0F, 5.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 0.0F, 0.0F);
        bipedLeftArm.setTextureOffset(34, 17).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 0.0F, 0.0F);
        bipedRightArm.setTextureOffset(34, 17).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.0F, 12.0F, 1.0F);
        bipedRightLeg.setTextureOffset(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 1.0F);
        bipedLeftLeg.setTextureOffset(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);
        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
